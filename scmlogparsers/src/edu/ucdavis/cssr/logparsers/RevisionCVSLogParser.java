package edu.ucdavis.cssr.logparsers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.*;
import java.sql.*;


public class RevisionCVSLogParser extends LogParser{

	private String project;
	private String module;
	public void setProject(String project, String module)  {
		this.project = project;
		this.module = module;
	}

	public void parseLog(File file, Connection conn) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			String filename, revision, author, timestampStr;
			Timestamp timestamp;
		
			/* the exact delimiters in cvs log file */
			String fileEntryDelimiter = "=============================================================================";
			String revisionEntryDelimiter = "----------------------------";
			
			/* for parsing the date from the log files */
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			
			/* to make the matching a bit more permissive */
			int flags = Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNIX_LINES;
			
			Pattern filenamePattern = Pattern.compile("(working|RCS) file:(.+)", flags);
			Pattern revisionPattern = Pattern.compile("revision (.+)", flags);
			Pattern infoPattern = Pattern.compile("date: ([^;]+);\\s+ author: ([^;]+);", flags);
			
			/* delete any leftover from a previous run for ths project */
			Statement statement = conn.createStatement();
			statement.execute("delete from revisions where project = '" + project + "' and module = '" + module + "'");
			
			String insertPart = "insert into revisions (project, filename, datetime, revision, userid, logmessage, module, diffUrl) VALUES ";
			PreparedStatement insertStatement = conn.prepareStatement(insertPart + "(?, ?, ?, ?, ?, ?, ?,?)");
		
			/* this will be the same for all inserts so set it now */
			insertStatement.setString(1, project);
			insertStatement.setString(7, module);					
			
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
				//filename = fm.group(1).trim();
				filename = fm.group(2).trim();
	
				insertStatement.setString(2, formatFile(filename, project, module));
				
				/* we're gonna be all tricky and build a large insert statement with a bunch of
				 * rows to speed things up a bit.  Now, each file with get an insert with
				 * a row per revision.  MySQL limit on an insert size is about 1M, so we should
				 * be pretty safe unless files begin exceeding 1000's of revisions.
				 */
				StringBuilder bigInsert = new StringBuilder(insertPart);
				boolean first = true;
				
				while (true) {
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
					revision = rm.group(1).trim();
					insertStatement.setString(4, revision);
					
					line = reader.readLine();

					/* Set the diffUrl */
					insertStatement.setString(8, formatRevisionLink(project, filename, revision ));
					
					/* get the author and timestamp */
					Matcher im = infoPattern.matcher(line);
					im.lookingAt();
					timestampStr = im.group(1);
					author = im.group(2);
					insertStatement.setString(5, author);
					try
					{
					timestamp = new java.sql.Timestamp(dateFormat.parse(timestampStr).getTime());
					}
					catch(Exception ex)
					{
						// Timezone...
						dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
						timestamp = new java.sql.Timestamp(dateFormat.parse(timestampStr).getTime());	
					}
					insertStatement.setTimestamp(3, timestamp);
				
					/* get the log message */
					StringBuilder logBuilder = new StringBuilder();
					line = reader.readLine();
					while (!line.startsWith(fileEntryDelimiter) && !line.startsWith(revisionEntryDelimiter)) {
						logBuilder.append(line);
						line = reader.readLine();
					}
					insertStatement.setString(6, logBuilder.toString());
				
					/* add it to our big insert statement.
					 * we only want one insert per file, but each insert can have a ton of rows
					 */
					if (!first) {
						bigInsert.append(",");
					}
					first = false;
					bigInsert.append(insertStatement.toString().split("VALUES", 2)[1]);
				
					/* the ==== ends each entry, so this *should* always work...
					 * famous last words
					 */
					if (line.startsWith(fileEntryDelimiter)) {
						//System.out.println(bigInsert.toString());
						
						System.out.println("Inserting statment " + ++i);
						statement.execute(bigInsert.toString());
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private String formatFile(String path, String project, String module)
	{
		// Need to remove "CVSROOT" from file path preceding project.
		String[] parts = path.split(Pattern.quote(project + "/" + module));
		String filename = module + parts[1];
		return filename.replace(",v","");
	}
	
	private String formatRevisionLink(String project, String path, String version)
	{
		String baseUrl = "http://" + project + ".cvs.sourceforge.net/viewvc/" + project + "/";
		
		if( version.equals("1.1") )
			return "";
		
		String[] v = version.split("[.]");
		if( v.length == 0 || v.length > 2 )
			return "";
		
		int a = Integer.parseInt(v[1]);
		int b = a - 1;
		String fileName = formatFile( path, project, module);
		return baseUrl + fileName + "?view=diff&r1=" + v[0] + "." + b + "&r2=" + v[0] + "." + a;
	}
	
    private void insertTransaction(Connection conn, int transaction, List<Integer> fileids) throws SQLException {
    	System.out.println("Transaction " + transaction + " has " + fileids.size() + " files in it");
    	StringBuilder sb = new StringBuilder("update revisions set transactionid = " + transaction + " where fileid in (");
    	boolean first = true;
    	for (Integer fileid : fileids) {
    		if (first) {
    			first = false;
    		} else {
    			sb.append(",");
    		}
    		sb.append(fileid);
    	}
    	sb.append(")");
    	System.out.println(sb.toString());
    	Statement st = conn.createStatement();
    	st.execute(sb.toString());
    	st.close();
    	
    }
    
    public void setTransactionIDs(Connection conn) {
    	try {
    		String sql = "select userid, datetime, fileID from revisions where project = '" 
    			+ project + "' and module = '" + module + "' order by userid, datetime asc";
    		Statement st = conn.createStatement();
    		// define the time window in millis.  For now, we use a sliding window of one hour.
    		long timeWindow = 1000 * 60 * 60; 
    		
    		st.execute(sql);
    		ResultSet rs = st.getResultSet();
    		
    		String lastUser = null;
    		String currentUser;
    		Date lastCommitDate = new Date(0);
    		Date currentCommitDate;
    		int transaction = 1;
    		List<Integer> transactionFileIds = new LinkedList<Integer>();
    		while (rs.next()) {
    			currentUser = rs.getString(1);
    			currentCommitDate = rs.getDate(2);
    		
    			/* if there is at least one file in the current transaction
    			 * and we see a new user or we see a committed file that is outside
    			 * the time window (of one hour), then record the transaction and begin
    			 * anew
    			 */
    			if (transactionFileIds.size() > 0 &&
    					(!currentUser.equals(lastUser) ||
    					currentCommitDate.getTime() - lastCommitDate.getTime() > timeWindow)) {
    				insertTransaction(conn, transaction++, transactionFileIds);
    				transactionFileIds = new LinkedList<Integer>();
    			} 
    			transactionFileIds.add(rs.getInt(3));
    			lastUser = currentUser;
    			lastCommitDate = currentCommitDate;
    		}
    		if (transactionFileIds.size() > 0) {
   				insertTransaction(conn, transaction++, transactionFileIds);
    		}
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
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
		
		RevisionCVSLogParser logParser = new RevisionCVSLogParser();
		logParser.setProject(project, module);
		logParser.parseLog(new File(logFilename), conn);
		logParser.setTransactionIDs(conn);
	}
}
