package ibpe.action;

import ibpe.part.*;

import java.util.List;

import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.internal.*;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * An action to delete selected objects.
 */
@SuppressWarnings("restriction")
public class DeleteAction extends IBPESelectionAction {

	/**
	 * Constructs a <code>DeleteAction</code> using the specified part.
	 * @param part The part for this action
	 */
	public DeleteAction(IWorkbenchPart part)
	{
		super(part);
		setLazyEnablementCalculation(false);
	}
	
	/**
	 * Returns <code>true</code> if the selected objects can
	 * be deleted.  Returns <code>false</code> if there are
	 * no objects selected or the selected objects are not
	 * {@link EditPart}s.
	 * @return <code>true</code> if the command should be enabled
	 */
	protected boolean calculateEnabled()
	{
		Command cmd = createDeleteCommand();
		return cmd != null && cmd.canExecute();
	}
	
	/**
	 * Create a command to remove the selected objects.
	 * @param objects The objects to be deleted.
	 * @return The command to remove the selected objects.
	 */
	public Command createDeleteCommand()
	{
		List<AbstractIBPEditPart<?>> parts = getSelectedIBPParts();
		CompoundCommand compoundCmd = new CompoundCommand();
		
		// If a transition is selected
		if (getSelectedTransition()!=null)
			return getSelectedTransition().getCommand(new GroupRequest(RequestConstants.REQ_DELETE));
		
		// Make a compound of delete-commands.
		for (AbstractIBPEditPart<?> p : parts)
		{
			Command cmd = p.getCommand(new GroupRequest(RequestConstants.REQ_DELETE));
			if (cmd != null) compoundCmd.add(cmd);
		}
		
		return compoundCmd;
	}
	
	/**
	 * Initializes this action's text and images.
	 */
	protected void init()
	{
		super.init();
		
		setText(GEFMessages.DeleteAction_Label);
		setToolTipText(GEFMessages.DeleteAction_Tooltip);
		setId(ActionFactory.DELETE.getId());
		
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		setEnabled(false);
	}

	/**
	 * Performs the delete action on the selected objects.
	 */
	public void run()
	{
		
		Command cmd = createDeleteCommand(); 
		if (cmd!=null) execute(cmd);
	}

}
