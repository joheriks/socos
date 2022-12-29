package ibpe.part;

import ibpe.commands.*;
import ibpe.editpolicies.*;
import ibpe.figure.*;
import ibpe.model.*;
import ibpe.part.BoxContainerPart.Direction;

import java.beans.PropertyChangeEvent;
import java.util.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.requests.*;
import org.eclipse.gef.tools.ConnectionDragCreationTool;


public abstract class BoxElementPart<E extends BoxElement>
       extends GraphNodePart<E>
       implements Comparable<BoxElementPart<?>> 
{
	
	private List<BoxElementPart<?>>[] listParents; // References to the second level LinkedLists containing this part
	private Direction moveDirection;
	private Point moveDelta;

	@SuppressWarnings("unchecked")
	public BoxElementPart()
	{
		listParents = new List[4];
		moveDelta = new Point();
	}
	
	@Override
	public List<Element> getModelChildren()
	{
		return getModel().getChildren();
	}
	
	// Meta-method. Subclasses should return the correct Figure class to instantiate 
	protected abstract Class<? extends Figure> getFigureClass(); 
	
	
	@Override
	protected Figure createFigure() 
	{
		Figure contentPane = null;

		try	{ contentPane = getFigureClass().newInstance(); }
		catch (InstantiationException e) { assert false;	}
		catch (IllegalAccessException e) { assert false; }
		
		/*
		targetAnchor = new MoveableAnchor(contentPane);
		sourceAnchor = new MoveableAnchor(contentPane);
		targetFeedbackAnchor = new MoveableAnchor(contentPane);
		sourceFeedbackAnchor = new MoveableAnchor(contentPane);
		*/
		
		return contentPane;
	}
	
	public Direction getMoveDirection() {
		return moveDirection;
	}

	public void setMoveDirection(Direction moveDirection) {
		this.moveDirection = moveDirection;
	}
	
	public void setParent(int Direction, List<BoxElementPart<?>> listParent) {
		listParents[Direction] = listParent;
	}
	
	public List<BoxElementPart<?>> getParent(int Direction) {
		return listParents[Direction];
	}
	
	public void propertyChange(PropertyChangeEvent evt) 
	{
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(Node.PROPERTY_ADD)		|| 
			evt.getPropertyName().equals(Node.PROPERTY_REMOVE)	|| 
			evt.getPropertyName().equals(Node.PROPERTY_BOUNDS)) 
		{
			refresh();
			if (getParent() instanceof BoxContainerPart)
			{
				BoxContainerPart parent = (BoxContainerPart) getParent();
				
				// this updates the EditPart's place in BoxContainerPart.system
				parent.removeEditPart(this);
				parent.addEditPart(this);
			}
			((AbstractIBPEditPart<?>)getParent()).propertyChange(evt);
		}
	}
	
	@Override
	protected void createEditPolicies()
	{
		super.createEditPolicies();
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,new RoundedSelectPolicy());
	}
	
	public Rectangle getModelBounds() {
		return getModel().getBounds().getCopy();
	}
	
	public Point getMoveDelta() {
		return moveDelta;
	}

	public void setMoveDelta(Point moveDelta) {
		this.moveDelta = moveDelta;
	}

	public void translateMoveDelta(Point moveDelta) {
		this.moveDelta.translate(moveDelta);
	}
	
	public Rectangle getMovedBounds() {
		return ((BoxElement)getModel()).getBounds().getTranslated(moveDelta);
	}
	
	public Rectangle getMovedArea() {
		return getMovedBounds().union(getModelBounds());
	}
	
	public int compareTo(BoxElementPart<?> o) {
		Rectangle reference = getMovedArea();
		Rectangle compare = o.getMovedArea();
		
		switch (moveDirection) {
		case LEFT_TO_RIGHT:
			if (reference.x < compare.x) {
				return -1;
			} else if (reference.x > compare.x) {
				return 1;
			} else {
				return 0;
			}
		case RIGHT_TO_LEFT:
			if (reference.x + reference.width > compare.x + compare.width) {
				return -1;
			} else if (reference.x + reference.width < compare.x + compare.width) {
				return 1;
			} else {
				return 0;
			}
		case TOP_TO_BOTTOM:
			if (reference.y < compare.y) {
				return -1;
			} else if (reference.y > compare.y) {
				return 1;

			} else {
				return 0;
			}
		case BOTTOM_TO_TOP:
			if (reference.y + reference.height > compare.y + compare.height) {
				return -1;
			} else if (reference.y + reference.height < compare.y + compare.height) {
				return 1;
			} else {
				return 0;
			}
		}
		return 0;
	}
	

	@Override
	protected void refreshVisuals()
	{
		super.refreshVisuals();
		
		if (getFigure().getParent() instanceof BoxContainerFigure && getModelBounds() != null)
			getFigure().getParent().setConstraint(getFigure(),getModelBounds());
	}
    
}
