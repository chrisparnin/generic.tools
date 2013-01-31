package edu.ncsu.csc.emerson.generics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

public class GeneralFeatureUsage 
{
	protected String tableName()
	{
		return "parameterized_types_changed";
	}
	
	public static class ProjectInfo
	{
		public String project;
		public int NumNested;
		public int NumBoundedTypes;
		public int NumWildcards;
		public int NumMultiple;
		public int Total;
	}
	
	public void run()
	{
		try 
		{
			Connection conn = Analysis.getDatabaseConnection();
			Statement s = conn.createStatement();

			Hashtable<String,ProjectInfo> projectInfo = new Hashtable<String,ProjectInfo>();

			ResultSet set = s.executeQuery(totalQuery());
			while( set.next() )
			{
				String project = set.getString("project");
				int num = set.getInt("num");
				if( !projectInfo.containsKey(project) )
				{
					projectInfo.put(project, new ProjectInfo());
					projectInfo.get(project).project = project;
					projectInfo.get(project).Total = num;
				}
			}

			// Multiple
			set = s.executeQuery(query("%,%"));
			while( set.next() )
			{
				String project = set.getString("project");
				int num = set.getInt("num");
				projectInfo.get(project).NumMultiple = num;
			}
			
			// Nested
			set = s.executeQuery(query("%<%<%"));
			while( set.next() )
			{
				String project = set.getString("project");
				int num = set.getInt("num");
				projectInfo.get(project).NumNested = num;
			}
			
			// Wildcards
			set = s.executeQuery(query("%?%"));
			while( set.next() )
			{
				String project = set.getString("project");
				int num = set.getInt("num");
				projectInfo.get(project).NumWildcards = num;
			}
			
			// Bounded
			set = s.executeQuery(query("%extends%"));
			while( set.next() )
			{
				String project = set.getString("project");
				int num = set.getInt("num");
				projectInfo.get(project).NumBoundedTypes = num;
			}

			for( ProjectInfo project : projectInfo.values() )
			{
				System.out.println(project.project + ";" + project.NumMultiple + ";" + project.NumWildcards + ";" + project.NumBoundedTypes + ";" + project.NumNested + ";" + project.Total);
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {

		try
		{
			//FileWriter writer = new FileWriter("output.sql");
			//BufferedWriter out = new BufferedWriter(writer);
			new GeneralFeatureUsage ().run();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	protected String totalQuery() {
		return "SELECT project,count(*) as num " +
			   "FROM parameterized_types_changed " +
			   "GROUP BY project";
	}
	
	public String query(String likePattern) {
		return "SELECT project,count(*) as num " +
			   "FROM parameterized_types_changed " +
			   "WHERE type_args like '"+likePattern+"' " + 
			   "GROUP BY project";
	}

}
