package ibpe.part;


import java.beans.PropertyChangeEvent;
import java.util.*;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.*;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.ConnectionDragCreationTool;

import ibpe.commands.CreateTransitionCommand;
import ibpe.editpolicies.GraphNodeEditPolicy;
import ibpe.model.*;


public abstract class GraphNodePart<E extends GraphNode> 
       extends AbstractDirectEditPart<E> implements NodeEditPart 
{
	//private HashMap<TransitionPart, MoveableAnchor> sourceAnchors;
	//private HashMap<TransitionPart, MoveableAnchor> targetAnchors;
	protected MoveableAnchor sourceAnchor;
	protected MoveableAnchor targetAnchor;
	protected MoveableAnchor sourceFeedbackAnchor;
	protected MoveableAnchor targetFeedbackAnchor;
	
	GraphNodePart()
	{
		//sourceAnchors = new HashMap<TransitionPart, MoveableAnchor>();
		//targetAnchors = new HashMap<TransitionPart, MoveableAnchor>();
	}

	@Override
	public DragTracker getDragTracker(Request request)
	{
		SelectionRequest req = (SelectionRequest)request;
		if (req.isControlKeyPressed() && !req.isShiftKeyPressed())
		{
			getViewer().select(this);
			return new ConnectionDragCreationTool();
		}
		else
			return super.getDragTracker(request);
	}
	
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,new GraphNodeEditPolicy());
		
		
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		super.propertyChange(evt);
		
		if (evt.getPropertyName().equals(Fork.PROPERTY_CHOICE_TYPE) ||
			evt.getPropertyName().equals(Transition.PROPERTY_TRANSITION))
		{
			refresh();
			// If choice type is changed, we need to refresh all outgoing transitions
			for (Object t : getSourceConnections())
				((TransitionPart)t).refreshVisuals();
			
		}
	}

	
	/** Returns the BoxContainer of the procedure that contains this part */
	public BoxContainerPart getRootBoxContainer()
	{
		return (BoxContainerPart)modelToPart(this.getModel().getProcedure().getBoxContainer());
	}

	
	@Override
    protected List<Transition> getModelSourceConnections() 
    {
        return getModel().getOutgoingTransitions();
    }

	
	@Override
    protected List<Transition> getModelTargetConnections() 
    {
    	return getModel().getIncomingTransitions();
    }

	
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection)
	{
		return new MoveableAnchor(this);
		/*
		if (connection instanceof TransitionPart)
		{
			TransitionPart transition = (TransitionPart) connection;
			if (sourceAnchors.containsKey(transition))
				return sourceAnchors.get(transition);
			
			MoveableAnchor anchor = new MoveableAnchor(figure);
			sourceAnchors.put(transition, anchor);
			return anchor;
		}
		return sourceAnchor;
		*/
	}
	
	
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection)
	{
		return new MoveableAnchor(this);
		/*
		if (connection instanceof TransitionPart) {
			TransitionPart transition = (TransitionPart) connection;
			if (targetAnchors.containsKey(transition))
				return targetAnchors.get(transition);
			MoveableAnchor anchor = new MoveableAnchor(figure);
			targetAnchors.put(transition, anchor);
			return anchor;
		}
		return targetAnchor;
		*/
	}
	
	
	public ConnectionAnchor getSourceConnectionAnchor(Request request)
	{
		if (request instanceof CreateConnectionRequest || request instanceof ReconnectRequest)
		{
			Point p = new Point();
			if (request instanceof CreateConnectionRequest) 
				return new MoveableAnchor(this).setLocation(((CreateTransitionCommand)((CreateConnectionRequest) request).getStartCommand()).getSourcePoint());
			else if (request instanceof ReconnectRequest) 
				p = ((ReconnectRequest) request).getLocation().getCopy();
			
			IFigure figure = getFigure();
			figure.translateToRelative(p);
			p.translate(figure.getBounds().getLocation().getNegated());
			
			return new MoveableAnchor(this).setLocation(p);
		}
		else
			return sourceFeedbackAnchor;
	}
	
	
	public ConnectionAnchor getTargetConnectionAnchor(Request request)
	{
		if (request instanceof CreateConnectionRequest || request instanceof ReconnectRequest)
		{
			Point p = new Point();
			if (request instanceof CreateConnectionRequest)
				p = ((CreateConnectionRequest) request).getLocation();
			else if (request instanceof ReconnectRequest) 
				p = ((ReconnectRequest) request).getLocation().getCopy();
			
			IFigure figure = getFigure();
			figure.translateToRelative(p);
			p.translate(figure.getBounds().getLocation().getNegated());
			
			return new MoveableAnchor(this).setLocation(p);
		}
		else
			return targetFeedbackAnchor;
	}
	

}
