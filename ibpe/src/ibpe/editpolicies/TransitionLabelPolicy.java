package ibpe.editpolicies;

import ibpe.commands.*;
import ibpe.model.*;
import ibpe.part.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.*;
import org.eclipse.gef.requests.*;


public class TransitionLabelPolicy extends GraphicalEditPolicy
{
	
	@Override
	public boolean understandsRequest(Request request) 
	{
		// Only allowed to be moved within the container.
		return REQ_MOVE.equals(request.getType());
	}
	
	
	@Override
	public Command getCommand(Request request)
	{
		// Handle move command.
		
		if (!REQ_MOVE.equals(request.getType()))
			return null;
			
		ChangeBoundsRequest r = (ChangeBoundsRequest)request;
		
		Transition t = ((TransitionPart)getHost().getParent()).getModel();
		return new MoveTransitionLabelCommand(t,t.getLabelPoint().getTranslated(r.getMoveDelta()));
	}

	
	@Override
	public EditPart getTargetEditPart(Request request) 
	{
		return getHost();
	}


}
