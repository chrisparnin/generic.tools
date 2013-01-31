package patterns;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.VariableDeclaration;

public class AnnotationVisitor extends AbstractVisitor {

	public AnnotationVisitor(Writer writer) 
	{
		super(writer);
	}
	
	@Override
	public boolean visit(NormalAnnotation annotation) 
	{
		//String parent = GetEnclosingContainerName(annotation);
		String parent = GetParentName(annotation.getParent());
		//annotation.getTypeNameProperty();
		String property = annotation.getTypeNameProperty().toString();
		
		List<String> values = new ArrayList<String>();
		for( Object obj : annotation.values() )
		{
			if( obj instanceof MemberValuePair)
			{
				MemberValuePair pair = (MemberValuePair)obj;
			}
			values.add( obj.toString());
		}
		
		Name typeName = annotation.getTypeName();
		if( parent != null )
		{
			WriteLine(parent + ":" + join(values,";") + ":" + typeName.getFullyQualifiedName());
		}
		return true;
		//NormalAnnotation;
		//MarkerAnnotation;
		//SingleMemberAnnotation;
	}

	public String GetParentName( ASTNode node )
	{
		if( node.getNodeType() == ASTNode.PACKAGE_DECLARATION)
			return "package";
		if( node.getNodeType() == ASTNode.FIELD_DECLARATION )
			return "field";
		if( node.getNodeType() == ASTNode.METHOD_DECLARATION )
		{
			MethodDeclaration meth = (MethodDeclaration)node;
			if( meth.isConstructor() )
				return "constructor";
			return "method";
		}
		if( node.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION )
		{
			if( node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION)
				return "parameter";			
			return "local";
		}
		if( node.getNodeType() == ASTNode.TYPE_DECLARATION )
			return "type";
		if( node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT )
			return "local";
		return "unknown";
	}
	
	@Override
	public boolean visit(MarkerAnnotation annotation) 
	{
		String parent = GetParentName(annotation.getParent());
		//String parent = GetEnclosingContainerName(annotation);
		//annotation.getTypeNameProperty();
		String property = annotation.getTypeNameProperty().toString();
		Name typeName = annotation.getTypeName();
		if( parent != null )
		{
			WriteLine(parent + ":" + "" + ":" + typeName.getFullyQualifiedName());
		}
		return true;
	}
	
	@Override
	public boolean visit(SingleMemberAnnotation annotation) 
	{
		String parent = GetParentName(annotation.getParent());
		//String parent = GetEnclosingContainerName(annotation);
		//annotation.getTypeNameProperty();
		String property = annotation.getTypeNameProperty().toString();
		Name typeName = annotation.getTypeName();
		if( parent != null )
		{
			WriteLine(parent + ":" + ""+ ":" + typeName.getFullyQualifiedName());
		}
		return true;
	}
}
