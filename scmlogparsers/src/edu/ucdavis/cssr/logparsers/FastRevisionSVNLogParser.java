package edu.ucdavis.cssr.logparsers;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.text.*;
import java.sql.Connection;


public class FastRevisionSVNLogParser extends LogParser {

    String project;
    String module;

    public FastRevisionSVNLogParser(String project) {
        super();
        this.project = project;
    }
	
    public void parseLog(File file, Connection conn) {
    
    }
    
    public String escape(String s) {
    	return s.replaceAll("\t", "\\t").replace("\n", "\\n");
    }
    
    public void parseLog(File file, String outputBase) {
		try {
			
			BufferedWriter sqlWriter = new BufferedWriter(new FileWriter(outputBase + ".sql"));
			BufferedWriter dataWriter = new BufferedWriter(new FileWriter(outputBase + ".data"));
            
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			
			/* for parsing the date from the log files */
			DateFormat SVNDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			DateFormat SQLDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
            System.out.println("removing old entries for project " + project);
			sqlWriter.write("delete from revisions where project = '" + project +"';\n");
			sqlWriter.write("load data local infile '" + outputBase + 
				".data' into table revisions (project, module, filename, datetime,datetimestr, revision, transactionid, userid, logmessage);\n");
			sqlWriter.close();
			
			
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
				Date date = SVNDateFormat.parse(dateStr);
				
				
				/* update this for all files, but skip over deletes (where action = "D") */
				NodeList fileList = entryNode.getElementsByTagName("path");
				int numFiles = fileList.getLength();
				for (int j = 0; j < numFiles; j++) {
					Node fileNode = fileList.item(j);
					/* if action is D, then the file was deleted, so skip over it because we don't care */
					if (fileNode.getAttributes().getNamedItem("action").getTextContent().equals("D")) {
						continue;
					}
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
				
					if (parts.length < 3) {
						continue;
					}
					//System.out.println("PATH:" + path);
					//System.out.println("1" + parts[1]);
					//System.out.println("2" + parts[2]);

					module = parts[1];
					path = "/" + parts[2];
					if (path.endsWith(".java")) {
						dataWriter.write(escape(project) + "\t" + escape(module) + "\t" + escape(path) + "\t" + escape(SQLDateFormat.format(date)) + "\t" + 
								escape(SQLDateFormat.format(date)) + "\t" + 
								escape(revision) + "\t" + escape(revision) + "\t" + escape(author) + "\t" + escape(msg) + "\n");
					}
					
				}
				
			}
			dataWriter.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
   

    
	public static void main(String args[]) throws Exception {
		if (args.length != 3) {
			System.err.println("usage: revisionsvnlogparser <project> <module> <svn xml log file> <outputfilebase>\n\n" + 
					"The first argument is the project name that will be used in the database\n\n" + 
					"The second argument is the module within the project that will be used in the database\n\n" +
					"you must specify the svn xml log file as the third argument\n" +
					"to get this log execute \"svn log --xml -v > project.xml.log\" from the root of the repository\n\n" +
					"The fourth argument is the base of the output file.  <base>.sql and <base>.data will be produced as output\n" +
					"by the program to be used by mysql");
			System.exit(1);
		}
        String project = args[0];
		String logFilename = args[1];
		String outputBase = args[2];

		
		FastRevisionSVNLogParser logParser = new FastRevisionSVNLogParser(project);
		logParser.parseLog(new File(logFilename), outputBase);
	}
	
}
