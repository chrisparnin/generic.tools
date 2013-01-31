package edu.ucdavis.cssr.logparsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class RevisionSVNLogParser extends LogParser {

    String project;
    String module;

    public RevisionSVNLogParser(String project, String module) {
        super();
        this.project = project;
        this.module = module;
    }
    
    public String escape(String s) {
    	return s.replaceAll("\t", "\\t").replace("\n", "\\n");
    }
    
    public void parseLog(File file, Connection conn) {
		try {
			
			File temp = File.createTempFile(file.getName(), ".data");
			//temp.deleteOnExit();
			BufferedWriter dataWriter = new BufferedWriter(new FileWriter(temp));
            			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			
			/* for parsing the date from the log files */
			DateFormat SVNDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			DateFormat SQLDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
            //System.out.println("removing old entries for project " + project);
            System.out.println("removing old entries for project " + project + " and module " + module);
            Statement deleteStatement = conn.createStatement();
            deleteStatement.execute("delete from revisions where project = '" + project + "' and module = '" + module + "'");
			
			NodeList nodeList = doc.getElementsByTagName("logentry");
			StringBuilder bigQuery;
			
			for (int i=0; i < nodeList.getLength(); i++) {
				Element entryNode = (Element)nodeList.item(i);
				String revision = entryNode.getAttribute("revision");
				System.out.println("inserting revision " + revision);

				String author = getText(entryNode, "author");
				if (author == null) {
					author = "NONE";
				}
				
				String msg = getText(entryNode, "msg");
				
				/* the format has a . separating the time from the milliseconds... ditch that */
				String dateStr = getText(entryNode, "date");//.split(".")[0];
				System.out.println("date: " + dateStr);
				java.util.Date date = SVNDateFormat.parse(dateStr);
				
				
				/* update this for all files, but skip over deletes (where action = "D") */
				NodeList fileList = entryNode.getElementsByTagName("path");
				int numFiles = fileList.getLength();
				for (int j = 0; j < numFiles; j++) {
					Node fileNode = fileList.item(j);
					
					// Chris Parnin: We would still like the record.
					/* if action is D, then the file was deleted, so skip over it because we don't care */
					//if (fileNode.getAttributes().getNamedItem("action").getTextContent().equals("D")) {
					//	continue;
					//}
					
					/* sometimes there are entries for copying a file from this module of the repo
					 * to another module.  We don't want to capture those.  They will be in the other
					 * repo when we mine it.
					 */
					if (fileNode.getAttributes().getNamedItem("copyfrom-path") != null &&
							!fileNode.getTextContent().trim().startsWith("/" + module)) {
						continue;
					}
					if (msg == null) {
						msg = "";
					}
					String path = fileNode.getTextContent().trim();
					String parts[] = path.split("/", 3);
				
					if( parts[0].equals("tags") || parts[0].startsWith("branch"))
					{
						continue;
					}
					//if (parts.length < 3) {
					//	continue;
					//}

					String diffUrl = formatRevisionLink(project, path, revision);
					String action = fileNode.getAttributes().getNamedItem("action").getTextContent();
					String state = "";
					if( action.equals("A")) { state = "added"; }
					else if( action.equals("M")) { state = "modified"; }
					else if( action.equals("D")) { state = "deleted"; }
					else
					{
						// skip entry.
						continue;
					}
					//module = parts[1];
					//path = "/" + parts[2];
					if (path.endsWith(".java")) {
						dataWriter.write(escape(project) + "\t" + escape(module) + "\t" + escape(path) + "\t" + escape(SQLDateFormat.format(date)) + "\t" + 
								escape(dateStr) + "\t" +
								escape(revision) + "\t" + escape(revision) + "\t" + escape(author) + "\t" + escape(msg) + "\t" + escape(diffUrl) + "\t" + escape(state) + "\n");
					}
					
				}
				
			}
			dataWriter.close();
			
			String tempPath = temp.getAbsolutePath();
			tempPath = tempPath.replace("\\","\\\\");
			
			Statement st = conn.createStatement();
			st.execute("load data local infile '" + tempPath + "' into table revisions (project, module, filename, datetime, datetimestr, revision, transactionid, userid, logmessage,diffurl,state)");
			st.close();
			
			System.out.println("Inserted data from file into db: " + tempPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private String formatRevisionLink(String project, String path, String version)
	{
		String baseUrl = "http://" + project + ".svn.sourceforge.net/viewvc/" + project;
		
		if( version.equals("1") )
			return "";
		
		int a = Integer.parseInt(version);
		int b = a - 1;
		String fileName = path;
		if( !fileName.startsWith("/"))
			baseUrl = "/" + baseUrl;
		return baseUrl + fileName + "?view=diff&r1=" + b + "&r2=" + a;
	}
   
	public static void main(String args[]) throws Exception {
		if (args.length != 4) {
			System.err.println("usage: revisionsvnlogparser <project> <module> <svn xml log file> <jdbc url>\n\n" + 
					"The first argument is the project name that will be used in the database\n\n" + 
					"The second argument is the module within the project that will be used in the database\n\n" +
					"you must specify the svn xml log file as the second argument\n" +
					"to get this log execute \"svn log --xml -v > project.xml.log\" from the root of the repository\n\n" +
					"and the jdbc url as the third argument.  An example jdbc url is:\n" + 
					"jdbc:mysql://hera.cs.ucdavis.edu/testdb?user=cabird&password=foobar\n" +
					"note that ampersands must by escaped with a slash in bash or you can put the arguments in quotes");
			System.exit(1);
		}
        String project = args[0];
        String module = args[1];
		String logFilename = args[2];
		String jdbcUrl = args[3];

		Connection conn = getConnection(jdbcUrl);
		setupRevisionTable(conn);
		
		RevisionSVNLogParser logParser = new RevisionSVNLogParser(project, module);
		logParser.parseLog(new File(logFilename), conn);
	}
	
}
