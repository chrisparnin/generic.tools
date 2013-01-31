package tokenize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.TypeParameter;

import patterns.AbstractVisitor;
import patterns.ContainerGranularity;
/**
 * This class controls all aspects of the application's execution
 */


@MyAnnotation(first="class",last=11)
public class Application implements IApplication {

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	@MyAnnotation(first="meth",last=3)
	public Object start(@MyAnnotation(first="param",last=12)IApplicationContext context) throws Exception {
		System.out.println("Hello RCP World!");
				
		@MyAnnotation(first="local",last=11)
		Hashtable<String,String> options = ProcessCommandArgs();
		@MyAnnotation(first="local",last=11)
		int test,test1,test2;
		String output = "output.txt";
		if( options.containsKey("output") )
		{
			output = options.get("output");
		}

	
		/* if the -server option is passed then run in server mode */
		if (options.containsKey("server"))
		{
			TokenizeServer server = new TokenizeServer(this);
			int port = 6000;
			if (options.containsKey("port"))
			{
				port = Integer.parseInt(options.get("port"));
				assert(port > 1024);
			}
			System.out.println("Running as server on port " + port);
			server.serveForever(port);
			return IApplication.EXIT_OK;
		}
		
		/*CAB - no need to wrap this... if it fails then we're hosed anyways */
		Writer outputWriter = new FileWriter(output);
		
		if (options.containsKey("directory") && options.containsKey("pattern"))
		{
			System.out.println("directory");
			File dir = new File(options.get("directory"));
			assert(dir.isDirectory());
			for (File javaFile : getDirectoryListing(dir))
			{
				String path = javaFile.getAbsolutePath();
				System.out.printf("Parsing %s\n", path);
				outputWriter.write("PATH:" + path + "\n");
				ParserTest(path, options.get("pattern"), ContainerGranularity.FULL, outputWriter);
			}
			System.out.println("Finished at " + new java.util.Date());
		}
		if( options.containsKey("file") && options.containsKey("pattern") )
		{
			ParserTest(options.get("file"),options.get("pattern"), ContainerGranularity.FULL, outputWriter);
		}
		else
		{
			ParserTest("C:/Users/cp125/Desktop/repo/activity/generics/tools/genericfactory/src/tokenize/Application.java","Annotations", ContainerGranularity.FULL, outputWriter);

			//ParserTest("C:/Users/cp125/Desktop/repo/activity/generics/tools/genericfactory/src/tokenize/Application.java","ClassTypeParams",output);
//			ParserTest("C:/Users/emerson/Documents/workspaces/workspace/Tokenize/examples/DevelopersController.2890.java","halstead",outputWriter);
//			ParserTest("C:/Users/emerson/Documents/workspaces/workspace/Tokenize/examples/DevelopersController.3838.java","halstead",outputWriter);
			//ParserTest("/Users/christianbird/working_papers/generics/tools/genericfactory/src/tokenize/Application.java","CastsParams",
			//		ContainerGranularity.FULL, outputWriter);

		}
		outputWriter.close();
				
		return IApplication.EXIT_OK;
	}
	
	
	private List<File> getDirectoryListing(File dir)
	{
		List<File> files = new LinkedList<File>();
		getDirectoryListingRec(dir, files);
		return files;
	}
	
	private void getDirectoryListingRec(File dir, List<File> files) 
	{
		System.out.println("visiting directory " + dir.getAbsolutePath());
		assert(dir.isDirectory());
		for (File item : dir.listFiles())
		{
			if (item.getName().endsWith(".java") && item.isFile()) 
			{
				files.add(item);
			} else if (item.isDirectory())
			{
				getDirectoryListingRec(item, files);
			}
		}
	}
	
	
	
	
	
	private Hashtable<String,String> ProcessCommandArgs() 
	{
		String[] args = Platform.getCommandLineArgs();
		
		Hashtable<String,String> options = new Hashtable<String,String>();
		
		for( int i=0; i < args.length; i++ )
		{
			String arg = args[i];
			if (arg.equals("-server"))
			{
				options.put("server", "True");
			}
			if (arg.equals("-port") && i + 1 < args.length ) 
			{
				options.put("port", args[++i]);
			}
			if( arg.equals("-file") && i + 1 < args.length )
			{
				options.put("file", args[++i]);
			}
			if( arg.equals("-pattern") && i + 1 < args.length )
			{
				options.put("pattern", args[++i]);
			}
			if( arg.equals("-output") && i + 1 < args.length )
			{
				options.put("output", args[++i]);
			}
			if (arg.equals("-directory") && i + 1 < args.length )
			{
				options.put("directory", args[++i]);
			}

		}
		return options;
	}
	
	private StringBuffer GetFileContents(String source) 
	{
		BufferedReader r=null;
		try 
		{
			r = new BufferedReader(new FileReader(source));
		} catch (FileNotFoundException e1) {
			System.err.println("Exception when reading file: " + source);
			return new StringBuffer();
		}
		
		StringBuffer buffer = new StringBuffer();
		String line = null;
		try {
			while( (line=r.readLine()) != null )
			{
				buffer.append(line).append("\n");
			}
		} catch (IOException e) {}
		return buffer;
	}

	public void ParserTest(String file, String pattern, 
			ContainerGranularity containerGranularity, Writer output) 
	{
		
			StringBuffer buffer = GetFileContents(file);
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setUnitName("Tmp"); //$NON-NLS-1$
			parser.setCompilerOptions(null);
			//parser.setProject(null);
			//parser.setProject(dummyicu.getJavaProject());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			//parser.setSource(tmpDoc.get().toCharArray());
			parser.setSource(buffer.toString().toCharArray());
			ASTNode node = parser.createAST(null);
			
			//DefinitionsVisitor visitor = new DefinitionsVisitor("output.txt");
			//visitor.Unit = (CompilationUnit)node;
			AbstractVisitor visitor = AbstractVisitor.getVisitor(pattern, output);
			visitor.setContainerGranularity(containerGranularity);
			visitor.Unit = (CompilationUnit)node;
			
			node.accept(visitor);
	}
	
	public class DefinitionsVisitor extends ASTVisitor
	{
		public DefinitionsVisitor(String file)
		{
			FileWriter writer = null;
			try 
			{
				writer = new FileWriter(file);
			} catch (IOException e) { e.printStackTrace();}
			Output = writer;
			
			int x = (int)3.0f;
		}
		
		private FileWriter Output;
		public CompilationUnit Unit;
		
		public void CloseStream()
		{
			try 
			{
				Output.close();
			} catch (IOException e) { e.printStackTrace(); }
		}
		
//		@Override
//		public boolean visit(VariableDeclarationStatement statement) 
//		{
//			int lineNumber = Unit.getLineNumber(statement.getStartPosition());
//
//			for( Object fragObj : statement.fragments())
//			{
//				VariableDeclarationFragment frag = (VariableDeclarationFragment)fragObj;
//				
//			
//				if( statement.getType().resolveBinding() != null)
//				//if( statement.getType().isParameterizedType() )
//				{
//					System.out.println( statement.getType() + " has type binding");
//					ITypeBinding binding = statement.getType().resolveBinding();
//					if( binding.isTypeVariable())
//						System.out.println("Fragment-"+lineNumber+": " + statement.getType() + " " + frag.getName());			
//				}
//				//if( statement.getType().getNodeType() == ASTNode
//			}
//			
//			
//			return true;
//		}

		
		public class Pair<T1, T2> {
			Pair(T1 a,T2 b){}
		}

		private class Fooness<T extends Object>
		{
			public Pair<T,T> twice(T value)
			{
				return new Pair<T,T>(value,value);
			}
			
			ArrayList<T> getManyFoo()
			{
				T too = null;
				T[] array = (T[]) new Object[3];
				ArrayList<T> a = new ArrayList<T>();
				//a.addAll(array);
				a.get(0);
				
				return a;
			}
			
			public void A()
			{
				List<Double> ints = new ArrayList<Double>();
				ints.add(2.3);
				List<Double> nums = ints; 
				nums.add(3.14);  
				Number x=ints.get(1);
			}
		}
		@Override
		public boolean visit(TypeParameter t)
		{
			int lineNumber = Unit.getLineNumber(t.getStartPosition());
			System.out.println(lineNumber + ":"+ "TypeParameter" + ":"+ t.toString());
			return true;
		}
		
		@Override
		public boolean visit(ParameterizedType p)
		{
			int lineNumber = Unit.getLineNumber(p.getStartPosition());
			//System.out.println(lineNumber + ":"+ "ParameterizedType" + ":"+ p.toString());
			WriteLine(lineNumber + ":"+ "ParameterizedType" + ":"+ p.toString());
			//typeParameter.typeArguments()
			//typeParameter.
			return true;
		}
		
		private void WriteLine(String value)
		{
			try 
			{
				Output.append(value);
				Output.append("\n");
			} catch (IOException e) {e.printStackTrace();}
		}		
		
		//@Override
		//public boolean visit()
		//{
			
		//	return true;
		//}
	}

	@MyAnnotation(first="interface",last=11)
	public interface annotationtest
	{
	
	}
	
	private void ScanTest(StringBuffer buffer) 
	{
		IScanner scanner = ToolFactory.createScanner(true, true, false, true);
		scanner.setSource(buffer.toString().toCharArray());
		   while (true) {
		    int token = 0;
			try {
				token = scanner.getNextToken();
			} catch (InvalidInputException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		     if (token == ITerminalSymbols.TokenNameEOF) break;
		     System.out.println(token + " : " + new String(scanner.getCurrentTokenSource()));
		   }
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		// nothing to do
	}
}
