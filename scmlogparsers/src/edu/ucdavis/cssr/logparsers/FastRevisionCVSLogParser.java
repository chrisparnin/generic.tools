package edu.ucdavis.cssr.logparsers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.*;
//import java.sql.*;



public class FastRevisionCVSLogParser extends LogParser{
	
	class Revision implements Comparable<Revision>{
		public String project, module, author, logMessage, filename, revision, revisionLink, state;
		public java.util.Date date;
		public String originalDateString;
		public int transactionId = 0;
		public int compareTo(Revision o) {
			return date.compareTo(o.date);
		}
	}
	
	class Transaction implements Comparable<Transaction>{
		public List<Revision> Revisions;
		public int Id;
		public Transaction(List<Revision> revs)
		{
			this.Revisions = revs;
			Collections.sort(this.Revisions);
		}
		
		public Transaction Parent;
		
		public Revision First()
		{
			return this.Revisions.get(0);
		}
		public Revision Last()
		{
			return this.Revisions.get(this.Revisions.size() - 1);
		}
	
		public Revision HasRevisionWithFile(String file)
		{
			for( Revision r: Revisions)
			{
				if( r.filename.equals(file) )
				{
					return r;
				}
			}
			return null;
		}

		
		public int compareTo(Transaction o) {
			return this.Last().compareTo(o.Last());
		}
	}

	private String project;
	private String module;
	public void setProject(String project, String module)  {
		this.project = project;
		this.module = module;
	}
	
	public String escape(String s) {
    	return s.replaceAll("\t", "\\t").replace("\n", "\\n");
    }
	
    public void parseLog(File file, Connection conn) {
		try {
			
			File dataFile = new File(project + "/" + module + ".data");
			System.out.println("writing to datafile: " + dataFile.getAbsolutePath());
			BufferedWriter dataWriter = new BufferedWriter(new FileWriter(dataFile));
			
            Statement st = conn.createStatement();
            System.out.println("removing old entries for project " + project);
			st.execute("delete from revisions where project = '" + project +"' and module = '" + module + "'");
		
			List<Revision> revisions = new ArrayList<Revision>();
			Revision revisionInst = new Revision();
			
			
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line, path;
			String timestampStr;
		
			/* the exact delimiters in cvs log file */
			String fileEntryDelimiter = "=============================================================================";
			String revisionEntryDelimiter = "----------------------------";
			/* the threshold time window */
    		long timeWindow = 1000 * 60 * 15; 
			
			/* for parsing the date from the log files */
			DateFormat CVSDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			DateFormat otherCVSDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			DateFormat SQLDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			
			/* to make the matching a bit more permissive */
			int flags = Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNIX_LINES;
			
			Pattern filenamePattern = Pattern.compile("(working|RCS) file:(.+)", flags);
			Pattern revisionPattern = Pattern.compile("revision (.+)", flags);
			Pattern infoPattern = Pattern.compile("date: ([^;]+);\\s+ author: ([^;]+);\\s+state: ([^;]+);", flags);
			
			boolean done = false;
			int i = 0;
			
			while (!done) {
				line = reader.readLine();
				if (line == null) {
					break;
				}
				
				while (!line.startsWith("Working file:")
					&& !line.startsWith("RCS file:")) {
					line = reader.readLine();
					/* if we get null back then we're at the end of the file, so quit */
					if (line == null) {
						done = true;
						break;
					}
				}
				
				Matcher fm = filenamePattern.matcher(line.trim());
				fm.lookingAt();
				path = fm.group(2).trim();
	
				while (true) {
					revisionInst.filename = formatFile(path);
					while (!line.startsWith(fileEntryDelimiter) && !line.startsWith(revisionEntryDelimiter)) {
						line = reader.readLine();
					}
					/* if we find ======= then we're at another file entry so break out of this while loop */
					if (line.startsWith(fileEntryDelimiter)) {
						break;
					}
					/* if we get here then we're in a revision entry */
					line = reader.readLine();
				
					/* get the revision */
					Matcher rm = revisionPattern.matcher(line);
					rm.lookingAt();
					revisionInst.revision = rm.group(1).trim();
					
					line = reader.readLine();

					/* Set the diffUrl */
					revisionInst.revisionLink = formatRevisionLink(path, revisionInst.revision);
					
					/* get the author and timestamp */
					Matcher im = infoPattern.matcher(line);
					im.lookingAt();
					timestampStr = im.group(1);
					revisionInst.author = im.group(2);
					if (im.group(3).equals("dead")) {
						revisionInst.state = "deleted";
					} else if (revisionInst.revision.equals("1.1")) {
						revisionInst.state = "added";
					} else {
						revisionInst.state = "modified";
					}
					
					try {
						revisionInst.date = CVSDateFormat.parse(timestampStr);
					} catch(Exception ex) {
						revisionInst.date = otherCVSDateFormat.parse(timestampStr);
					}
					revisionInst.originalDateString = timestampStr;
				
					/* get the log message */
					StringBuilder logBuilder = new StringBuilder();
					line = reader.readLine();
					while (!line.startsWith(fileEntryDelimiter) && 
						!(line.startsWith(revisionEntryDelimiter) && line.length() <= 30)) {
						logBuilder.append(line);
						line = reader.readLine();
					}
					revisionInst.logMessage = logBuilder.toString();
					Pattern pattern = Pattern.compile("branches:\\s+[0-9.]+;");
					Matcher matcherLog = pattern.matcher(revisionInst.logMessage);
					revisionInst.logMessage = matcherLog.replaceFirst("");
					// skip non-java files and branches.
					if (revisionInst.filename.endsWith(".java") && 
                   revisionInst.revision.split("[.]").length == 2) {
						revisions.add(revisionInst);
					}
					revisionInst = new Revision();
					
				}
			}
			Collections.sort(revisions);
		
			int transactionId = 1;
			Revision r1, r2;
			int j;
			for (i = 0; i < revisions.size(); i++) {
				r1 = revisions.get(i);
				if (r1.transactionId > 0)
					continue;
				r1.transactionId = transactionId;
				for (j = i+1; j < revisions.size(); j++) {
					r2 = revisions.get(j);
					if (r2.date.getTime() - r1.date.getTime() > timeWindow) {
						break;
					}
					if (!r2.author.equals(r1.author) || !r2.logMessage.equals(r1.logMessage)) {
						continue;
					}
					r2.transactionId = transactionId;
				}
				transactionId++;
			}
			
			Hashtable<Integer, List<Revision>> trans = new Hashtable<Integer, List<Revision>>();
			for (Revision r : revisions) 
			{
				if( !trans.containsKey(r.transactionId) )
					trans.put(r.transactionId, new ArrayList<Revision>());
				trans.get(r.transactionId).add(r);
			}
		
			List<Transaction> transactions = new ArrayList<Transaction>();
			for( int id : trans.keySet())
			{
				Transaction t = new Transaction(trans.get(id));
				transactions.add(t);
			}
			
			Collections.sort(transactions);

			// Partial ordering
			int id = 0;
			for( Transaction t: transactions)
			{
				t.Id = id++;
			}
			
			// Check if any transaction has the same overlap....
			HashSet<Transaction> overlaps = FindOverlaps(transactions);
			int overlapSize = overlaps.size();
			while( overlaps.size()> 0)
			{
				System.out.print("Merging");
				MergeOverlaps(transactions, overlaps);
				overlaps = FindOverlaps(transactions);
			}
			System.out.println("Successfully merged " + overlapSize + " transactions");
			
			// Final ordering
			Collections.sort(transactions);
			id = 0;
			for( Transaction t: transactions)
			{
				t.Id = id++;
			}
			
			System.out.println("Writing " + revisions.size() + " revisions to database.");
			for( Transaction t: transactions)
			{
				for (Revision r : t.Revisions) {
					dataWriter.write(escape(project) + "\t" + escape(module) + "\t" + escape(r.filename) + "\t" + escape(SQLDateFormat.format(r.date)) + "\t" + 
						escape(r.originalDateString) + "\t" +
						escape(r.revision) + "\t" + t.Id + "\t" + escape(r.author) + "\t" + escape(r.logMessage) + "\t" + escape(r.revisionLink) + 
						"\t" + escape(r.state) + "\n");
				}
			}
			
			dataWriter.close();
			String tempPath = dataFile.getAbsolutePath();
			tempPath = tempPath.replace("\\","\\\\");
			st.execute("load data local infile '" + tempPath + 
				"' into table revisions (project, module, filename, datetime,datetimestr, revision, transactionid, userid, logmessage, diffurl, state);\n");
			st.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void MergeOverlaps(List<Transaction> transactions, HashSet<Transaction> overlaps) 
	{
		for( Transaction c : overlaps )
		{
			for( Revision r : c.Revisions)
			{
				Revision o = c.Parent.HasRevisionWithFile(r.filename);
				if( o != null )
				{
					int oVersion = Integer.parseInt(o.revision.split("[.]")[1]);
					int rVersion = Integer.parseInt(r.revision.split("[.]")[1]);
					if( rVersion > oVersion )
					{
						// If somehow how newer, replace.
						c.Parent.Revisions.remove(o);
						c.Parent.Revisions.add(r);
					}
				}
				else
				{
					c.Parent.Revisions.add(r);
				}
			}
			transactions.remove(c);
			Collections.sort( c.Parent.Revisions );
		}
	}
	
	

	private HashSet<Transaction> FindOverlaps(List<Transaction> transactions) throws Exception
	{
		HashSet<Transaction> overlaps = new HashSet<Transaction>();
		for( Transaction t: transactions)
		{
			for( Transaction p: transactions)
			{
				if( t.Id == p.Id )
					continue;
				
				boolean afterStart = t.First().date.compareTo(p.Last().date) < 0;
				boolean overlapsStart = p.Last().date.compareTo(t.Last().date) < 0;
				
				boolean beforeEnd = t.Last().date.compareTo(p.First().date) > 0;
				boolean overlapsEnd = p.First().date.compareTo(t.First().date) > 0;
				if( (beforeEnd  && overlapsEnd) ||
					 afterStart && overlapsStart)
				{
					//     [  t  ]
					//  [ p ]  [ p ]   Overlap cases.
					// [p]         [p]  good cases, no time overlap.
					String info = t.First().date + " to " + t.Last().date + " overlaps with " + 
					              p.First().date + " to " + p.Last().date;
					String nums = t.Id + " " + p.Id;
					//throw new Exception("Detected overlap in transactions ("+nums+"): " + info);
					System.out.println("!!!!Detected overlap in transactions ("+nums+"): " + info);

					if( p.Parent != null )
					{
						// would need to rethink if multiple overlaps are happening.
						//throw new Exception("Overlaps more complicated than planned for...");
					}
					else
					{
						p.Parent = t;
						overlaps.add(p);
					}
				}
			}
		}
		return overlaps;
	}
	
	private String formatFile(String path)
	{
		// Need to remove "CVSROOT" from file path preceding project.
		String[] parts = path.split(Pattern.quote(project + "/" + module));
		String filename = parts[1].replace("/Attic", "");
		return filename.replace(",v","");
	}
	
	private String formatRevisionLink(String path, String version)
	{
		String baseUrl = "http://" + project + ".cvs.sourceforge.net/viewvc/" + project + "/";
		
		if( version.equals("1.1") )
			return "";
		
		String[] v = version.split("[.]");
		if( v.length == 0 || v.length > 2 )
			return "";
		
		int a = Integer.parseInt(v[1]);
		int b = a - 1;
		String fileName = formatFile(path);
		return baseUrl + module + fileName + "?view=diff&r1=" + v[0] + "." + b + "&r2=" + v[0] + "." + a;
	}
	
	public static void main(String args[]) throws Exception {
		
		if (args.length != 4) {
			System.err.println("usage: revisioncvslogparser <project> <module> <cvs log file> <jdbc url>\n\n" + 
					"The first argument is the project name that will be used in the database\n\n" + 
					"The second argument is the module name that will be used in the database\n\n" + 
					"you must specify the cvs log file as the second argument\n" +
					"to get this log execute \"cvs log > project.log\" from the root of the repository\n\n" +
					"and the jdbc url as the third argument.  An example jdbc url is:\n" + 
					"jdbc:mysql://hera.cs.ucdavis.edu/testdb?user=cabird&password=foobar\n" +
					"note that ampersands must by escaped with a slash in bash or you can put the arguments in quotes\n");
			System.exit(1);
		}

		String project = args[0];
		String module = args[1];
		String logFilename = args[2];
		String jdbcUrl = args[3];

		Connection conn = getConnection(jdbcUrl);
		setupRevisionTable(conn);
		
		FastRevisionCVSLogParser logParser = new FastRevisionCVSLogParser();
		logParser.setProject(project, module);
		logParser.parseLog(new File(logFilename), conn);
	}
}
