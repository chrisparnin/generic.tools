package edu.ncsu.csc.emerson.generics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AnalyzeAll {

	public static void main(String[] args) {
		
		try {
			
			FileWriter writer = new FileWriter("output.sql");
			BufferedWriter out = new BufferedWriter(writer);
			
			new HalsteadAnalysis(out).run();
			new CastAnalysis(out).run();
			new ParameterizedDeclarationAnalysis(out).run();
			new ParameterizedTypeAnalysis(out).run();
			
			out.flush();
			out.close();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
