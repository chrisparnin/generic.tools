package edu.ncsu.csc.emerson.generics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AnnotationAnalysis extends BasicAnalysis{

	public AnnotationAnalysis(Writer out) {
		super(out);
	}

	public static void main(String[] args) {
		//new AnnotationAnalysis(new PrintWriter(System.out)).run();

		try
		{
			FileWriter writer = new FileWriter("an_output.sql");
			BufferedWriter out = new BufferedWriter(writer);
			new AnnotationAnalysis(out).run();
		}
		catch(Exception e)
		{
		}
	}
	
	protected String tableName() {
		return "annotations";
	}
	
	@Override
	protected BasicMetric loadFrom(ResultSet rs)throws SQLException {
		BasicMetric m = new BasicMetric(0);
		String l = rs.getString("num");
		if(l!=null){
			return new BasicMetric(rs.getInt("num"));
		}
		return m;
	}
	
	@Override
	public String query() {
		return "SELECT NULLIF(count("+tableName()+".annotation_type),0) as num, revisions.state, revisions.filename, revisions.module, revisions.project, revisions.DateTime FROM " +
				tableName()+" RIGHT JOIN revisions ON " +
				tableName()+".fileid = revisions.FileID " +
						"WHERE (container_granularity='full' " + 
						"OR container_granularity IS NULL) " + //FIXME waiting for cbird fix
						"GROUP BY revisions.state, revisions.filename, revisions.module, revisions.project, revisions.DateTime "+
						"ORDER BY revisions.project";
	}
}