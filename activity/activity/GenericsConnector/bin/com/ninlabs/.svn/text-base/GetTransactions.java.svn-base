package com.ninlabs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


public class GetTransactions {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException 
	{
		String project = args[0];
		String module = args[1];
		String jdbcUrl = args[2];
		boolean doingGit=false;
		if (args.length > 3 && args[3].equals("--git")) {
			doingGit = true;	
		}
		/*
		String project = "freemind";
		String module = "Freemind";
		String jdbcUrl = "jdbc:mysql://localhost/generics?user=root&password=20pastnoon";
		*/
		
		Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(jdbcUrl);

		Statement stmt = conn.createStatement();

		if( stmt.execute("select filename,transactionId, revision, datetimestr, state from revisions " + 
						 "WHERE project='"+project+"' AND module = '"+ module + "' " + 
						 "ORDER BY transactionID") )
        {
        	ResultSet results = stmt.getResultSet();
        	while (results.next()) 
        	{
    			String files = results.getString("filename");
    			int transId = results.getInt("transactionId");
    			String datetime = results.getString("datetimestr");
    			String state = results.getString("state");
			String revision = results.getString("revision");
			if (doingGit) {
    				System.out.println(transId + ";" + revision + ";" + files + ";" + datetime + ";" + state);
			} else {
    				System.out.println(transId + ";" + files + ";" + datetime + ";" + state);
			}
        	}
        }
	}

}
