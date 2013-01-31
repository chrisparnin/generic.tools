package edu.ncsu.csc.generics.declarations.popup.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

/*
 * Finds every generic variable reference and can be used to
 * make sure some other visitor finds them too.
 */
public class TestVisitor extends ASTVisitor{

	private Set<Type> found = new HashSet<Type>();
	
	public boolean visit(SimpleType st){
		
		IBinding binding = st.resolveBinding();
		
		if(binding instanceof ITypeBinding && ((ITypeBinding)binding).isTypeVariable()){			
			found.add(st);
		}
			
		return true;
	}
	
	public void compareTo(Set<Type> typesProducedByOtherVisitor, ICompilationUnit icu, CompilationUnit cu){
		
		Set<Type> otherCopy = new HashSet<Type>(typesProducedByOtherVisitor);
		Set<Type> thisCopy = new HashSet<Type>(found);
		
		otherCopy.removeAll(found);
		thisCopy.removeAll(typesProducedByOtherVisitor);
		
		if(!otherCopy.isEmpty()){
			System.err.println("TestVisitor broken: didn't find all elements");
			print(icu, cu, otherCopy);
		}
		
		if(!thisCopy.isEmpty()){
			System.err.println("Other Visitor broken: didn't find all generics");
			print(icu, cu, thisCopy);
		}
	}

	private void print(ICompilationUnit icu, CompilationUnit cu, Set<Type> missing) {
		for(Type t : missing){
			System.err.println(icu.getPrimaryElement().getElementName() + "\t" + cu.getLineNumber(t.getStartPosition()));
		}
	}
}