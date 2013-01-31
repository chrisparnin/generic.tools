package patterns;

import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;

public class RawTypesVisitor extends AbstractVisitor {
	
	private static Set<String> rawTypes;
	
	static {
		System.out.println("static initializer");
		rawTypes = new HashSet<String>();
		String rawTypesString = "BlockingQueue, List, Queue, Set, SortedSet, AbstractCollection, AbstractList, " +
				"AbstractQueue, AbstractSequentialList, AbstractSet, ArrayBlockingQueue, ArrayList, ConcurrentLinkedQueue, " +
				"CopyOnWriteArrayList, CopyOnWriteArraySet, DelayQueue, EnumSet, HashSet, LinkedBlockingQueue, LinkedHashSet, " +
				"LinkedList, PriorityBlockingQueue, PriorityQueue, Stack, SynchronousQueue, TreeSet, Vector, " + 
				"Map, ConcurrentMap, SortedMap, AbstractMap, ConcurrentHashMap, EnumMap, HashMap, Hashtable, IdentityHashMap, " +
				"LinkedHashMap, TreeMap, WeakHashMap";
		for (Object o : rawTypesString.split("[ ]*,[ ]*"))
		{
			rawTypes.add((String)o);
			System.out.println("*" + o + "*");
		}
	}
	
	public RawTypesVisitor (Writer writer) {
		super(writer);
	}
	
	/* if one wants to add to the list of known raw types at runtime */
	public static void addRawType(String typeName) {
		rawTypes.add(typeName);
	}
	
	public boolean genericVisit(ASTNode node) {
		//System.out.println(node.getClass().getSimpleName() + ":" + node.toString().trim());
		return true;
	}
	
	public boolean examineType(ASTNode sourceNode, Type type) {
		//System.out.println("Type: " + type.toString() + " ");
		if (type.isParameterizedType()) {
			/* if we get here than the type is parameterized, so don't worry about it */
			ParameterizedType pt = (ParameterizedType) type;
			//System.out.println("is Parameterized with: " + pt.typeArguments());
			Object o = type.getProperty("typeArguments");
			//System.out.println(o);
		} else {
			/* the type must is not parameterized, so look to see if it is one of the 
			 * standard JDK generic types and output something if it is
			 */
			String[] parts = type.toString().split("\\.");
			//System.out.println("parts: " + parts.toString() + " " + parts.length + " " + parts[parts.length-1]);
			
			String simpleTypeName = parts[parts.length-1];
			
			if (rawTypes.contains(simpleTypeName)) {
				System.out.println("Warning... rawtype: " + type);
				int lineNumber = sourceNode.getStartPosition();
				String parent = GetEnclosingContainerName(sourceNode);
				WriteLine(lineNumber + ":" + parent + ":" + simpleTypeName);
			}
		}
	
		/* for exploring the set of properties */
		List sp = type.structuralPropertiesForType();
		for (Object o : sp) {
			//System.out.println("Structural Property: " + o.toString());
		}
		return true;
	}
	
	public boolean visit(VariableDeclarationExpression node) {
		genericVisit(node);
		examineType(node, node.getType());
		return true;
	}
	
	public boolean visit(FieldDeclaration node) {
		genericVisit(node);
		examineType(node, node.getType());
		return true;
	}
	
	public boolean visit(MethodDeclaration node) { 
		genericVisit(node);
		//node.
		/* first look at the return type */
		Type type = node.getReturnType2();
		if (type != null) {
			examineType(type, type);
		}
		/* next look at all the parameter types */
		for (Object o : node.parameters()) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
			examineType(svd, svd.getType());
		}
		return true;
	}
	
	public boolean visit(MethodRefParameter node) {
		genericVisit(node);
		Type type = node.getType();
		examineType(node, type);
		return true;
	}
	
	public boolean visit(VariableDeclarationStatement node) {		
		System.out.println("");
		genericVisit(node);
		for (VariableDeclarationFragment vdf : (List<VariableDeclarationFragment>) node.fragments()) {
			/* each fragment is an individual variable and it's value if given,
			 * in the variable declaration statement.  As in:
			 *   int a, b, c=5;
			 */
			//System.out.println("fragment: " + vdf.getName());
			
		}
		//WriteLine("test: " + node + " " + node.getType());
		examineType(node, node.getType());
		return true;
	}
	
	/* this is for testing the visitor */
	public List<Integer> testMethod() {
		String foo;
		int a, b = 4, c;
		List badList;
		String bar = "fred";
		List<String> l = new LinkedList<String>();
		java.util.Map m;
		return null;
	}
}
