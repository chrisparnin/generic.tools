package edu.ncsu.csc.heteroAddFinder.popup.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
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
public abstract class FindInterestingMethods 
						implements IObjectActionDelegate {
	
	/*
	 * The user selection
	 */
	protected ISelection selection;

	/*
	 * CompilationUnits found so far
	 */
	protected List<ICompilationUnit> unitsToInspect;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {/*do nothing*/}

	public void selectionChanged(IAction action, ISelection s) {
		this.selection =  s;
	}
	
	
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
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
					for(int i = 0; i<unitsToInspect.size(); i++){
						
						if(m.isCanceled())
							return;
						
						ICompilationUnit unit = unitsToInspect.get(i);
						m.subTask("Inspecting "+unit.getElementName());
						try {
							processCU(unit);
						} catch (IOException e) {
							e.printStackTrace();
						}		
						m.worked(1);
					}
					
				}
			}
		};
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
	 * Processes a type
	 * 
	 * @param t
	 * @throws IOException 
	 */
	protected abstract void processCU(ICompilationUnit t) throws IOException;
	
	
	
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
		
	
}
