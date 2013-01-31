package edu.ncsu.csc.emerson.generics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;


public class RawAnalysis extends BasicAnalysis{

	public RawAnalysis(Writer out) {
		super(out);
	}

	public static void main(String[] args) {

		try
		{
			FileWriter writer = new FileWriter("raw_output.sql");
			BufferedWriter out = new BufferedWriter(writer);
			new RawAnalysis(out).run();
			//new RawAnalysis(new PrintWriter(System.out)).run();

		}
		catch(Exception e)
		{
		}
	}
	
	protected String tableName() {
		return "rawtypes";
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
		return "SELECT NULLIF(count(rawtypes.rawtype_type),0) as num, revisions.state, revisions.filename, revisions.module, revisions.project, revisions.DateTime FROM " +
				tableName()+" RIGHT JOIN revisions ON " +
				tableName()+".filename = revisions.Filename AND rawtypes.revision = revisions.transactionid " +
						//"WHERE revisions.fileid = 765948 AND (container_granularity='full' " + 
						"WHERE (container_granularity='full' " + 
						"OR container_granularity IS NULL) " + //FIXME waiting for cbird fix 
						"GROUP BY revisions.state,revisions.filename, revisions.module, revisions.project, revisions.DateTime "+
						"ORDER BY revisions.project";
	}
}