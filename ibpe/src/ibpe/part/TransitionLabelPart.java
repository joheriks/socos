package ibpe.part;

import ibpe.editpolicies.*;
import ibpe.figure.*;

import org.eclipse.gef.EditPolicy;


public class TransitionLabelPart extends TextContainerPart 
{
	@Override
	protected TextContainerFigure createFigure()
	{
		return new TextContainerFigure(4,4,true);
	}

	
	@Override
	protected void createEditPolicies()
	{
		super.createEditPolicies();
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new TransitionLabelPolicy());
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableEditPolicyOptionalHandles(false));
	}
		
}
