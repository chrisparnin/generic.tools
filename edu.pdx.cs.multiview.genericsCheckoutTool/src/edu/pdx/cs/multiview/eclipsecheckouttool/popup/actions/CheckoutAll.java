package edu.pdx.cs.multiview.eclipsecheckouttool.popup.actions;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.ui.model.CVSTagElement;
import org.eclipse.team.internal.ccvs.ui.operations.CVSOperation;
import org.eclipse.team.internal.ccvs.ui.operations.CheckoutSingleProjectOperation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.PluginAction;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.pdx.cs.multiview.eclipsecheckouttool.Activator;


@SuppressWarnings("restriction")
public class CheckoutAll implements IObjectActionDelegate {


	private IAction activeAction;
	private IWorkbenchPart targetPart;
	
	public CheckoutAll() {super();}
	

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.activeAction = action;
		this.targetPart = targetPart;
	}
	
	
	private List<ProjectInterval> getProjectChanges(){
		
		List<ProjectInterval> changes = new ArrayList<ProjectInterval>(); 
		
		IEditorPart activeEditor = targetPart.getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
		if(activeEditor instanceof ITextEditor){			
			ITextSelection selection = (ITextSelection)((ITextEditor)activeEditor).getSelectionProvider().getSelection();
			String lines = selection.getText();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			
			for(String line : lines.split("\r")){
				String str = line.replace("\r", "").replace("\n", "");
				String[] components = line.split(",");
				if(!str.isEmpty()){
					try {
						changes.add(new ProjectInterval(components[0],components[1],components[2],Integer.parseInt(components[3]),new Timestamp(df.parse(components[4]).getTime())));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}		
		
		return changes;
	}
	
	private ICVSRemoteResource[] getCVSProjects(){
		if(activeAction instanceof PluginAction){
			IStructuredSelection selection = (IStructuredSelection)((PluginAction) activeAction).getSelection();
			Object firstElement = selection.getFirstElement();
			if(firstElement instanceof IWorkbenchAdapter){
				IWorkbenchAdapter adapter = (IWorkbenchAdapter)firstElement;
				return (ICVSRemoteResource[])adapter.getChildren(null);
			}else if(firstElement instanceof ICVSRemoteFolder){
				ICVSRemoteFolder f = (ICVSRemoteFolder)firstElement;
				try {
					return (ICVSRemoteResource[])f.fetchChildren(null);					
				} catch (CVSException e) {
					e.printStackTrace();
				}
			}
		}
		
		return new ICVSRemoteResource[0];
	}
	
	private ICVSRemoteResource[] getRemoteResource(){
		if(activeAction instanceof PluginAction){
			IStructuredSelection selection = (IStructuredSelection)((PluginAction) activeAction).getSelection();
			Object firstElement = selection.getFirstElement();
			if(firstElement instanceof ICVSRemoteFolder){
				ICVSRemoteFolder f = (ICVSRemoteFolder)firstElement;
				return new ICVSRemoteResource[]{f};
			}
			else if(firstElement instanceof CVSTagElement){
				ICVSRepositoryLocation l = ((CVSTagElement)firstElement).getRoot();
				try {
					return l.members(null, false, new NullProgressMonitor());
				} catch (CVSException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	

	public void run(IAction action) {
		
		if(getProjectChanges().isEmpty()){
			MessageDialog.openInformation(
					new Shell(),
					"EclipseCheckoutTool Plug-in",
					"You must select some dates in an open text editor.");
			return;
		}

		ICVSRemoteResource[] resources = getRemoteResource();
		
		if(resources == null){
			MessageDialog.openInformation(
					new Shell(),
					"EclipseCheckoutTool Plug-in",
			"You must have a CVS folder selected in the CVS Repo Explorer.");
			return;
		}
		
		List<CheckoutSingleProjectOperation> checkouts = 
			new LinkedList<CheckoutSingleProjectOperation>();
		for(ProjectInterval pi : getProjectChanges()){
			try {
				pi.matchWithOneOf(resources);
				checkouts.add(pi.checkoutOperationForBefore(targetPart));						
				checkouts.add(pi.checkoutOperationForAfter(targetPart));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		perform(checkouts);
	}
	
	
	private void perform(final List<CheckoutSingleProjectOperation> checkouts) {
		
		Job job = new Job("Checking out..."){
			public IStatus run(IProgressMonitor mon){
				
				mon.beginTask("Checking out projects from CVS", checkouts.size());
				
				List<IStatus> status = new ArrayList<IStatus>();
				
				for(CheckoutSingleProjectOperation checkout : checkouts){
					try {
						checkout.execute(new SubProgressMonitor(mon,1));						
						checkForErrors(status,checkout);
					} catch (Exception e) {
						e.printStackTrace();
					}
					 if (mon.isCanceled()) return Status.CANCEL_STATUS;
				}
				
				mon.done();
				
				return new MultiStatus(	Activator.PLUGIN_ID, 
										IStatus.WARNING, 
										status.toArray(new IStatus[0]), 
										"Checkout Warnings", 
										new RuntimeException());
			}

			private void checkForErrors(List<IStatus> existing, CheckoutSingleProjectOperation checkout) throws Exception {
				Method m = CVSOperation.class.getDeclaredMethod("getErrors");
				m.setAccessible(true);
				IStatus[] status = (IStatus[]) m.invoke(checkout);
				for(IStatus s : status){
					existing.add(s);
				}
			}
		};
		
		job.setUser(true);
		job.schedule();
	}


	public void selectionChanged(IAction action, ISelection selection) {}
}

@SuppressWarnings("restriction")
class ProjectInterval{
	
	private final String module;
	private final String castChange;
	private final int delta;
	private final Timestamp start, end;
	private final String projectName;
	
	private ICVSRemoteResource resource;
	
	public ProjectInterval(String project, String module, String castChange, int delta,	Timestamp timestamp) {

		this.projectName = project;
		this.module = module;
		this.castChange = castChange;
		this.delta = delta;
		
		this.start = localTime(timestamp,-1000);
		this.end = localTime(timestamp,0);
	}

	private Timestamp localTime(Timestamp timeInUTC,int offset){
		return localTime(timeInUTC,offset,"GMT+12:00");//TODO not sure why this is the right offset
	}
	
	private Timestamp localTime(Timestamp timeInUTC, int offset, String tz) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeZone(TimeZone.getTimeZone(tz));
		cal.setTimeInMillis(timeInUTC.getTime());
		Timestamp timestamp = new Timestamp(timeInUTC.getTime()
				+ cal.get(Calendar.DST_OFFSET) + cal.get(Calendar.ZONE_OFFSET)
				+ offset);
//		System.out.println(tz + "\t" + timestamp);
		return timestamp;
	}

	private RemoteFolder remoteFolderAtTime(Timestamp t) {
		return new RemoteFolder((RemoteFolder) resource.getParent(),
														resource.getRepository(),
														resource.getRepositoryRelativePath(),
														new CVSTag(t));
	}
	
	public CheckoutSingleProjectOperation checkoutOperationForBefore(IWorkbenchPart part) {
		return checkoutOperation(part, start,"BEFORE");
	}
	
	public CheckoutSingleProjectOperation checkoutOperationForAfter(IWorkbenchPart part) {
		return checkoutOperation(part, end,"AFTER");
	}

	private CheckoutSingleProjectOperation checkoutOperation(
			IWorkbenchPart part, Timestamp time, String postfix) {

		RemoteFolder projectBefore = remoteFolderAtTime(time);
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
		String projectName = this.projectName + " " + df.format(time) + " " + resource.getName() + " " + postfix;
		
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().
						getProject(projectName);
		return new CheckoutSingleProjectOperation(part, projectBefore,
				newProject, null, false);
	}

	public void matchWithOneOf(ICVSRemoteResource[] resources) {
		for(ICVSRemoteResource r : resources){
			if(r.getName().equals(module)){
				resource = r;
				return;
			}
		}
		throw new RuntimeException("Could not find resource matching module name!");
	}	
}