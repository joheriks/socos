package ibpe.editpolicies;

import ibpe.commands.DeleteCommand;
import ibpe.model.Element;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

public class DeleteElementEditPolicy extends ComponentEditPolicy
{
	
	@Override
	protected Command getDeleteCommand(GroupRequest deleteRequest) {
		return createDeleteCommand(deleteRequest); 
	}	
	
	public Command getCommand(Request request)
	{
		if(request.getType().equals(REQ_DELETE)) {
			return new DeleteCommand((Element)getHost().getModel()); 
		}
		return null;
	}
}
