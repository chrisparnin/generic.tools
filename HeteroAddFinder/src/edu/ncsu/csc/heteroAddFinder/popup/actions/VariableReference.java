package edu.ncsu.csc.heteroAddFinder.popup.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;

@SuppressWarnings("restriction")
class VariableReference{

	private Expression expression;
	private CompilationUnit root;

	public VariableReference(Expression next, CompilationUnit root) {
		this.expression = next;
		this.root = root;
	}
	
	/**
	 * SuperTypeBegin is used to find the Super Types of an object given an
	 * expression and steps into the recursion for interfaces and superclasses
	 * of the expression
	 * 
	 * @param expression
	 * @return
	 * @return
	 */
	public SortedMap<String, Integer> superTypes() {

		HashMap<String, Integer> superTypes = new HashMap<String, Integer>();

		ITypeBinding expressionType = expression.resolveTypeBinding();

		if(expressionType==null){
			superTypes.put("[Expression Type Unresolvable]", 0);
		}else{
			
			// special case: if this is a primitive, then it's autoboxed. change
			// binding to boxed type
			// e.g.: collection.add(12134);
			if (expressionType.isPrimitive()) {
				expressionType = Bindings.getBoxedTypeBinding(expressionType,
						expression.getAST());
			}
			superTypesInternal(superTypes, expressionType, 0);
		}
			
		SuperTypeMapSort sortedSuperTypes = new SuperTypeMapSort(superTypes);

		SortedMap<String, Integer> sortedMap = new TreeMap<String, Integer>(
				sortedSuperTypes);
		sortedMap.putAll(superTypes);

		return sortedMap;
	}
	
	/**
	 * typeOf is used to search for an objects's Add Type and will place them in
	 * the database for statistical analysis
	 */
	public String type() {
		ITypeBinding binding = expression.resolveTypeBinding();
		return binding == null ? "<unknown expression type>"
				: binding.getName();
	}
	
	public int lineNumber(){
		return root.getLineNumber(expression.getStartPosition());
	}

	/**
	 * SuperTypeFinder performs recursion on the superClasses and Interfaces of
	 * the expression passed from a collection. The method tracks the interface
	 * level and class level in the type hierarchy.
	 * 
	 * @param typeBinding
	 * @param level
	 */
	private void superTypesInternal(Map<String, Integer> superTypes,
			ITypeBinding typeBinding, Integer level) {

		String bindingName = typeBinding.getName();

		insertBindingName(superTypes, level, bindingName);

		// special case: bindings to an interface without superinterfaces will
		// not have object superclass
		// but saying so will make our analysis easier
		if (typeBinding.isInterface()
				&& typeBinding.getInterfaces().length == 0) {
			insertBindingName(superTypes, level + 1, "Object");
		}

		List<ITypeBinding> directSupertypes = new ArrayList<ITypeBinding>(
				Arrays.asList(typeBinding.getInterfaces()));

		ITypeBinding superClass = typeBinding.getSuperclass();
		if (superClass != null)
			directSupertypes.add(superClass);

		for (ITypeBinding b : directSupertypes) {
			superTypesInternal(superTypes, b, level + 1);
		}
	}
	

	private void insertBindingName(Map<String, Integer> superTypes,
			Integer level, String bindingName) {
		if (superTypes.containsKey(bindingName)) {
			Integer oldLevel = superTypes.get(bindingName);
			if (oldLevel > level) {
				superTypes.put(bindingName, level);
			}
		} else {
			superTypes.put(bindingName, level);
		}
	}

	/**
	 * SuperTypeMapSort is used to sort the HashMap that is storing the super
	 * types of the collection object. The Map is sorted based off of hierarchy
	 * level value from least to greatest.
	 * 
	 */
	private static class SuperTypeMapSort implements Comparator<String> {
		private Map<String, Integer> sort;

		public SuperTypeMapSort(Map<String, Integer> sort) {
			this.sort = sort;
		}

		public int compare(String x, String y) {
			int diff = sort.get(x) - sort.get(y);
			return diff == 0 ? -1 : diff;
		}
	}
	
}