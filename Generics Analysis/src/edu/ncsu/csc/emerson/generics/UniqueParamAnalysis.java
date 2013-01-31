package edu.ncsu.csc.emerson.generics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;

public class UniqueParamAnalysis 
{
	public static class ProjectInfo
	{
		public String project;
		public Hashtable<String,Integer> Params = new Hashtable<String,Integer>();
	}
	
	public void run()
	{
		try 
		{
			Connection conn = Analysis.getDatabaseConnection();
			Statement s = conn.createStatement();

			Hashtable<String,ProjectInfo> projectInfo = new Hashtable<String,ProjectInfo>();

			ResultSet set = s.executeQuery(query());
			while( set.next() )
			{
				String project = set.getString("proj");
				String classType = set.getString("class");

				if( !projectInfo.containsKey(project) )
				{
					projectInfo.put(project, new ProjectInfo());
					projectInfo.get(project).project = project;
				}

				projectInfo.get(project).Params.put(classType,set.getInt("count"));
			}

			for( ProjectInfo info : projectInfo.values())
			{
				for( String klass : info.Params.keySet() )
				{
					System.out.println(info.project + ";" + klass + ";" + info.Params.get(klass));
				}
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
			new UniqueParamAnalysis ().run();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	protected String query() 
	{
		return
		"select proj, class, count(*) as count from " +
		"(" +
		"select t.project as proj, t.class_type as class, t.type_args, count(*) " +
		"from parameterized_types as t, parameterized_declarations as d," +
		"(" +
		"select distinct t2.class_type " +
		"from parameterized_types as t2, parameterized_declarations as d2 " +
		"where t2.project = d2.project and t2.class_type = d2.class_type and " +
        "t2.class_type <> 'List' and t2.class_type <> 'ArrayList' " +
        "and !(length(t2.type_args) = 1 or t2.type_args REGEXP '^.,.$' or " +
        "t2.type_args REGEXP '^. (super|extends) .(,. (super|extends) .)*$') " +
        ") as valid_classes " +
        "where t.project = d.project and t.class_type = d.class_type and " +
        "t.class_type = valid_classes.class_type " +
        "group by proj, class, type_args) as foo group by proj, class";
	}
}
