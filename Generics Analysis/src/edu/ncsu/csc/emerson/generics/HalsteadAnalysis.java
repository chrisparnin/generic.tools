package edu.ncsu.csc.emerson.generics;

import java.io.PrintWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

public class HalsteadAnalysis extends Analysis<HalsteadMetric>{

	public HalsteadAnalysis(Writer out) {
		super(out);
	}

	public static void main(String[] args) {
		new HalsteadAnalysis(new PrintWriter(System.out)).run();
	}

	public String tableName(){
		return "halstead";
	}
	
	@Override
	public String query() {
		return "SELECT halstead.distinct_operators, halstead.distinct_operands, halstead.total_operators, halstead.total_operands, " +
				"revisions.state, revisions.filename, revisions.module, revisions.project, revisions.DateTime " +
				"FROM halstead RIGHT JOIN revisions ON halstead.fileid = revisions.FileID ORDER BY revisions.project";
	}
	
	protected void printHeader(){
		log("DROP TABLE IF EXISTS " + newTableName() + ";");
		log("CREATE TABLE " + newTableName() + "(time TIMESTAMP, project VARCHAR(50), " 
						+ "totalOperators INT,"
						+ "totalOperands INT,"
						+ "sumDistinctOperators INT,"
						+ "sumDistinctOperands INT" 
						+ ");");
	}

	protected void print(Timestamp time, String project, Map<String, Revision> mostRecentRevisions) {
		HalsteadMetric sumMetric = metricFrom(mostRecentRevisions.values());
		String timeString = time.toString().replace(".0", "");
		log("INSERT INTO " +newTableName() + " VALUES ('" + timeString 
				+ "','" + project + "',"
				+ sumMetric.totalOperators + "," + sumMetric.totalOperands + ","+ 
				sumMetric.distinctOperators + ","+ sumMetric.distinctOperands
				+");");
	}


	@Override
	protected HalsteadMetric loadFrom(ResultSet rs)throws SQLException {
		HalsteadMetric m = new HalsteadMetric();
		m.distinctOperators = rs.getInt("distinct_operators");
		m.distinctOperands = rs.getInt("distinct_operands");
		m.totalOperators = rs.getInt("total_operators");
		m.totalOperands = rs.getInt("total_operands");
		return m;
	}
	
	protected HalsteadMetric metricFrom(Iterable<Revision> rs){
		HalsteadMetric sumMetric = new HalsteadMetric();
		for (Revision r : rs) {
			sumMetric.totalOperators += r.t.totalOperators;
			sumMetric.totalOperands += r.t.totalOperands;
			sumMetric.distinctOperators += r.t.distinctOperators;
			sumMetric.distinctOperands += r.t.distinctOperands;
		}
		return sumMetric;
	}
}