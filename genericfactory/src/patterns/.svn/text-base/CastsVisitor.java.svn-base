package patterns;

import java.io.Writer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Type;

public class CastsVisitor extends AbstractVisitor
{
	
	public CastsVisitor(Writer writer)
	{
		super(writer);
	}
	
	@Override
	public boolean visit(CastExpression cast) 
	{
		int lineNumber = Unit.getLineNumber(cast.getStartPosition());
		Type t = cast.getType();
		String parent = GetEnclosingContainerName(cast);
		if( parent != null )
		{
			
			WriteLine(lineNumber + ":" + parent + ":"  +t);
		}
		return true;
	}
}