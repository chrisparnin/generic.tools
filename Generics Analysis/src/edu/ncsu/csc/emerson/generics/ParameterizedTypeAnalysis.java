package edu.ncsu.csc.emerson.generics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;


public class ParameterizedTypeAnalysis extends BasicAnalysis{

	public ParameterizedTypeAnalysis(Writer out) {
		super(out);
	}

	public static void main(String[] args) {
		//new ParameterizedTypeAnalysis(new PrintWriter(System.out)).run();
		try
		{
			FileWriter writer = new FileWriter("pt_output.sql");
			BufferedWriter out = new BufferedWriter(writer);
			new ParameterizedTypeAnalysis(out).run();
		}
		catch(Exception e)
		{
		}
	}

	@Override
	protected String tableName() {
		return "parameterized_types";
	}
}