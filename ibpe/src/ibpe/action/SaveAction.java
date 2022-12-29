package ibpe.action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;


public class SaveAction extends org.eclipse.gef.ui.actions.SaveAction implements IWorkbenchAction
{
	public SaveAction(IEditorPart editor)
	{
		super(editor);
		setLazyEnablementCalculation(false);
	}
	
}
