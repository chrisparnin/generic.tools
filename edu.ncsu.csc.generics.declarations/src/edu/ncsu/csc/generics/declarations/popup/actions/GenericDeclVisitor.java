package edu.ncsu.csc.generics.declarations.popup.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/*
 * TODO
 * 
 * xClean up "already found" code.
 * xRefactor into different classes.
 * xCheck into SVN.
 * Extract table data:
 * 	x-What type of entity
 *  x-Reference Depth
 *  x-Group by binding
 *  -Print Out
 * Draw diagram with single lines
 * Maybe calculate co-occurances?
 */
public class GenericDeclVisitor extends ASTVisitor{

	Set<Type> refsFound = new HashSet<Type>();
	Set<GenericBinding> results = new HashSet<GenericBinding>();
	
	private void locateParams(Type t, String label) {
			
		if(t.isSimpleType()){
			SimpleType st = (SimpleType)t;
			IBinding b = st.getName().resolveBinding();
			if(b instanceof ITypeBinding && ((ITypeBinding)b).isTypeVariable()){				
				if(!refsFound.add(st)){
					System.err.println("Error: Inserting type that I already found!");
				}
				GenericBinding gb = computeDistanceToDecl(st.getName());
				gb.setDeclaration(b.getJavaElement());
				gb.setReferenceType(label);
				results.add(gb);
			}
		}		
		
		if(t.isParameterizedType()){
			locateParams(((ParameterizedType) t).typeArguments(),label);
		}
		
		if(t.isWildcardType()){
			Type bound = ((WildcardType) t).getBound();
			if(bound!=null)
				locateParams(bound,label);
		}
		
		if(t.isArrayType()){
			ArrayType at = (ArrayType) t;
			locateParams(at.getComponentType(),label);
//			locateParams(at.getElementType()); -- same thing, apparently
		}
		
		if(t.isQualifiedType()){
			QualifiedType qt = (QualifiedType) t;
			locateParams(qt.getQualifier(),label);
		}
	}
	
	private GenericBinding computeDistanceToDecl(Name sn) {

		ASTNode node = sn;
		int depth = 0;
		String targetName = sn.getFullyQualifiedName();
		
		while(node!=null){
			if(node instanceof MethodDeclaration){
				
				if(contains(((MethodDeclaration) node).typeParameters(),targetName)){
					return new GenericBinding(depth,"Method");
				}else{
					depth++;
				}
					
			}else if(node instanceof TypeDeclaration){
				
				if(contains(((TypeDeclaration) node).typeParameters(),targetName)){
					return new GenericBinding(depth,"Type");
				}else{
					depth++;
				}
			}
			node = node.getParent();
		}
		
		System.err.println("Error: Could not find type binding!");
		return null;
	}
	
	public boolean contains(Iterable<?> typeParameters, String name){
		for(Object o : typeParameters){
			TypeParameter tp = (TypeParameter) o;
			if(tp.getName().toString().equals(name)){
				return true;
			}
		}
		return false;
	}

	private void locateParams(List<?> typeArguments, String label) {
		for(Object o : typeArguments){
			locateParams((Type) o,label);
		}
	}
	
	private void locateTypeParameters(List<?> typeParameters, String label) {
		for(Object o: typeParameters){
			TypeParameter tp = (TypeParameter) o;
			locateParams(tp.typeBounds(),label);
		}
	}
	
//	private Set<TypeParameter> typeParameters = new HashSet<TypeParameter>();
//	
//	public boolean visit(TypeParameter tp){
//		typeParameters.add(tp);
//		return true;
//	}
	
	public boolean visit(TypeDeclaration td){
		
		//class Foo<X,Y extends X>
		locateTypeParameters(td.typeParameters(),"TypeDeclParam");
		
		//extends ArrayList<X>
		Type superclassType = td.getSuperclassType();
		
		if(superclassType!=null)		
			locateParams(superclassType,"TypeDeclSuper");		
		
		//implements List<X>,Iterable<X>
		locateParams(td.superInterfaceTypes(),"TypeDeclInterface");		
		
		return true;
	}
	
	//enum SomeEnum implements List<Object>
	public boolean visit(EnumDeclaration e){
		locateParams(e.superInterfaceTypes(),"EnumDeclInterface");
		return true;
	}
	
	public boolean visit(MethodDeclaration md){
		
		Type returnType = md.getReturnType2();
		
		if(returnType!=null)//constructor
			locateParams(returnType,"MethodDeclReturn");
		
		for(Object o : md.parameters()){
			SingleVariableDeclaration sd = (SingleVariableDeclaration) o;
			locateParams(sd.getType(),"MethodDeclParam");
		}
		
		locateTypeParameters(md.typeParameters(),"MethodDeclTypeParam");
		
		return true;
	}
	

	public boolean visit(VariableDeclarationStatement vds){
		locateParams(vds.getType(),"VarDeclTypeStatType");		
		return true;
	}
	
	//typeically var decl in for statement
	public boolean visit(VariableDeclarationExpression e){
		locateParams(e.getType(),"VarDeclExpType");
		return true;
	}
	
	public boolean visit(FieldDeclaration fd){
		locateParams(fd.getType(),"FieldDeclType");	
		return true;
	}
	
	public boolean visit(MethodInvocation mi){
		locateParams(mi.typeArguments(),"MethodInvocationTypeArg");
		return true;
	}
	
	public boolean visit(SuperMethodInvocation smi){
		locateParams(smi.typeArguments(),"SuperInvocationTypeArg");
		return true;
	}
	
	public boolean visit(ClassInstanceCreation cic){
		
		locateParams(cic.getType(),"ClassInstCreationType");
		
		//I'm not sure what this is: it's not String in new List<String>(): always empty?
		locateParams(cic.typeArguments(),"ClassInstCreationArg");
		
		return true;
	}
	
	public boolean visit(CatchClause cc){
		locateParams(cc.getException().getType(),"CatchType");
		return true;
	}
	
	public boolean visit(EnhancedForStatement efs){
		locateParams(efs.getParameter().getType(),"ForEachType");
		return true;
	}
	
// this should be caught by method declarations, catch clauses, and enhanced for statements above
//	public boolean visit(SingleVariableDeclaration svd){
//		locateParams(svd.getType());
//		return true;
//	}

	//not sure
	public boolean visit(AnnotationTypeMemberDeclaration atd){
		locateParams(atd.getType(),"AnnTypeMemberDeclType");
		return true;
	}
	
	//MyObj o = (MyObj)o2;
	public boolean visit(CastExpression atd){
		locateParams(atd.getType(),"CastType");
		return true;
	}
	
	//myObj instanceof MyClass
	public boolean visit(InstanceofExpression e){
		locateParams(e.getRightOperand(),"InstanceOfType");
		return true;
	}
	
	//No idea what this is; findbugs doesn't have 'em
	public boolean visit(MethodRefParameter m){
		locateParams(m.getType(),"MethodRefParamType");
		return true;
	}
	
	//MyClass.class
	public boolean visit(TypeLiteral tl){
		locateParams(tl.getType(),"TypeLiteralType");
		return true;
	}
}

class GenericBinding{

	private String kind;
	private int depth;
	private String referenceType;
	private IJavaElement decl;

	public GenericBinding(int depth, String kind) {
		this.depth = depth;
		this.kind = kind;
	}

	public void setReferenceType(String refType) {
		this.referenceType = refType;
	}

	public void setDeclaration(IJavaElement javaElement) {
		this.decl = javaElement;
	}
	
	public String toString(){
		return kind + "," + referenceType + "," + decl.getHandleIdentifier() + "," + depth;
	}
}

//class GenericDeclVisitor extends ASTVisitor{
//
//private TypeDeclaration td;
//
//public boolean visit(TypeDeclaration td){
//	boolean willVisit = !td.typeParameters().isEmpty();
//	
//	if(willVisit)
//		this.td = td;
//	else
//		this.td = null;
//	
//	return willVisit;
//}
//
//public void endVisit(TypeDeclaration _){
//	if(td!=null)
//		System.out.println("end visit " + td.getName());
//}
//
//
//
//public boolean visit(SimpleName sn){
//	
//	IBinding binding = sn.resolveBinding();
//	
//	if(binding instanceof ITypeBinding && ((ITypeBinding)binding).isTypeVariable()){			
//		processTypeVar(sn);
//	}
//		
//	return true;
//}
//
//
//private void processTypeVar(SimpleName sn) {
//	
//	if(isTypeParameter(sn)){
//		debug("-Type Parameter");
//		return;
//	}else if(inParamType(sn)){
//		debug("-Passthrough");
//		//inParamType++;		
//	}else{
//		debug("-Solito");
//		//notInParamType++;
//	}
//	
//	if(inLocalVarDecl(sn)){
//		debug("--Local");
////		inLocalVar++;
//	}else if(inField(sn)){
////		inInstVar++;
//		debug("--Field");
//	}else if(inParamList(sn)){
////		inParamList++;
//		debug("--Param");
//	}else if(inReturnType(sn)){
////		inReturnVal++;
//		debug("--Return");
//	}else if(inInherit(sn)){
//		debug("--Inherit");
//	}else{
//		debug("woops!");
//	}
//		
//}
//
//private boolean inInherit(SimpleName sn) {
//	TypeDeclaration td = parentOf(sn, TypeDeclaration.class);		
//	return isChildOf(sn,td.getSuperclassType());
//}
//
//private boolean isChildOf(final ASTNode target, ASTNode toTraverse) {
//	
//	if(toTraverse==null)
//		return true;
//	
//	//I hate this anti-pattern
//	final List<Object> l = new ArrayList<Object>(1);
//	
//	toTraverse.accept(new ASTVisitor() {
//	
//		public void preVisit(ASTNode n){
//			if(n==target)
//				l.add(n);
//		}
//	
//	});
//	
//	return !l.isEmpty();
//}
//
//private boolean inReturnType(SimpleName sn) {
//	return parentOfIs(sn, MethodDeclaration.class);
//}
//
//private boolean inField(SimpleName sn) {
//	return parentOfIs(sn,FieldDeclaration.class);
//}
//
//private boolean inParamList(SimpleName sn) {
//	
//	VariableDeclaration parent = parentOf(sn,VariableDeclaration.class);
//	
//	if(parent==null)
//		return false;
//	
//	return parent.resolveBinding().isParameter();
//}
//
//private boolean inLocalVarDecl(SimpleName sn) {
//	return parentOfIs(sn,VariableDeclarationStatement.class);
//}
//
//void debug(String arg){
//	System.out.println(arg);
//}
//
//private boolean isTypeParameter(SimpleName sn) {
//	return sn.getParent() instanceof TypeParameter;
//}
//
//private boolean inParamType(SimpleName sn) {
//	return parentOfIs(sn,ParameterizedType.class);
//}
//
//private boolean parentOfIs(ASTNode n, Class<? extends ASTNode> c) {
//	return parentOf(n,c)!=null;
//}
//
//@SuppressWarnings("unchecked")
//private <T  extends ASTNode> T parentOf(ASTNode n, Class<T> c) {
//	
//	if(n==null)
//		return null;
//	else if(c.isAssignableFrom(n.getClass()))
//		return (T)n;
//	
//	return parentOf(n.getParent(), c);
//}
//}