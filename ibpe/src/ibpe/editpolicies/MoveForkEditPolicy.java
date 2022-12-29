package ibpe.editpolicies;

import java.util.ArrayList;
import java.util.List;

import ibpe.commands.*;
import ibpe.figure.*;
import ibpe.io.SequenceFragment;
import ibpe.model.*;
import ibpe.part.*;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.*;
import org.eclipse.gef.requests.*;


public class MoveForkEditPolicy extends NonResizableEditPolicyOptionalHandles
{
	
	public MoveForkEditPolicy()
	{
		super(false);
		setDragAllowed(true);
	}
	
	
	@Override
	public ForkPart<?> getHost()
	{
		return (ForkPart<?>)super.getHost();
	}
	
	
	@Override
	public Command  getMoveCommand( ChangeBoundsRequest request ) 
	{
		BoxContainerPart root = getHost().getRootBoxContainer();
		final Fork model = getHost().getModel();
		Point pos = root.snapToGrid(root.toAbsolute(getHost().getModel().getPosition().getTranslated(request.getMoveDelta())));
		final Point modelpos = root.fromAbsolute(pos);
		return new MoveForkCommand(model,modelpos);
	}
	
	
	@Override
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) 
	{
		IFigure feedback = getDragSourceFeedbackFigure();
		
		PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
		getHostFigure().translateToAbsolute(rect);
		rect.translate(request.getMoveDelta());
		BoxContainerPart root = getHost().getRootBoxContainer();
		Point pos = root.toAbsolute(getHost().getModel().getPosition().getTranslated(request.getMoveDelta()));
		pos = root.snapToGrid(pos);
		rect.setLocation(pos);
		rect.translate(-rect.width/2,-rect.height/2);
		feedback.setBounds(rect);
	}
	
}
