package edu.ncsu.csc.emerson.generics;

import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public abstract class BasicAnalysis  extends Analysis<BasicMetric>{

	public BasicAnalysis(Writer out) {
		super(out);
	}

	
	public String query() {
		return "SELECT SUM(" + tableName()+ ".count) as count, revisions.state, revisions.filename, revisions.module, revisions.project, revisions.DateTime FROM " +
				tableName()+" RIGHT JOIN revisions ON " +
				tableName()+".fileid = revisions.FileID " +
						"WHERE (container_granularity='full' " + 
						"OR container_granularity IS NULL) " + //FIXME waiting for cbird fix
						"GROUP BY revisions.state, revisions.filename, revisions.module, revisions.project, revisions.DateTime "+
						"ORDER BY revisions.project";
	}

	protected void print(Timestamp time, String projectName, Map<String, Revision> mostRecentRevisions) {
				
		int total = 0;
		for (Revision r : mostRecentRevisions.values()) {
			 total += r.t.count;
		}
		System.out.println(total + ";" + time.toString().replace(".0", ""));
		
		String timeString = time.toString().replace(".0", "");
		log("INSERT INTO " +newTableName() + " VALUES ('" + timeString 
											+ "','" + projectName + "'," + total+");");
	}
	
	/*
	 * if there's already a revision in the collection with it for the same file, 
	 * we'll just increment the cast count for the existing revision
	 */
	protected void add(Revision rev, Timestamp timestamp, String projectName) {
		
		List<Revision> revs = getRevisionsFor(timestamp,projectName);
		
		for(Revision r : revs){
			if( r.filename.equals(rev.filename) && 
					r.module.equals(rev.module)){
				r.t = new BasicMetric(rev.t.count);
				return;
			}
		}
		
		revs.add(rev);
	}

	
	@Override
	protected BasicMetric loadFrom(ResultSet rs)throws SQLException {
		return new BasicMetric(rs.getInt("count"));
	}
	
	protected BasicMetric metricFrom(Iterable<Revision> rs){
		int val = 0;
		for (Revision r : rs) {
			 val += r.t.count;
		}
		return new BasicMetric(val);
	}
}

