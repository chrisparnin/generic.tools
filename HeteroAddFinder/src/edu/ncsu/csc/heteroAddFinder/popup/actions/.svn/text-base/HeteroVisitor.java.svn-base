package edu.ncsu.csc.heteroAddFinder.popup.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

class HeteroVisitor extends ASTVisitor implements Iterable<Variable>{

	private Map<IVariableBinding, List<Expression>> typeInserts = new HashMap<IVariableBinding, List<Expression>>();
	private CompilationUnit root;

	public HeteroVisitor(ICompilationUnit u) {
		root = toCompilationUnit(u);
		root.accept(this);
	}
	
	private CompilationUnit toCompilationUnit(ICompilationUnit u) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setSource(u);

		CompilationUnit node = (CompilationUnit) parser
				.createAST(new NullProgressMonitor());
		return node;
	}

	public boolean visit(MethodInvocation mi) {

		String methodName = mi.getName().getIdentifier();
		if (methodName.equals("add") && mi.arguments().size() == 1) {
			Expression expression = mi.getExpression();
			if (expression instanceof SimpleName) {
				SimpleName sn = (SimpleName) expression;
				IBinding binding = sn.resolveBinding();
				if (binding instanceof IVariableBinding) {
					IVariableBinding varBinding = (IVariableBinding) binding;
					ITypeBinding type = varBinding.getType();
					if (type.getBinaryName().startsWith("java.util.")) {// assume
																		// a
																		// collection
						IVariableBinding decl = varBinding
								.getVariableDeclaration();
						List<Expression> existingTypes = typeInserts.get(decl);
						if (existingTypes == null) {
							existingTypes = new ArrayList<Expression>();
						}
						Expression expr = (Expression) mi.arguments().get(0);
						existingTypes.add(expr);
						typeInserts.put(decl, existingTypes);
					}
				}
			}
		}

		return true;
	}

	@Override
	public Iterator<Variable> iterator() {
		return new Iterator<Variable>() {
			
			Iterator<Entry<IVariableBinding, List<Expression>>> es = typeInserts.entrySet().iterator();
			
			@Override
			public void remove() {}
			
			@Override
			public Variable next() {
				return new Variable(es.next(),root);
			}
			
			@Override
			public boolean hasNext() {
				return es.hasNext();
			}
		};
	}
}