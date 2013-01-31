package edu.ncsu.csc.generics.declarations.popup.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class AnalyzeDeclarations implements IObjectActionDelegate {

	/*
	 * The user selection
	 */
	protected ISelection selection;
	

	/*
	 * CompilationUnits found so far
	 */
	protected List<ICompilationUnit> unitsToInspect;
	
	/*
	 * The elements found  so far
	 */
	private Set<IJavaElement> elements;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {/*do nothing*/}

	public void selectionChanged(IAction action, ISelection s) {
		this.selection =  s;
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
		elements = new HashSet<IJavaElement>();
		unitsToInspect = new LinkedList<ICompilationUnit>();
		
		try {
			ProgressMonitorDialog d = new ProgressMonitorDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
					);
			
			d.run(true, true, getRunnable());
			
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			displayResults();
		}
	}

	private IRunnableWithProgress getRunnable() {
		return new IRunnableWithProgress(){

			public void run(IProgressMonitor m) throws InvocationTargetException, InterruptedException {
				if(selection instanceof IStructuredSelection){
					IStructuredSelection sel = (IStructuredSelection)selection;
					
					m.subTask("Gathering Classes");
					
					Iterator<?> iter = sel.iterator();			
					while(iter.hasNext()){						
						Object o = iter.next();
						if(o instanceof IParent){							
							IParent parent = (IParent)o;							
							traverse(parent,m);
						}
					}
					
					m.beginTask("Inspecting Classes",unitsToInspect.size());
					
					for(ICompilationUnit unit : unitsToInspect){
						if(m.isCanceled())
							return;
						
						m.subTask("Inspecting "+unit.getElementName());
						processCU(unit);		
						m.worked(1);
					}
					
				}
			}
		};
	}
	
	private void displayResults() {
		for(IJavaElement method : elements)
			displayInEditor(method);
	}

	
	/**
	 * Recursively looks through each IParent and then does something
	 * on the children
	 * 
	 * @param parent
	 */
	void traverse(IParent parent, IProgressMonitor monitor) {
		
		if(monitor.isCanceled())
			return;
		
		if(parent instanceof JarPackageFragmentRoot)
			return;
		
		if(parent instanceof IPackageFragment)
			monitor.subTask("Looking through " + ((IPackageFragment)parent).getElementName());
		
		if(parent instanceof ICompilationUnit){
			unitsToInspect.add((ICompilationUnit)parent);
		}else{
			try {
				for(IJavaElement element : parent.getChildren()){
					if(element instanceof IParent)
						traverse((IParent)element,monitor);
				}
	
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Displays m for the user
	 * 
	 * @param m
	 */
	protected void display(IJavaElement m) {
		
		elements.add(m);
	}

	private void displayInEditor(IJavaElement m) {
		showInEditor(m);
	}	
	
	public static IEditorPart showInEditor(IJavaElement m){
		return showInEditor(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage(),m);
	}
	
	private static IEditorPart showInEditor(IWorkbenchPage page, IJavaElement m){
		try {
        	IEditorPart p = JavaUI.openInEditor(m);
	        page.activate(p);
	        JavaUI.revealInEditor(p,m);
	        return p;
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
	}		


	protected void processCU(ICompilationUnit t){
		 ASTParser parser = ASTParser.newParser(AST.JLS3);
		 parser.setResolveBindings(true);
		 parser.setSource(t);
			
		 CompilationUnit node = (CompilationUnit)parser.createAST(new NullProgressMonitor());
		 
		 GenericDeclVisitor v = new GenericDeclVisitor();
		 TestVisitor test = new TestVisitor();
		 
		 node.accept(v);
		 node.accept(test);
		 
		 test.compareTo(v.refsFound,t,node);
		 
		 for(GenericBinding gb : v.results){
			 System.out.println(gb);
		 }
	}
}