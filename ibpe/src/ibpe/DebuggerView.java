package ibpe;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;


public class DebuggerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "fi.imped.socos.ibpe.io.DebuggerView";

	public static DebuggerView instance;
	
	public TreeViewer viewer;
	private ViewContentProvider content;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	protected ArrayList stack;
	protected String s1;
	protected Object[] lastchild;
	
	public Display display;
	
	class ChildElement {
		public String text;
		public HashMap parent;
		ChildElement( String t, HashMap p ) { text = t; parent = p; }
	}
	
	
	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {

		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	
		}
		
		public void dispose() {
	
		}	
		
		public Object[] getElements(Object parent) {
			/*
			if (parent.equals(getViewSite())) {
				if (stack==null)
					return new Object[0];
				else
					return stack.toArray();

			}*/
			return getChildren(parent);
		}
		
		public Object getParent(Object child) {
			if (child instanceof ChildElement) return ((ChildElement)child).parent;
			else if (child instanceof HashMap) return getViewSite();
			else return null;
		}
		
		public Object [] getChildren(Object parent) {
	
			if (parent instanceof HashMap)
			{
				HashMap p = (HashMap)parent;
				ArrayList vars = (ArrayList)p.get("vars");
				ArrayList vals = (ArrayList)p.get("vals");
				Object[] children = new ChildElement[vars.size()];
				for (int i=0; i<vars.size(); i++)
				{
					children[i] = new ChildElement((String)vars.get(i) + " = '" + (String)vals.get(i) + "'",
											   p);		
				}
				
				return children;
			} 
			else if (parent.equals(getViewSite()))
			{
				if (stack==null)
					return new Object[0];
				return stack.toArray();
			}
		
			return new Object[0];
		}
		
		public boolean hasChildren(Object parent) {
		
			return !(parent instanceof ChildElement);
		}

	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			if (obj instanceof HashMap)
			{
				return (String)((HashMap)obj).get("name");
			}
			else if (obj instanceof ChildElement)
			{
				return ((ChildElement)obj).text;
			}
			return obj.toString();
		}
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public DebuggerView() {
   
       instance = this;
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		content = new ViewContentProvider();
		viewer.setContentProvider(content);
	//	viewer.expandAll();
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		
		
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "fi.imped.socos.IBPE.viewer");
		display = parent.getDisplay();
		 
	}

	public void updateTree( ArrayList stack ) {
		
		this.stack = stack;	
		
		viewer.refresh();
		if(stack!=null )
		{
			viewer.setExpandedState(stack.get(0), true);
			for (Object o : stack.subList(1,stack.size()))
				viewer.setExpandedState(o, false);
		}
	}
	
	
	/*
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DebuggerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
				
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
				
			}
		};
		
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
				
			}
		});
	}
	
	private void addTreeListenerAction(){
		viewer.addTreeListener(new ITreeViewrListener(){
			public void TreeListener(){
				
			}
		});
	}
	
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"IBP Debugger View",
			message);
	}
*/
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}