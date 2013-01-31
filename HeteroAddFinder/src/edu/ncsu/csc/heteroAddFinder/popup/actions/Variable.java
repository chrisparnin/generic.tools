package edu.ncsu.csc.heteroAddFinder.popup.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IVariableBinding;

class Variable implements Iterable<VariableReference>{

	private Entry<IVariableBinding, List<Expression>> var;
	private IJavaElement variableDeclaration;
	private CompilationUnit root;

	public Variable(Entry<IVariableBinding, List<Expression>> var, CompilationUnit root) {
		this.var = var;
		
		
		variableDeclaration = (IJavaElement) var.getKey().getJavaElement();
		this.root = root;
	}

	public int declarationLineNumber() {

		try {
			return root.getLineNumber(((ISourceReference) variableDeclaration).getSourceRange().getOffset());
		} catch (Exception _) {
			return -1;
		}
	}

	public String declarationType() {
		if(variableDeclaration!=null)
			return variableDeclaration.toString().split(" ")[0];
		else
			return "<Unknown>";
	}

	@Override
	public Iterator<VariableReference> iterator() {
		return new Iterator<VariableReference>() {
			
			Iterator<Expression> iterator = var.getValue().iterator();
			
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public VariableReference next() {
				return new VariableReference(iterator.next(),root);
			}

			@Override
			public void remove() {}
		};
	}
	
	public String commonSuperclassOfAdders(){

		List<Bag<String>> superClasses = new LinkedList<Bag<String>>();
		Set<String> baseClasses = new HashSet<String>();
		
		for(VariableReference ref : this){
			Iterator<Entry<String, Integer>> iterator = ref.superTypes().entrySet().iterator();
			int i = 0;
			while(iterator.hasNext()){
				Entry<String, Integer> pair = iterator.next();			
				
				if(i == pair.getValue()){
					putAt(superClasses, i, pair.getKey());
				}else{
					putAt(superClasses, i = i + 1, pair.getKey());
				}
				
				if(i==0)
					baseClasses.add(pair.getKey());
			}
		}
		
		Bag<String> potentialsSoFar = new HashBag<String>();
		for(Bag<String> potentials : superClasses){
			potentialsSoFar.addAll(potentials);
			for(String potential: potentials){
				if(potential.equals("Object") || potential.equals("Cloneable") || potential.equals("Serializable")){
					//ignore these
				}else if(potentialsSoFar.getCount(potential)==var.getValue().size()){
					return potential;
				}
			}
		}
		
		return "<"+Arrays.toString(baseClasses.toArray())+">";
		
	}

	private void putAt(List<Bag<String>> superClasses, int i,
			String key) {
		
		while(superClasses.size()<=i){
			superClasses.add(new HashBag<String>());
		}
		superClasses.get(i).add(key);
	}
}