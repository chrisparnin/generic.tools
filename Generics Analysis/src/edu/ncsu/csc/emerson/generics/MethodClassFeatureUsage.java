package edu.ncsu.csc.emerson.generics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;

public class MethodClassFeatureUsage 
{
	protected String tableName()
	{
		return "parameterized_declarations_changed";
	}
	
	public static class ProjectInfo
	{
		public String project;
		public HashSet<String> Classes = new HashSet<String>();
		public HashSet<String> Methods = new HashSet<String>();
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
				String module = set.getString("module");
				String filename = set.getString("filename");
				String kind = set.getString("kind");
				String classType = set.getString("class_type");
				String typeArgs = set.getString("type_args");

				String key = module + "." + filename + "." + classType + "." + typeArgs;

				if( !projectInfo.containsKey(project) )
				{
					projectInfo.put(project, new ProjectInfo());
					projectInfo.get(project).project = project;
				}
				if( kind == null )
					continue;

				if( kind.equals("method") )
				{
					projectInfo.get(project).Methods.add(key);
				}
				if( kind.equals("class") )
				{
					projectInfo.get(project).Classes.add(key);
				}
			}

			for( ProjectInfo info : projectInfo.values())
			{
				System.out.println(info.project + ";" + info.Classes.size() + ";" + info.Methods.size());
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
			new MethodClassFeatureUsage ().run();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	protected String totalQuery() {
		return "SELECT project,module,kind,filename,class_type,type_args " +
			   "FROM parameterized_declarations_changed ";
		// NOTE container_granularity is NULL at the moment for this table.
	}
}
