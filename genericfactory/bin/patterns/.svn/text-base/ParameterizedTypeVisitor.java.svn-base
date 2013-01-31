package patterns;
import java.io.Writer;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;


public class ParameterizedTypeVisitor extends RawTypesVisitor {
	
	public ParameterizedTypeVisitor(Writer writer)
	{
		super(writer);
	}
	
	private String getTypeName(Type type) {
		ITypeBinding binding = type.resolveBinding();
		if (binding != null) {
			return binding.getQualifiedName();
		} else {
			return type.toString();
		}
	}

	public boolean examineType(ASTNode sourceNode, Type type) {
		if (type.isParameterizedType()) {
			ParameterizedType pt = (ParameterizedType) type;
			int lineNumber = Unit.getLineNumber(pt.getStartPosition());
			String parent = GetEnclosingContainerName(pt);
			StringBuffer typeArgs = new StringBuffer();
			boolean first = true;
			for (Object typeArg : pt.typeArguments()) {
				if (!first) {
					typeArgs.append(",");
				}
				first = false;
				typeArgs.append(getTypeName((Type)typeArg));
			}
			if( parent != null )
			{
				WriteLine(lineNumber + ":" + parent + ":"  + getTypeName(pt.getType()) + ":" + typeArgs);
			}
		}
		return true;
	}

	/*
	@Override
	public boolean visit(ParameterizedType pt) 
	{
		int lineNumber = Unit.getLineNumber(pt.getStartPosition());
		String parent = GetEnclosingContainerName(pt);
		StringBuffer typeArgs = new StringBuffer();
		boolean first = true;
		for (Object type : pt.typeArguments()) {
			if (!first) {
				typeArgs.append(",");
			}
			first = false;
			typeArgs.append(getTypeName((Type)type));
		}

		if( parent != null )
		{
			
			WriteLine(lineNumber + ":" + parent + ":"  + getTypeName(pt.getType()) + ":" + typeArgs);
		}
		return true;
	} */
	
	
}
