package edu.ucdavis.cssr.logparsers;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.text.*;
import java.sql.Connection;
import java.sql.Statement;


public class GitLogParser extends LogParser {

    String project;
    String module;

    public GitLogParser(String project) {
        super();
        this.project = project;
        this.module = "";
    }
    
    public void getLog() {

    }
	
    public String escape(String s) {
    	return s.replaceAll("\t", "\\t").replace("\n", "\\n");
    }
	
    String ensureAndGetField(String field, String line) {
		if (!line.startsWith(field + ":")) {
			throw new RuntimeException("expected \"" + field + "\" in line\n" + line);
		}
		return line.replace(field + ":", "").trim();
	}
    
    void ensureStartsWith(String field, String line) {
		if (!line.startsWith(field)) {
			throw new RuntimeException("expected \"" + field + "\" in line\n" + line);
		}
    }
    
    public void parseLog(File file, Connection conn) {
		try {
			File temp = File.createTempFile(file.getName(), ".data");
			BufferedWriter dataWriter = new BufferedWriter(new FileWriter(temp));
            BufferedReader logReader = new BufferedReader(new FileReader(file));
			/* for parsing the date from the log files */
			DateFormat gitDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat SQLDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
            System.out.println("removing old entries for project " + project);
            Statement deleteStatement = conn.createStatement();
            deleteStatement.execute("delete from revisions where project = '" + project + "' and module = '" + module + "'");
		
			StringBuilder sb;
			String line;
			String revision, committer, parent, msg, status, path, diffUrl;
			Date dateTime;
			String[] parts;
			int transaction = 1;

			line = logReader.readLine();
			while (true) {
				ensureStartsWith("__START_GIT_COMMIT_LOG_MSG__", line);
				line = logReader.readLine();
				revision = ensureAndGetField("revision", line);
				/* this is ASF specific... may want to change later */
				diffUrl  = "http://github.com/apache/" + project + "/commit/" + revision;
				line = logReader.readLine();
				committer = ensureAndGetField("committer", line);
				line = logReader.readLine();
				dateTime = gitDateFormat.parse(ensureAndGetField("date", line.split("\\+")[0]));
				line = logReader.readLine();
				parent = ensureAndGetField("parent", line);
				/* get the log message */
				sb = new StringBuilder();
				while (true) {
					line = logReader.readLine();
					if (line.startsWith("__END_GIT_COMMIT_LOG_MSG__")) {
						break;
					}
					sb.append(line + "\n");
				}
				msg = sb.toString();
				/* some commit messages don't actually change any files or 
				 * only change files on branches. Right now we're
				 * not recording those, so only increment transaction then
				 * something happens on the trunk.  We use this
				 * boolean to indicate this.
				 */
				boolean validTransaction = false;
				/* get the changed files */
				while (true) {
					line = logReader.readLine();
					if (line == null || line.startsWith("__START_GIT_COMMIT_LOG_MSG__")) {
						break;
					}
					if (line.trim().length() == 0) {
						continue;
					}
					parts = line.split("\t");
					status = parts[0];
					if (status.equals("M")) {
							status = "modified";
					} else if (status.equals("A")) {
						status = "added";
					} else if (status.equals("D")) {
						status = "deleted";
						/* for now, skip renames and copies */
					} else if (status.startsWith("R") || status.startsWith("C")) {
						continue;
					} else {
						System.err.println(line);
						throw new RuntimeException("Invalid Status: " + status);
					}
					path = parts[1];

					if (path.endsWith(".java") && (msg.contains("trunk@") || !msg.contains("svn.apache.org"))) {
						dataWriter.write(escape(project) + "\t" + escape(module) + "\t" + escape(path) + "\t" +  
								escape(SQLDateFormat.format(dateTime)) + "\t" + 
								escape(SQLDateFormat.format(dateTime)) + "\t" + 
								escape(revision) + "\t" + transaction + "\t" + escape(committer) + "\t" + 
								escape(msg) + "\t" + escape(diffUrl) + "\t" + escape(status) + "\n");
						validTransaction = true;
					}
					
				}
				if (line == null) {
					break;
				}
				if (validTransaction) {
					transaction++;
				}
			}
			dataWriter.close();
			String tempPath = temp.getAbsolutePath();
			tempPath = tempPath.replace("\\","\\\\");
			
			Statement st = conn.createStatement();
			st.execute("load data local infile '" + tempPath + "' into table revisions (project, module, filename, datetime, datetimestr, revision, transactionid, userid, logmessage,diffurl,state)");
			st.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
	public static void main(String args[]) throws Exception {
    	String logCmd = "git log --reverse --full-history --all --date=iso --name-status -M -C --pretty=format:\"" +
    			"__START_GIT_COMMIT_LOG_MSG__%nrevision: %H%ncommitter: %cn%ndate: %ci%nparent: %P%n%s%n%b%n__END_GIT_COMMIT_LOG_MSG__\"";
		if (args.length != 3) {
			System.err.println("usage: gitlogparser <project> <git log file> <outputfilebase>\n\n" + 
				"The first argument is the project name that will be used in the database\n\n" + 
				"you must specify the git log file as the second argument\n" +
				"to get this log execute the following command from the root of the repository\n" + logCmd + "\n" +
				"and the jdbc url as the third argument.  An example jdbc url is:\n" + 
				"jdbc:mysql://hera.cs.ucdavis.edu/testdb?user=cabird&password=foobar\n" +
				"note that ampersands must by escaped with a slash in bash or you can put the arguments in quotes");
			System.exit(1);
		}
        String project = args[0];
		String logFilename = args[1];
		String jdbcUrl = args[2];

		Connection conn = getConnection(jdbcUrl);
		setupRevisionTable(conn);

		
		GitLogParser logParser = new GitLogParser(project);
		logParser.parseLog(new File(logFilename), conn);
	}
	
}
