package ibpe.editpolicies;

import ibpe.commands.*;
import ibpe.model.*;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import java.util.*;


public class DeleteTransitionEditPolicy extends ConnectionEditPolicy 
{
	@Override
	protected Command getDeleteCommand(GroupRequest request)
	{
		List<Element> elems = new ArrayList<Element>();
		List<Transition> trs = new ArrayList<Transition>();
		trs.add((Transition)getHost().getModel()); 
		
		// Delete the whole subtree
		while (!trs.isEmpty())
		{
			Transition t = trs.remove(0);
			if (!elems.contains(t))
				elems.add(t);
			if (t.getTarget() instanceof Fork 
			    && !elems.contains(t.getTarget()))
			{
				elems.add(t.getTarget());
				trs.addAll(t.getTarget().getOutgoingTransitions());
			}
		}
		
		if (elems.size()>0) 
			return new DeleteCommand(elems);
		else
			return null;
	}
	
}
