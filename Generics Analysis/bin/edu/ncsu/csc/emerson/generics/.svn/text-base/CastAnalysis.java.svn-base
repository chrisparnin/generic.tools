package edu.ncsu.csc.emerson.generics;

import java.io.PrintWriter;
import java.io.Writer;


public class CastAnalysis extends BasicAnalysis{

	public CastAnalysis(Writer out) {
		super(out);
	}

	public static void main(String[] args) {
		new CastAnalysis(new PrintWriter(System.out)).run();
	}
	
	protected String tableName() {
		return "casts";
	}
}