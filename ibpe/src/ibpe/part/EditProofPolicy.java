package ibpe.part;

import ibpe.commands.UpdateProofCommand;
import ibpe.model.Proof;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

public class EditProofPolicy extends DirectEditPolicy
{

	@Override
	protected Command getDirectEditCommand(DirectEditRequest request)
	{
		String text=(String)request.getCellEditor().getValue();
		return new UpdateProofCommand((Proof)getHost().getModel(), text);
	}

	@Override
	protected void showCurrentEditValue(DirectEditRequest request)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void storeOldEditValue(DirectEditRequest request)
	{		
		// TODO
	}

	@Override
	protected void revertOldEditValue(DirectEditRequest request)
	{
		// TODO
	}

}
