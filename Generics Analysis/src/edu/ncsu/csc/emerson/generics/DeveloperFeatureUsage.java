package edu.ncsu.csc.emerson.generics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

public class DeveloperFeatureUsage 
{
	protected String paramDecs()
	{
		return "parameterized_declarations_changed";
	}
	
	protected String paramTypes()
	{
		return "parameterized_types_changed";
	}
	
	public String fields()
	{
		return paramDecs()+ ".kind, COUNT("+paramDecs()+ ".class_type) as num";
	}
	
	public String fieldsTypes()
	{
		return paramTypes()+ ".class_type, COUNT("+paramTypes()+ ".class_type) as num";
	}
	
	public static class DeveloperData
	{
		public String userId;
		public String project;
		public int numClasses;
		public int numMethods;
		public int numCommits;
		public int numTypeParams;
		public int numAnnotations;
	}

	public void run()
	{
		try 
		{
			Connection conn = Analysis.getDatabaseConnection();
			Statement s = conn.createStatement();

			Hashtable<String,DeveloperData> devInfo = new Hashtable<String,DeveloperData>();
						
			// For declarations
			ResultSet set = s.executeQuery(query());
			while( set.next() )
			{
				String project = set.getString("revisions.project");
				String userId = set.getString("revisions.userId");
				String kind = set.getString(paramDecs()+".kind");
				String num = set.getString("num");
				String key = project+"^_^"+userId;
				if( !devInfo.containsKey(key) )
				{
					DeveloperData data = new DeveloperData();
					data.project = project; data.userId = userId;
					devInfo.put(key, data);
				}
				if( kind == null )
					continue;
				if( kind.equals("class"))
				{
					devInfo.get(key).numClasses = Integer.parseInt(num);
				}
				if (kind.equals("method"))
				{
					devInfo.get(key).numMethods = Integer.parseInt(num);
				}
			}
			
			// For type parameters
			set = s.executeQuery(queryTypes());
			while( set.next() )
			{
				String project = set.getString("revisions.project");
				String userId = set.getString("revisions.userId");
				String key = project+"^_^"+userId;
				//System.out.println(key);
				if( !devInfo.containsKey(key) )
				{
					DeveloperData data = new DeveloperData();
					data.project = project; data.userId = userId;
					devInfo.put(key, data);
				}
				devInfo.get(key).numTypeParams = set.getInt("num");
			}
			
			// For user's commits:
			set = s.executeQuery("select project,userId, count(*) as num from revisions group by project, userId");
			while( set.next() )
			{
				String project = set.getString("revisions.project");
				String userId = set.getString("revisions.userId");
				int num = set.getInt("num");
				String key = project+"^_^"+userId;
				
				devInfo.get(key).numCommits = num;
			}
			
			// For annotations
			Hashtable<String,Integer> devAnnotations = CalculateAnnotationsDeltas(s);
			for( String key : devAnnotations.keySet() )
			{
				if( !devInfo.containsKey(key) )
				{
					DeveloperData data = new DeveloperData();
					data.project = key.split("\\^_\\^")[0]; data.userId = key.split("\\^_\\^")[1];
					devInfo.put(key, data);
				}
				devInfo.get(key).numAnnotations = devAnnotations.get(key);

			}
			
			for( DeveloperData data : devInfo.values() )
			{
				System.out.println(data.project + ";" + data.userId + ";" + data.numMethods + ";" + data.numClasses + ";" + data.numTypeParams + ";" + data.numAnnotations + ";"+ data.numCommits);
			}
			System.out.println("_____________________");
			for( DeveloperData data : devInfo.values() )
			{
				if( data.numCommits >= 100 )
				{
					System.out.println(data.project + ";" + data.userId + ";" + data.numMethods + ";" + data.numClasses + ";" + data.numTypeParams + ";" + data.numAnnotations + ";" + data.numCommits);
				}
			}
			
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	// Because no "annotations_changed" table exists, we have to manually calculate them here:
	public Hashtable<String,Integer> CalculateAnnotationsDeltas(Statement s)
	{
		Hashtable<String,Integer> developerCount = new Hashtable<String,Integer>();
		try 
		{
			ResultSet set = s.executeQuery(annotationsQuery());
			Hashtable<String,Integer> fileCount = new Hashtable<String,Integer>();


			while( set.next() )
			{
				String project = set.getString("revisions.project");
				String module = set.getString("revisions.module");
				String file = set.getString("revisions.filename");
				String userId = set.getString("revisions.userId");
				String state = set.getString("revisions.state");
				
				String key = project + "." + module + "." + file;
				
				int last = 0;
				if( fileCount.containsKey(key) )
				{
					last = fileCount.get(key);
				}
				int delta =  set.getInt("num") - last;
				if( state.equals("deleted"))
				{
					delta = -set.getInt("num");
				}
				
				int lastDev = 0;
				if( developerCount.containsKey(project+"^_^"+userId))
				{
					lastDev = developerCount.get(project+"^_^"+userId);
				}
				developerCount.put(project+"^_^"+userId, lastDev + delta);
				fileCount.put(key, set.getInt("num"));
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return developerCount;
	}
	
	public static void main(String[] args) {

		try
		{
			//FileWriter writer = new FileWriter("output.sql");
			//BufferedWriter out = new BufferedWriter(writer);
			new DeveloperFeatureUsage ().run();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	protected String annotationsQuery() {
		return "SELECT NULLIF(count(annotations.annotation_type),0) as num, revisions.state, revisions.userId, revisions.filename, revisions.module, revisions.project, revisions.DateTime FROM " +
				"annotations RIGHT JOIN revisions ON " +
				"annotations.fileid = revisions.FileID " +
						"WHERE (container_granularity='full' " + 
						"OR container_granularity IS NULL) " + //FIXME waiting for cbird fix
						"GROUP BY revisions.state, revisions.filename, revisions.module, revisions.project, revisions.userId, revisions.DateTime "+
						"ORDER BY revisions.project, revisions.DateTime";
	}
	
	protected String queryTypes() {
		return "SELECT " + fieldsTypes() + ",revisions.project, revisions.userId FROM " +
				paramTypes()+" RIGHT JOIN revisions ON " +
				paramTypes()+".fileid = revisions.FileID " +
						"WHERE (container_granularity='full' " + 
						"OR container_granularity IS NULL) " + //FIXME waiting for cbird fix
						"GROUP BY revisions.project, revisions.userId " +
						"ORDER BY revisions.project";
	}

	
	protected String query() {
		return "SELECT " + fields() + ",revisions.project, revisions.userId FROM " +
				paramDecs()+" RIGHT JOIN revisions ON " +
				paramDecs()+".fileid = revisions.FileID " +
						"WHERE (container_granularity='full' " + 
						"OR container_granularity IS NULL) " + //FIXME waiting for cbird fix
						"GROUP BY revisions.project, revisions.userId, "+paramDecs()+".kind " +
						"ORDER BY revisions.project";
	}

}
