package edu.ncsu.csc.heteroAddFinder.popup.actions;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;

public class FindHeteroAdds extends FindInterestingMethods {

	private String insert = "INSERT INTO HeteroAddFinderResults(ProjectName,ClassName,DeclType,DeclLineNumber,AddType,AddLineNumber,SuperType,STLevel)";
	private String values = "VALUES";
	
	@Override
	protected void processCU(ICompilationUnit u) throws IOException {
		
		HeteroVisitor visitor = new HeteroVisitor(u);		

		String projectName = u.getJavaProject().getElementName();
		IPath path = u.getResource().getFullPath();

		FileWriter writer = getWriter();

		for (Variable v : visitor) {

			for (VariableReference ref : v) {
				try {
					SortedMap<String, Integer> superTypes = ref.superTypes();
					
					for (Entry<String, Integer> sortedEntry : superTypes.entrySet()) {
						writer.write(insert + "\n");
						writer.write(values + "\n");
						writer.write("('" + projectName + "', '"
								+ path + "', '" + v.declarationType() + "', "
								+ v.declarationLineNumber() + ", '" + ref.type() + "', "
								+ ref.lineNumber() + ", '" + sortedEntry.getKey()
								+ "', " + sortedEntry.getValue() + ");\n");

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			System.out.print(path + "\t" + v.declarationLineNumber() + "\t");
			System.out.println(v.commonSuperclassOfAdders());
		}
		writer.close();
	}

	private FileWriter getWriter() throws IOException {
		JFileChooser fr = new JFileChooser();
		FileSystemView fw = fr.getFileSystemView();
		String dir = fw.getDefaultDirectory().toString();
		FileWriter writer = new FileWriter(dir + "/HeteroAdds.sql", true);
		return writer;
	}
}
