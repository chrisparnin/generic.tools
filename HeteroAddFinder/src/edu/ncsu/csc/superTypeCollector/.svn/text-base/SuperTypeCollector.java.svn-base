package edu.ncsu.csc.superTypeCollector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.jface.dialogs.InputDialog;

public class SuperTypeCollector {

	/**
	 * SuperTypeCollector is an analysis tool that queries the SQL database for Super Types
	 * and displays a calculated result of a single object's Super Type and its associated collection's Super Type
	 * @param args
	 * @author Bradley Herrin
	 */
	public static void main(String[] args) {
		
		try {
           
			Connection conn = getDatabaseConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs;
 
            rs = stmt.executeQuery("SELECT * FROM heteroaddfinderresults WHERE DeclType NOT LIKE '%<%'");
            
            while ( rs.next() ) {
                String projectName = rs.getString("ProjectName");
                String declType = rs.getString("DeclType");
                System.out.print(projectName);
                System.out.print("  |*|  ");
                System.out.print(declType);
                System.out.print("  |*|  ");
                System.out.print("SuperType will be listed here");
                System.out.print("  |*|  " + "\n");
              
            }
            conn.close();
        } catch (Exception e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
    }
	
	/**
	 * Prompts user for database qualifications and returns connection if valid
	 * @return
	 * @throws SQLException
	 */
	public static Connection getDatabaseConnection() throws SQLException {
		String dbUsername, dbPassword;
		
		InputDialog dbUser = new InputDialog(null, "DB Credential Check", "Enter your MySQL User Name:", "USERNAME", null);
		dbUser.open(); 
		dbUsername = dbUser.getValue();
		
		InputDialog dbPass = new InputDialog(null, "DB Credential Check", "Enter your MySQL Password:", "PASSWORD", null);
		dbPass.open(); 
		dbPassword = dbPass.getValue();

		String url = "jdbc:mysql://eb2-2291-fas01.csc.ncsu.edu:4747/generics";

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return DriverManager.getConnection(url, dbUsername, dbPassword);
	}

}
