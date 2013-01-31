package edu.ncsu.csc.emerson.generics;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Analysis<T> {
	
	private Writer out;
	
	public Analysis(Writer out){
		this.out = out;
	}

	public void run() {
		Connection conn = null;
		Statement statement = null;
		ResultSet results = null;
		try {
			conn = getDatabaseConnection();
			statement = conn.createStatement(
					java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			//statement.setFetchSize(Integer.MIN_VALUE);

			// Query cache bug...
			statement.execute("SET SESSION query_cache_type = OFF;");
			//statement.execute("SET GLOBAL query_cache_size = 0;");
			//statement.setQueryTimeout(2000);
			results = run(statement);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (results != null)
					results.close();
				if (statement != null)
					statement.close();
				if (conn != null)
					conn.close();
			} catch (SQLException _) {
			}
		}
	}

	public ResultSet run(Statement statement) throws SQLException {
		ResultSet results = statement.executeQuery(query());
		process(results);
		return results;
	}
	
	protected abstract String query();
	
	protected abstract String tableName();
	
	public String newTableName(){
		return tableName() + "_over_time";
	}

	protected void printHeader(){
		log("DROP TABLE IF EXISTS " + newTableName() + ";");
		log("CREATE TABLE " + newTableName() + "(time TIMESTAMP, project VARCHAR(50), " + tableName() + " INT);");
	}
	
	protected void print(){		
		
		for(Project p : projects.values()){

			Map<String,Revision> mostRecentRevision = new HashMap<String, Revision>();
			
			for(Map.Entry<Timestamp, List<Revision>> e : p.timeToRevs.entrySet()){
				Timestamp t = e.getKey();
	
				for (Revision r : e.getValue()) {
	
					if (r.state.startsWith("deleted")) {
						mostRecentRevision.remove(r.filename);//sometimes filename is not there... ?
					} else {
						mostRecentRevision.put(r.filename, r);
					}
	
				}
				
				//System.out.println(mostRecentRevision.size());
				print(t,p.name,mostRecentRevision);
			}
		}
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected abstract void print(Timestamp t, String name, Map<String, Revision> mostRecentRevision);

	public Map<String,Project> projects = new HashMap<String, Project>();
	
	private void process(ResultSet r) throws SQLException {
		
		String lastProjectName = null;
		
		printHeader();
		
		while (r.next()) {

			try {
				Revision rev = new Revision();
				rev.t = loadFrom(r);
				rev.state = r.getString("revisions.state");
				rev.filename = r.getString("revisions.filename");
				rev.module = r.getString("revisions.module");				
				String project = r.getString("revisions.project");
				
				if(lastProjectName!=null && !lastProjectName.equals(project)){
					print();
					projects.clear();
				}
				lastProjectName = project;
				
				add(rev, r.getTimestamp("revisions.DateTime"), project);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		print();

		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		r.close();
	}
	
	protected void log(String s){
		try {
			out.append(s + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void add(Revision rev, Timestamp timestamp, String string) {
		getRevisionsFor(timestamp,string).add(rev);
	}
	
	protected abstract T loadFrom(ResultSet r) throws SQLException;

	protected List<Revision> getRevisionsFor(Timestamp time, String projectName){
		Project project = projects.get(projectName);
		if(project==null){
			project = new Project();
			project.name = projectName;
			projects.put(projectName, project);
		}
		
		List<Revision> revs = project.timeToRevs.get(time);
		if (revs == null) {
			revs = new ArrayList<Revision>();
			project.timeToRevs.put(time, revs);
		}
		return revs;
	}

	public static Connection getDatabaseConnection() throws SQLException {

		String url = "jdbc:mysql://eb2-2291-fas01.csc.ncsu.edu:4747/generics?netTimeoutForStreamingResults=200000";
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return DriverManager.getConnection(url, "emerson", "icfp");
	}


	class Project {
		String name;
		SortedMap<Timestamp, List<Revision>> timeToRevs = new TreeMap<Timestamp, List<Revision>>();
	}

	public class Revision {
		public String state;
		public String filename;
		public String module;
		public T t;
	}
}
