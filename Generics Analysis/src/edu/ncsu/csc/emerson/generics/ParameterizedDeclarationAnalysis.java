package edu.ncsu.csc.emerson.generics;

import java.io.PrintWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ParameterizedDeclarationAnalysis extends BasicAnalysis{

	public ParameterizedDeclarationAnalysis(Writer out) {
		super(out);
	}

	public static void main(String[] args) {
		new ParameterizedDeclarationAnalysis(new PrintWriter(System.out)).run();
	}

	@Override
	protected String tableName() {
		return "parameterized_declarations";
	}
	
//	@Override
//	protected BasicMetric loadFrom(ResultSet rs)throws SQLException {
//		return new BasicMetric(rs.getString("num")==null ? 0 : rs.getInt("num"));
//	}
	
	@Override
	public String query() {
		return "SELECT NULLIF(count("+tableName()+".class_type),0) as count, revisions.state, revisions.filename, revisions.module, revisions.project, revisions.DateTime FROM " +
				tableName()+" RIGHT JOIN revisions ON " +
				tableName()+".fileid = revisions.FileID " +
						"WHERE revisions.project ='squirrel-sql' and (container_granularity='full' " + 
						"OR container_granularity IS NULL) " + //FIXME waiting for cbird fix
						"GROUP BY revisions.state, revisions.filename, revisions.module, revisions.project, revisions.DateTime "+
						"ORDER BY revisions.project";
	}
}