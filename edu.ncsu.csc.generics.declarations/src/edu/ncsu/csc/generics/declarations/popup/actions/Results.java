package edu.ncsu.csc.generics.declarations.popup.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//						TypeDeclParam			TypeDeclSuper			TypeDeclInterface
public class Results<A,B extends A> 	extends ArrayList<A>	implements List<A>{

	//FieldDeclType?
	A a;
	
	//MethodDeclReturn,MethodDeclParam
	A foo(A a){
		
		//VarDeclTypeStatType or VarDeclExp
		A a2;
		
		//ForEachType and ClassInstCreationType
		for(A a3 : new ArrayList<A>()){
			
		}	
		
		//CastType
		a2 = (A)a;
		
		//MethodInvocationTypeArg
		Collections.<A>emptyList();
		
		return a;
	}
	
	// MethodDeclTypeParam,MethodDeclReturn,MethodDeclParam
	<M> M method(M m){
		
		//VarDeclTypeStatType or VarDeclExp
		M m2;
		
		//ForEachType and ClassInstCreationType
		for(M _ : new ArrayList<M>()){
			
		}	
		
		//CastType
		m2 = (M)a;
		
		//MethodInvocationTypeArg
		Collections.<M>emptyList();
		
		return m;
	}
}

//How Possible? MethodRefParameter or ClassInstCreationArg
//Never happen? CatchType. EnumDeclInterface, InstanceOf, TypeLiteral




			  //79					30		32
class Clazz<A,B extends A> 	extends ArrayList<A>	implements List<A>{

	//105
	A a;
	
	//198,321
	A foo(A a){
		
		//179 or 10
		A a2;
		
		//23					 115
		for(A a3 : new ArrayList<A>()){
			
		}	
		
		//8
		a2 = (A)a;
		
		//5
		Collections.<A>emptyList();
		
		return a;
	}
	
	// 15,57,138
	<M> M method(M m){
		
		//29
		M m2;
		
		//5 and 20
		for(M _ : new ArrayList<M>()){
			
		}	
		
		//11
		m2 = (M)a;
		
		//6
		Collections.<M>emptyList();
		
		return m;
	}
}