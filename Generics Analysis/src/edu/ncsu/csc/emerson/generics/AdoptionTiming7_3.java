package edu.ncsu.csc.emerson.generics;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;


public class AdoptionTiming7_3 
{

	public static class ProjectTiming
	{
		public String project;
		public Date FirstAnnotationDate; 
		public Date FirstGenericDate; 
	}
	
	public void run()
	{
		try
		{
			Connection conn = Analysis.getDatabaseConnection();
			Statement s = conn.createStatement();

			String annotationsQuery = new AnnotationAnalysis(null).query()+", revisions.DateTime";
			String typesQuery = new ParameterizedTypeAnalysis(null).query()+", revisions.DateTime";
			Hashtable<String,ProjectTiming> timing = new Hashtable<String,ProjectTiming>();
			

			// Annotations
			ResultSet set = s.executeQuery(annotationsQuery);
			while( set.next() )
			{
				String project = set.getString("revisions.project");
				Date date = set.getDate("revisions.DateTime");
				
				if( !timing.containsKey(project))
				{
					ProjectTiming p = new ProjectTiming();
					p.project = project;
					timing.put(project, p);
				}
				
				if(  set.getInt("num") > 0 && timing.get(project).FirstAnnotationDate == null )
				{
					timing.get(project).FirstAnnotationDate = date;
				}
			}

			// Generics
			set = s.executeQuery(typesQuery);
			while( set.next() )
			{
				String project = set.getString("revisions.project");
				Date date = set.getDate("revisions.DateTime");
				
				if( !timing.containsKey(project))
				{
					ProjectTiming p = new ProjectTiming();
					p.project = project;
					timing.put(project, p);
				}
				
				if(  set.getInt("count") > 0 && timing.get(project).FirstGenericDate == null )
				{
					timing.get(project).FirstGenericDate = date;
				}
			}

			// project, first generic type date, first annotation type date,  isAnnotationBefore
			for( ProjectTiming p : timing.values())
			{
				System.out.println(p.project + ";" + p.FirstGenericDate + ";" + p.FirstAnnotationDate);
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
	{
		new AdoptionTiming7_3().run();
	}
}
