package edu.ncsu.csc.emerson.generics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.bind.DatatypeConverter;

public class TestFile 
{
	public static Connection getDatabaseConnection() throws SQLException {

		String url = "jdbc:mysql://<database>:4747/generics?netTimeoutForStreamingResults=200000";
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return DriverManager.getConnection(url, "***", "***");
	}

	public void DoQuery()
	{
		try 
		{
			Connection conn = getDatabaseConnection();
			Statement s = conn.createStatement();
	
			ResultSet set = s.executeQuery("SELECT project,baseEncodedAttachment FROM table");
			while( set.next() )
			{
				String project = set.getString("project");
				String module = set.getString("baseEncodedAttachment");
				
				// See http://stackoverflow.com/questions/917604/decode-base64-string-java-5
			}
	
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
}
