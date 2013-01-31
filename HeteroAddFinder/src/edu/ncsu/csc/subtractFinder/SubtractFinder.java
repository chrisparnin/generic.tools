package edu.ncsu.csc.subtractFinder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.jface.dialogs.InputDialog;

public class SubtractFinder {

	public static void main(String args[]) {
		try {
			ArrayList<String> subtracts = new ArrayList<String>();
			
			Connection conn = getDatabaseConnection();
			
			// Get all projects
			PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT project FROM generics.parameterized_declarations;");
			ResultSet rs = stmt.executeQuery();
			ArrayList<String> projects = new ArrayList<String>();
			//System.out.println("Projects:");
			while (rs.next()) {
				projects.add(rs.getString(rs.findColumn("project")));
				//System.out.println(projects.get(projects.size()-1));
			}
			//System.out.println();
			
			// Find subtracts for each project
			//System.out.println("Revisions: ");
			String count = "", previous = "";
			for (int i=0; i<projects.size(); i++) {
				stmt = conn.prepareStatement("SELECT DISTINCT revision FROM generics.parameterized_declarations WHERE project = ? ORDER BY revision;");
				stmt.setString(1, projects.get(i));
				ResultSet rsRevisions = stmt.executeQuery();
				count = ""; previous = "";
				ArrayList<Integer> revisions = new ArrayList<Integer>();
				while (rsRevisions.next()) {
					try {
						Integer r = rsRevisions.getInt(1);
						revisions.add(r);
						//System.out.println("Revision "+r+" of project "+projects.get(i));
					} catch (SQLException e) {
						System.out.println("Invalid revision number: "+projects.get(i)+": "+rsRevisions.getString(1));
					}
				}
				Collections.sort(revisions);

				for (Integer rev : revisions) {
					stmt = conn.prepareStatement("SELECT COUNT(*) FROM generics.parameterized_declarations WHERE project = ? AND revision = ?");
					stmt.setString(1, projects.get(i));
					stmt.setInt(2, rev);
					ResultSet rsCount = stmt.executeQuery();
					if (rsCount.next()) {
						previous = count;
						count = rsCount.getString(1);
						//System.out.println("Number of adds for revision "+rev+": "+count);
						if (count.compareTo(previous) < 0) {
							subtracts.add(projects.get(i)+": Revision #"+rev);
						}
					}
				}
			}
			//System.out.println();

			// Print out all subtracts
			System.out.println("Subtracts:");
			for (int i=0; i<subtracts.size(); i++) {
				System.out.println(subtracts.get(i));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
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
