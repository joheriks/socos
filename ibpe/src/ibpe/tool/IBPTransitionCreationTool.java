package ibpe.tool;

import ibpe.commands.CreateTransitionCommand;
import ibpe.model.Fork;
import ibpe.model.IFChoice;
import ibpe.part.*;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.tools.ConnectionCreationTool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Cursor;

import java.util.*;


public abstract class IBPTransitionCreationTool extends ConnectionCreationTool 
{
	abstract protected Fork createFork();

	@Override
	protected boolean handleButtonDown(int button) 
	{
		if (isInState(STATE_CONNECTION_STARTED) && getCurrentInput().isShiftKeyDown()) {
			return handleCreateBendpoint();
		}
		if (button == 1 && stateTransition(STATE_CONNECTION_STARTED, STATE_TERMINAL))
			return handleCreateConnection();

		super.handleButtonDown(button);
		
		if (isInState(STATE_CONNECTION_STARTED))
			//Fake a drag to cause feedback to be displayed immediately on mouse down.
			handleDrag();
		return true;
	}
	
	
	@Override
	protected boolean handleKeyDown( KeyEvent key )
	{
		boolean b = super.handleKeyDown(key); 
		if (key.keyCode==SWT.SHIFT)
		{
			getTargetRequest().getExtendedData().put("bendpointModifier",Boolean.TRUE);
			if (getTargetEditPart()!=null)
				getTargetEditPart().showTargetFeedback(getTargetRequest());
		}
		return b;
	}
	

	@Override
	protected boolean handleKeyUp( KeyEvent key )
	{
		boolean b = super.handleKeyUp(key); 
		if (key.keyCode==SWT.SHIFT)
		{
			getTargetRequest().getExtendedData().put("bendpointModifier",Boolean.FALSE);
			if (getTargetEditPart()!=null)
				getTargetEditPart().showTargetFeedback(getTargetRequest());
		}
		return b;
	
	}

	
	private boolean handleCreateBendpoint() 
	{
		if (getTargetRequest() instanceof CreateConnectionRequest && 
			((CreateConnectionRequest)getTargetRequest()).getStartCommand() instanceof CreateTransitionCommand) 
		{
			CreateConnectionRequest req = (CreateConnectionRequest)getTargetRequest();
			BoxContainerPart origin = ((GraphNodePart<?>)req.getSourceEditPart()).getRootBoxContainer();
			Point p = req.getLocation().getCopy();
			origin.getFigure().translateToRelative(p);
			p = origin.fromAbsolute(origin.snapToGrid(p));
			if (!req.getExtendedData().containsKey("bendpoints"))
				req.getExtendedData().put("bendpoints",new ArrayList<Point>());
			((List<Point>)req.getExtendedData().get("bendpoints")).add(p);
		}
		return false;
	}
	
	
	@Override
	protected void handleFinished()
	{
		if (getCommand()!=null && getCommand() instanceof CreateTransitionCommand)
		{
			CreateTransitionCommand c = (CreateTransitionCommand)getCommand();
			EditPart p = (EditPart)getCurrentViewer().getEditPartRegistry().get(c.t);
			p.getViewer().select(p);
		}
		super.handleFinished();
		getDomain().loadDefaultTool();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected Request getSourceRequest() 
	{
		Request req = super.getSourceRequest();
		req.getExtendedData().put("bendpointModifier", (Boolean)getCurrentInput().isShiftKeyDown());
		req.getExtendedData().put("fork",createFork());
		return req;
	}
		
	
	@Override
	protected EditPartViewer.Conditional getTargetingConditional() {
		return new EditPartViewer.Conditional() {
			public boolean evaluate(EditPart editpart) {
				return editpart.getTargetEditPart(getTargetRequest())!=null;
				/*
				if (!(editpart instanceof AbstractIBPEditPart<?>))
					return false;
				AbstractIBPEditPart<?> part = (AbstractIBPEditPart<?>)editpart;
				
				Point p = getLocation().getCopy();
				part.getFigure().translateToRelative(p);
				System.out.println(p);
				return part.getFigure().getBounds().contains(p);*/
			}
		};
	}

	
	@Override
	protected Cursor calculateCursor() 
	{
		return (getCurrentInput().isShiftKeyDown()) ? getDefaultCursor() : super.calculateCursor(); 
	}
	
	
}
