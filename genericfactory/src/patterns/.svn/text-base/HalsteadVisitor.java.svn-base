package patterns;

import java.io.Writer;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

public class HalsteadVisitor extends AbstractVisitor{

	private Bag<String> operators = new HashBag<String>();
	private Bag<String> operands = new HashBag<String>();
	
	public HalsteadVisitor(Writer writer) {
		super(writer);
	}
	
	private void operator(ASTNode node) {
		addOperator(typeName(node));
	}
	
	private void operator(ASTNode node, Object name){
		addOperator(typeName(node) + "\t" + name.toString());
	}

	private void addOperator(String id) {
		operators.add(id);
		//System.out.println("operator: " + id);
	}
	
	private void operand(ASTNode node, Object name){
		addOperand(typeName(node) + "\t" + name.toString());
	}

	private void addOperand(String id) {
		operands.add(id);
		//System.out.println("operand:  " + id);
	}

	private String typeName(ASTNode node) {
		return node.getClass().toString().
				replace("class org.eclipse.jdt.core.dom.", "");
	}

	/*
	 * This happens at the absolute end... output values here:
	 * 
	 * n1 = the number of distinct operators 
	 * n2 = the number of distinct operands 
	 * N1 = the total number of operators 
	 * N2 = the total number of operands
	 * 
	 * http://en.wikipedia.org/wiki/Halstead_complexity_measures
	 */
	@Override
	public void endVisit(CompilationUnit node) {
		super.endVisit(node);
		WriteLine(n1() + ":" + n2() + ":" + N1() + ":" + N2());
		//System.out.println(effort());
	}

	private int N2() {
		return operands.size();
	}

	private int N1() {
		return operators.size();
	}

	private int n2() {
		return operands.uniqueSet().size();
	}

	private int n1() {
		return operators.uniqueSet().size();
	}
	
	private int length(){
		return N1() + N2();
	}
	
	private int vocabulary(){
		return n1() + n2();
	}
	
	private double volume(){
		return length() * Math.log(vocabulary()) / Math.log(2);
	}
	
	private double difficulty(){
		return n1()/2.0 + n2()==0 ? 0 : N2()/n2();
	}
	
	private double effort(){
		return volume()*difficulty();
	}
	
	

	/*
	 * Interesting operators...
	 */
	@Override
	public boolean visit(InfixExpression node) {
		operator(node,node.getOperator());
		return true;
	}
	
	@Override
	public boolean visit(PostfixExpression node) {
		operator(node,node.getOperator());
		return true;
	}

	@Override
	public boolean visit(PrefixExpression node) {
		operator(node,node.getOperator());
		return true;
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		operator(node,node.booleanValue());
		return true;
	}
	
	//a cast will get counted twice
	@Override
	public boolean visit(CastExpression node) {
		operator(node,node.getClass().toString());
		return true;
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		operator(node,node.arguments().size());
		return true;
	}
	
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		operator(node,node.arguments().size());
		return true;
	}
	
	/*
	 * Skipped
	 */

	@Override
	public boolean visit(Block node) {
//		operator(node);
		return true;
	}
	
	@Override
	public boolean visit(CompilationUnit node) {
//		operator(node);
		return true;
	}
	
	@Override
	public boolean visit(EmptyStatement node) {
//		operator(node);
		return true;
	}
	
	//wrapper
	@Override
	public boolean visit(ExpressionStatement node) {
//		operator(node);
		return true;
	}
	
	//wrapper
	@Override
	public boolean visit(TypeDeclarationStatement node) {
		//operator(node);
		return true;
	}
	
	@Override
	public boolean visit(BlockComment node) {
//		operator(node);
		return false;
	}
	
	@Override
	public boolean visit(Javadoc node) {
//		operator(node);
		return false;
	}
	
	@Override
	public boolean visit(LineComment node) {
//		operator(node);
		return false;
	}

	@Override
	public boolean visit(MemberRef node) {
//		operator(node);
		return false;
	}
	
	@Override
	public boolean visit(MethodRef node) {
//		operator(node);
		return false;
	}
	
	@Override
	public boolean visit(MethodRefParameter node) {
//		operator(node);
		return false;
	}
	
	@Override
	public boolean visit(TagElement node) {
//		operator(node);
		return false;
	}
	
	@Override
	public boolean visit(TextElement node) {
//		operator(node);
		return false;
	}
	
	@Override
	public boolean visit(QualifiedType node) {
//		operator(node.getClass().toString()+node);
		return true;
	}

	//simpletypes contain simplenames
	@Override
	public boolean visit(SimpleType node) {
//		operand(node,node.getName());
		return true;
	}
	
	//typically just a SimpleName and maybe a comma if theres more than one
	@Override
	public boolean visit(VariableDeclarationFragment node) {
//		operator(node);
		return true;
	}


	/*
	 * Operands
	 */
	
	@Override
	public boolean visit(CharacterLiteral node) {
		operand(node,node.charValue());
		return true;
	}
	
	@Override
	public boolean visit(Modifier node) {
		operand(node,node.getKeyword());
		return true;
	}
	

	@Override
	public boolean visit(NumberLiteral node) {
		operand(node,node.getToken());
		return true;
	}
	
	@Override
	public boolean visit(PrimitiveType node) {
		operand(node,node.getPrimitiveTypeCode());
		return true;
	}
	
	//count the qualified name, but don't look any further
	@Override
	public boolean visit(QualifiedName node) {
		operand(node,node.getFullyQualifiedName());
		return false;
	}
	
	@Override
	public boolean visit(SimpleName node) {
		operand(node,node.getIdentifier());
		return true;
	}
	
	@Override
	public boolean visit(StringLiteral node) {
		operand(node,node.getLiteralValue());
		return true;
	}

	/*
	 * Uninteresting operators...
	 */
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ArrayAccess node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ArrayType node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(AssertStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(BreakStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(CatchClause node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(DoStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(Initializer node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(LabeledStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(MemberValuePair node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(NullLiteral node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ParameterizedType node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(SwitchCase node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ThisExpression node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(TryStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(TypeParameter node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		operator(node);
		return true;
	}

	@Override
	public boolean visit(WildcardType node) {
		operator(node);
		return true;
	}

}
