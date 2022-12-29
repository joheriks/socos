package ibpe.action;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

public class CutAction extends CopyAction
{
	public CutAction(IWorkbenchPart part) 
	{
		super(part);
	}
	
	@Override 
	protected void init()
	{ 
		super.init(); 
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages(); 
		setText("Cut"); 
		setId(ActionFactory.CUT.getId()); 
		setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT)); 
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT)); 
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED)); 
		setEnabled(false); 
	} 
	

	@Override
	public boolean calculateEnabled()
	{
		DeleteAction da = (DeleteAction)getEditor().getActionRegistry().getAction(ActionFactory.DELETE.getId());
		return super.calculateEnabled() && da.calculateEnabled();
	}

	
	@Override 
	public void run()
	{
		super.run();
		DeleteAction da = (DeleteAction)getEditor().getActionRegistry().getAction(ActionFactory.DELETE.getId());
		da.run();
	}
}
