package patterns;

import java.io.Writer;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;

public class ClassTypeParametersAndMethoParametersVisitor extends AbstractVisitor
{
	
	public ClassTypeParametersAndMethoParametersVisitor(Writer output) 
	{
		super(output);
	}
	
	@Override
	public boolean visit(MethodDeclaration method)
	{
		if( method.typeParameters().size() > 0 )
		{
			int lineNumber = Unit.getLineNumber(method.getStartPosition());
			String name = method.getName().getFullyQualifiedName();
			ArrayList<String> names = new ArrayList<String>();
			for( Object param: method.typeParameters() )
			{
				TypeParameter p = (TypeParameter)param;
				names.add(p.getName().getFullyQualifiedName());
			}
			WriteLine(lineNumber + ":method:"+name+":" + this.join(names, ","));
		}
		return true;
	}
	
	@Override
	public boolean visit(TypeDeclaration type) 
	{
		if( type.typeParameters().size() > 0 )
		{
			int lineNumber = Unit.getLineNumber(type.getStartPosition());
			String name = type.getName().getFullyQualifiedName();
			ArrayList<String> names = new ArrayList<String>();
			for( Object param: type.typeParameters() )
			{
				TypeParameter p = (TypeParameter)param;
				names.add(p.getName().getFullyQualifiedName());
			}
			WriteLine(lineNumber + ":class:"+name+":" + this.join(names, ","));
		}

		//if( type.subtreeMatch(matcher, other))
		
		
		//for( Object fragObj : type.fragments())
		{
		//	VariableDeclarationFragment frag = (VariableDeclarationFragment)fragObj;
			
		
		//	if( statement.getType().resolveBinding() != null)
			//if( statement.getType().isParameterizedType() )
			{
		//		System.out.println( statement.getType() + " has type binding");
				//ITypeBinding binding = statement.getType().resolveBinding();
				//if( binding.isTypeVariable())
				//	System.out.println("Fragment-"+lineNumber+": " + statement.getType() + " " + frag.getName());			
			}
			//if( statement.getType().getNodeType() == ASTNode
		}
		
		
		return true;
	}
}