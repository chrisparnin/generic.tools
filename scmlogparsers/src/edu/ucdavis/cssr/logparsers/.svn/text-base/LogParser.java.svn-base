package edu.ucdavis.cssr.logparsers;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

import java.sql.Statement;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class LogParser {

    final static String createRevisionsTableSql = "CREATE TABLE revisions ( " +
        "FileID          int             NOT NULL AUTO_INCREMENT, " +
        "Project       varchar(100)    NOT NULL," +
        "module          varchar(100)," + // for now, we'll actually allow this to be null
        "DateTime        datetime   NOT NULL, " +
        "userID          varchar(100)    NOT NULL, " +
        "FileName        varchar(300)    NOT NULL, " +
        "Revision        varchar(50)     NOT NULL, " +
        "TransactionID   int             NOT NULL default 0, " +
        "sourcefile      boolean         NOT NULL default false, " +
        "logmessage      text, " +
        "PRIMARY KEY  (FileID) " +
        ");";
	
	protected String getText(Element node, String tag) {
		//System.out.println("tag: " + tag);
		assert(node != null);
		NodeList nodeList = node.getElementsByTagName(tag);
		if (nodeList.getLength() == 0) {
			return null;
		}
		Element valueNode = (Element)nodeList.item(0);
		//System.out.println(valueNode);
		if (valueNode.hasChildNodes()) {
			return valueNode.getFirstChild().getTextContent();
		}
		return null;
	}
	
    public static void setupRevisionTable(Connection conn) throws Exception {
        Statement stmt = conn.createStatement();
        try {
            /* test to see if the revisions table exists */
            stmt.execute("select count(*) from revisions");
        } catch (SQLException e) {
        	e.printStackTrace();
            System.out.println("couldn't execute query on table revisions... creating it");
            stmt.execute(createRevisionsTableSql);
        }
	}
	
	/* create a connection based on a url that is passed in on the command line
	 *  jdbc:postgresql://localhost/test?user=cabird&passwd=foobar
	 */
	public static Connection getConnection(String jdbcUrl) throws Exception{
        System.out.print("connecting...");
        System.out.flush();

        if (jdbcUrl.contains("postgresql")) {
            Class.forName("org.postgresql.Driver");
        } else if (jdbcUrl.contains("mysql")) {
            Class.forName("com.mysql.jdbc.Driver");
        }

        Connection conn = DriverManager.getConnection(jdbcUrl);        

        System.out.println("done.");
        return conn;
	}
	
	public abstract void parseLog(File file, Connection conn);
	
}
