package ibpe.commands;

import ibpe.model.BoxElement;
import ibpe.model.Element;
import ibpe.model.Node;
import ibpe.model.Postcondition;
import ibpe.model.Precondition;

import ibpe.part.BoxElementPart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.*;

public class BoxAddChildrenCommand extends Command /*BoxMoveCommand*/ {
	
	Point feedbackFigureLocation;
	List<Dimension> newRelLocations;
	List<Point> oldLocations;
	
	/*
	public BoxAddChildrenCommand()
	{
		feedbackFigureLocation = new Point();
		newRelLocations = new ArrayList<Dimension>();
		oldLocations = new ArrayList<Point>();
	}
	
	public void setFeedbackFigureLocation(Point feedbackFigureLocation) {
		this.feedbackFigureLocation = feedbackFigureLocation;
	}
	public boolean canExecute()
	{
		if(parts.isEmpty())
			return  false;
		else
			return  !(((Element) parent.getModel()).getParent() instanceof Postcondition || ((Element) parent.getModel()).getParent() instanceof Precondition)
			& (!(parts.get(0).getModel() instanceof Precondition) ) & (!(parts.get(0).getModel() instanceof Postcondition) );
	}
	
	public void execute()
	{
		// Store the old locations of the BoxElements
		Rectangle boxRect = parts.get(0).getModelBounds().getCopy();
		for(BoxElementPart part : parts)
		{
			oldLocations.add( part.getModelBounds().getLocation().getCopy());
			boxRect.union(part.getModelBounds());
		}
		
		Point topLeft = boxRect.getLocation();
		// newRelLocations stores the locations of all BoxElements relative to
		// the top left corner of the union of all BoxElements.
		for(BoxElementPart part : parts)
		{
			Point location = part.getModelBounds().getLocation().getCopy();
			newRelLocations.add( location.getDifference(topLeft) );
		}
		
		int i = 0;
		for (BoxElementPart part : parts)
		{
			// The new position for a BoxElement is the position of the feedback figure
			// translated with the BoxElement's corresponding relative location.
			Point location = feedbackFigureLocation.getTranslated(newRelLocations.get(i));
			((BoxElement)part.getModel()).setLocation( location );
			((Node)parent.getModel()).add((Node)part.getModel());
			i++;
		}
	}
	
	public void undo()
	{
		int i = 0;
		for (BoxElementPart part : parts)
		{
			((Node)parent.getModel()).remove(((Node)part.getModel()));
			((BoxElement)part.getModel()).getBounds().setLocation(oldLocations.get(i));
			i++;
		}
		oldLocations.clear();
	}
	*/
}
