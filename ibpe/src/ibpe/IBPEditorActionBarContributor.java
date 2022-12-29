package ibpe;

import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class IBPEditorActionBarContributor extends ActionBarContributor 
{
	@Override
	protected void declareGlobalActionKeys() {}

	
	@Override
	protected void buildActions()
	{
		IWorkbenchWindow window = getPage().getWorkbenchWindow();

		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		addRetargetAction((RetargetAction) ActionFactory.DELETE.create(window));
		addRetargetAction((RetargetAction) ActionFactory.COPY.create(window));
		addRetargetAction((RetargetAction) ActionFactory.CUT.create(window));
		addRetargetAction((RetargetAction) ActionFactory.PASTE.create(window));
		
		RetargetAction r = new RetargetAction("CHECK_FILE",null);
		r.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/verify.png"));
		addRetargetAction(r);
		
		r = new RetargetAction("DEBUG_PLAY",null);
		r.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/debug.png"));
		addRetargetAction(r);
		
		r = new RetargetAction("DEBUG_STEPINTO",null);
		//r.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD ));
		r.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/debug-step-into.png"));
		addRetargetAction(r);
		
		r = new RetargetAction("DEBUG_STEPOVER",null);
		r.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/debug-step-over.png"));
		addRetargetAction(r);
		
		r = new RetargetAction("DEBUG_STOP",null);
		r.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/stop.png"));
		addRetargetAction(r);

		r = new RetargetAction("DEBUG_PAUSE",null);
		r.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/pause.png"));
		addRetargetAction(r);
		
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());
	}
	
	
	@Override
	public void contributeToToolBar( IToolBarManager toolBarManager )
	{
		toolBarManager.add(getAction(ActionFactory.UNDO.getId()));
		toolBarManager.add(getAction(ActionFactory.REDO.getId()));
		toolBarManager.add(getAction(ActionFactory.DELETE.getId()));
		toolBarManager.add(getAction(ActionFactory.COPY.getId()));
		toolBarManager.add(getAction(ActionFactory.CUT.getId()));
		toolBarManager.add(getAction(ActionFactory.PASTE.getId()));
		
		toolBarManager.add(new Separator());
		
		toolBarManager.add(getAction("CHECK_FILE"));
		toolBarManager.add(getAction("DEBUG_PLAY"));
		toolBarManager.add(getAction("DEBUG_STEPINTO"));
		toolBarManager.add(getAction("DEBUG_STEPOVER"));
		toolBarManager.add(getAction("DEBUG_STOP"));
		toolBarManager.add(getAction("DEBUG_PAUSE"));	
		
		toolBarManager.add(new Separator());

		toolBarManager.add(getAction(GEFActionConstants.ZOOM_IN));
		toolBarManager.add(getAction(GEFActionConstants.ZOOM_OUT));
		toolBarManager.add(new ZoomComboContributionItem(getPage()));

		toolBarManager.add(new Separator());
	}

	
	@Override
	public void contributeToMenu(IMenuManager menuManager)
	{
		super.contributeToMenu(menuManager);
		MenuManager viewMenu = new MenuManager("&View");
		viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
		viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
		menuManager.insertAfter(IWorkbenchActionConstants.WINDOW_EXT,viewMenu);
	}
}
