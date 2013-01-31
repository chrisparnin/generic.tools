package patterns;

import java.io.Writer;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;

/*
* @deprecated use {@link ClassTypeParametersAndMethoParametersVisitor}^M                                                                  
*/
public class ClassTypeParametersVisitor extends AbstractVisitor
{
	
	public ClassTypeParametersVisitor(Writer output) 
	{
		super(output);
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
			WriteLine(lineNumber + ":"+name+":" + this.join(names, ","));
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