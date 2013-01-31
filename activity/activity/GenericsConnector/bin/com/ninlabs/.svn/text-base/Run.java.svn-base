package com.ninlabs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Dictionary;
import java.util.Hashtable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Run {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void _main(String[] args) throws ClassNotFoundException, SQLException, IOException 
	{
		//chimp.cs.ubc.ca --port=4747 --user=chris udc -p
		String jdbcUrl = "jdbc:mysql://chimp.cs.ubc.ca:4747/udc_temp";
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(jdbcUrl,"chris","capcom");
        
        
        
        Hashtable<Integer, User> users = new Hashtable<Integer, User>();
        RunQuery(conn, "select * from usersrefactoringperweek", users);
        System.out.println("Got users.");
        RunZeroQuery(conn, "select * from norefactoringweeks", users);
        System.out.println("Got zero rows.");

    	FileWriter writer = new FileWriter("results.txt");
        for( User user : users.values())
        {
        	for( int week : user.countPerWeek.keySet() )
        	{
	        	writer.append(user.id + "," + week +"," + user.countPerWeek.get(week));
	        	writer.append("\n");
        	}
        }
        writer.close();
        
	}
	
	private static class User
	{
		public int id;
		public Hashtable<Integer,Integer> countPerWeek = new Hashtable<Integer,Integer>();
	}

	private static void RunQuery(Connection conn, String sql, Hashtable<Integer, User> users) throws SQLException {
		Statement stmt = conn.createStatement();
        try {
            /* test to see if the revisions table exists */
			if( stmt.execute(sql) )
            {
            	ResultSet results = stmt.getResultSet();
            	while (results.next()) 
            	{
        			int userid = results.getInt("userId");
        			if( !users.containsKey(userid))
        			{
        				User user = new User();
        				user.id = userid;
        				users.put(userid, user);
        			}
        			User user = users.get(userid);
        			int week = results.getInt("weekday");
        			int count = results.getInt("count");
        			
        			user.countPerWeek.put(week, count);        			
            	}
            }
        } catch (SQLException e) {
        	e.printStackTrace();
            System.out.println("couldn't execute query on table revisions... creating it");
        }
	}

	private static void RunZeroQuery(Connection conn, String sql, Hashtable<Integer, User> users) throws SQLException {
		Statement stmt = conn.createStatement();
        try {
            /* test to see if the revisions table exists */
			if( stmt.execute(sql) )
            {
				int zeroRows = 0;
            	ResultSet results = stmt.getResultSet();
            	while (results.next()) 
            	{
        			int userid = results.getInt("userId");
        			if( !users.containsKey(userid))
        			{
        				continue;
        			}
        			User user = users.get(userid);
        			int week = results.getInt("weekday");
        			int count = results.getInt("count");
        			
        			// doesn't have refactoring, insert 0 row.
        			if( !user.countPerWeek.containsKey(week) )
        			{
        				user.countPerWeek.put(week, 0);
        				zeroRows++;
        			}
            	}
            	System.out.println("Zero rows:" + zeroRows);
            }
        } catch (SQLException e) {
        	e.printStackTrace();
            System.out.println("couldn't execute query on table revisions... creating it");
        }
	}

	
}
