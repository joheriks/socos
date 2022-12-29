package ibpe.commands;

import ibpe.model.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.*;


public class CreateTransitionCommand extends Command 
{
	private List<Point> waypoints = new ArrayList<Point>();
	private GraphNode source;
	private GraphNode target;
	private Point sourcePoint;
	private Point targetPoint;
	private Point labelPoint;

	public Transition t;

	
	public void setSource( GraphNode gn )
	{
		source = gn;
	}

	public void setTarget( GraphNode gn )
	{
		target = gn;
	}

	@Override
	public boolean canExecute()
	{
		return source != null && 
			   target != null &&
			   labelPoint != null;/* &&
			   source.getProcedure()==target.getProcedure() &&
			   !(target instanceof Precondition || source instanceof Postcondition);*/
	}
	
	@Override
	public void execute()
	{
		assert canExecute();
		
		t = new Transition(sourcePoint,targetPoint,waypoints,labelPoint,new ArrayList<Element>());
		source.getProcedure().add(t);
		t.setSource(source);
		t.setTarget(target);
		// TODO: handle the source==target case in an editpolicy
		/*if (source == target && bendpoints.isEmpty()) {
			// If we're making a transition with the same source as target, create default bendpoints
			Point source = sourcePoint.getTranslated(bounds.getLocation());
			Point target = targetPoint.getTranslated(bounds.getLocation());
	        
	        t.setPoints(DefaultBendpointCreator.getPoints(bounds, source, target));
		} else {
			t.setPoints(bendpoints);
		}*/
		
		//source.addSourceTransition(t);
		//target.addTargetTransition(t);
		//target.setSource(t);
	}
	
	@Override
	public void undo()
	{	
		t.setSource(null);
		t.setTarget(null);
		source.getProcedure().remove(t);
		/*
		source.removeSourceTransition(t);
		target.removeTargetTransition(t);
		*/
	}

	public void setSourcePoint( Point p ) { sourcePoint = p;	}
	public void setTargetPoint( Point p ) { targetPoint = p;	}
	public void setLabelPoint( Point p) { labelPoint = p;	}
	public void setBendpoints( List<Point> wps ) {	waypoints = wps; }

	public GraphNode getSource() { return source; }
	public GraphNode getTarget() { return target; }
	public Point getSourcePoint() {	return sourcePoint; }
	public Point getTargetPoint() {	return targetPoint;	}
	
	public List<Point> getBendpoints() { return new ArrayList<Point>(waypoints); }
	
	public void addBendpoint(Point p) { waypoints.add(p); }
}