package patterns;

import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.MethodVerifier;

public abstract class AbstractVisitor extends ASTVisitor
{
	
	private ContainerGranularity containerGranularity;
	
	public static AbstractVisitor getVisitor(String visitorName, Writer writer)
	{
		visitorName = visitorName.toLowerCase();
		if (visitorName.equals("castsparams"))
		{
			return new CastsVisitor(writer);
		}
		if (visitorName.equals("classtypeparams"))
		{
			return new ClassTypeParametersVisitor(writer);
		}
		if (visitorName.equals("parameterizeddeclarations"))
		{
			return new ClassTypeParametersAndMethoParametersVisitor(writer);
		}
		if (visitorName.equals("parameterizedtypes"))
		{
			return new ParameterizedTypeVisitor(writer);
		}
		if (visitorName.equals("halstead"))
		{
			return new HalsteadVisitor(writer);
		}
		if (visitorName.equals("rawtypes"))
		{
			return new RawTypesVisitor(writer);
		}
		if (visitorName.equals("annotations"))
		{
			return new AnnotationVisitor(writer);
		}
		throw new RuntimeException("No visitor named " + visitorName);
	}
	
	public AbstractVisitor(Writer writer)
	{
		Output = writer;
	}
	
	protected Writer Output;
	public CompilationUnit Unit;

	static String line =  System.getProperty("line.separator");
	protected void WriteLine(String value)
	{
		try 
		{
			Output.append(value);
			Output.append(line);
			
			System.out.println("[Write Buffer] " + value);
		} catch (IOException e) {e.printStackTrace();}
	}
	
	 public String join(Collection<?> s, String delimiter) 
	 {
        StringBuffer buffer = new StringBuffer();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

	//public void CloseStream()
	//{
	//	try 
	//	{
	//		Output.close();
	//	} catch (IOException e) { e.printStackTrace(); }
	//}
	
	/* one of the problems with using the container to group results of visitors is that
	 * the signature changes if the container migrates to use of generics.  For instance
	 * String Join(List)
	 * may become
	 * String Join(List<String>)
	 * so this method will normalize the container signature
	 */
	public String standardizeContainerSignature(String containerSignature)
	{
		StringBuffer s = new StringBuffer();
		int bracketCount = 0;
		char []chars = containerSignature.toCharArray();
		for (int i=0; i < chars.length; i++) {
			if (chars[i] == '<') {
				bracketCount++;
				continue;
			}
			if (chars[i] == '>') {
				bracketCount--;
				continue;
			}
			if (bracketCount == 0) {
				s.append(chars[i]);
			}
		}
		return s.toString();
	}
	
	public ASTNode GetEnclosingContainer(ASTNode input)
	{
		ASTNode parent = input.getParent();
		while( parent != null && parent.getNodeType() != ASTNode.METHOD_DECLARATION && parent.getNodeType() != ASTNode.TYPE_DECLARATION )
		{
			parent = parent.getParent();
			
		}
		return parent;
	}
	
	public String GetParameterTypes(MethodDeclaration method)
	{
		ArrayList<String> names = new ArrayList<String>();
		for( int i = 0; i < method.parameters().size(); i++ )
		{
			SingleVariableDeclaration param = (SingleVariableDeclaration)method.parameters().get(i);
			names.add(param.getType().toString());
		}
		return join(names,",");
	}
	
	public void setContainerGranularity(ContainerGranularity containerGranularity) {
		this.containerGranularity = containerGranularity;
	}
	
	public String GetEnclosingContainerName(ASTNode input)
	{
		ASTNode parent = GetEnclosingContainer(input);
		if( parent == null )
			return null;
		
		String name = "ERROR";
		
		if( parent.getNodeType() == ASTNode.METHOD_DECLARATION) {
			if (containerGranularity == ContainerGranularity.NAME_ONLY) {
				name = standardizeContainerSignature(
						((MethodDeclaration)parent).getName().getFullyQualifiedName()
						);
			} else {
				name = standardizeContainerSignature(
					 ((MethodDeclaration)parent).getName().getFullyQualifiedName() + 
					 "("+GetParameterTypes((MethodDeclaration)parent) + ")"
				);
			}
		}
		if( parent.getNodeType() == ASTNode.TYPE_DECLARATION) {
			name = standardizeContainerSignature(
					((TypeDeclaration)parent).getName().getFullyQualifiedName()
			);
		}
		
		return name;
	}
}
