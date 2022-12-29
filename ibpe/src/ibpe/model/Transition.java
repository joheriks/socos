package ibpe.model;

import java.util.*;
import org.eclipse.draw2d.geometry.*;


public class Transition extends Node
{
	/**
	 * Objects of this type represent a transition arc, i.e. an arrow between two
	 * GraphNodes.
	 */
	public static final String PROPERTY_TRANSITION = "NodeTransition";
	
	private GraphNode source,target;
	private Point sourcePoint; // in source's coordinate system 
	private Point targetPoint; // in target's coordinate system
	private TextContainer label;
	private Point labelPoint;
	private List<Point> waypoints;

	/** Initialize transition with source and target points, waypoints and label 
	 * position and contents. Source and target both start as null.
	 */
	public Transition( Point sp, Point tp, List<Point> wps, Point lp, List<Element> decls )
	{
		sourcePoint = sp;
		targetPoint = tp;
		waypoints = new ArrayList<Point>();
		for (Point wp : wps) waypoints.add(wp);

		source = null;
		target = null;
		
		add(label=new TextContainer(this));
		for (Element decl : decls)
			label.add(decl);

		labelPoint = lp;
	}

	
	public GraphNode getSource()
	{
		return source;
	}
	
	
	public GraphNode getTarget()
	{
		return target;
	}
	
	
	/*
	public TextContainer getLabel()
	{
		return label;
	}
	*/
		
	
	public void setSource(GraphNode newsource)
	{
		GraphNode oldsource = source;
		source = newsource;
		
		// notify observers of old source about the change
		if (oldsource!=null)
			oldsource.getListeners().firePropertyChange(PROPERTY_TRANSITION, null, this);

		// notify observers of new source about the change
		if (source!=null && source!=oldsource)
			source.getListeners().firePropertyChange(PROPERTY_TRANSITION, null, this);
	}
	
	
	public void setTarget(GraphNode newtarget)
	{
		GraphNode oldtarget = target;
		target = newtarget;
		
		// notify observers of old target about the change
		if (oldtarget!=null)
			oldtarget.getListeners().firePropertyChange(PROPERTY_TRANSITION, null, this);
		
		// notify observers of new target about the change
		if (target!=null && target!=oldtarget)
			target.getListeners().firePropertyChange(PROPERTY_TRANSITION, null, this);
	}
	
	
	public List<TextRow> getStatements()
	{
		return label.getChildrenOfType(TextRow.class);
	}
	
	
	public void addPoint(int i, Point p)
	{
		waypoints.add(i, p.getCopy());
		getListeners().firePropertyChange(PROPERTY_TRANSITION, null, null);
	}
	
	
	public Point getPoint(int index)
	{
		return waypoints.get(index).getCopy();
	}

	
	public void removePoint( int index )
	{
		waypoints.remove(index);
		getListeners().firePropertyChange(PROPERTY_TRANSITION, null, null);
	}

	
	public List<Point> getWaypoints() 
	{
		ArrayList<Point> pts = new ArrayList<Point>();
		for (Point p : waypoints) pts.add(p.getCopy());
		return pts;
	}
	
	
	public void setWaypoints(List<Point> points) 
	{
		waypoints.clear();
		for (Point p : points) waypoints.add(p.getCopy());
		getListeners().firePropertyChange(PROPERTY_TRANSITION, null, null);
	}
	

	public void moveSourceAnchor(Point moveDelta)
	{
		if (sourcePoint != null)
			sourcePoint.translate(moveDelta);
		getListeners().firePropertyChange(PROPERTY_TRANSITION, null, null);
	}
	
	
	public void moveTargetAnchor(Point moveDelta)
	{
		if (targetPoint != null)
			targetPoint.translate(moveDelta);
		getListeners().firePropertyChange(PROPERTY_TRANSITION, null, null);
	}

	
	public void setLabelPoint( Point p )
	{
		assert p!=null;
		labelPoint = p.getCopy();
		getListeners().firePropertyChange(PROPERTY_TRANSITION, null, null);
	}

	
	public Point getLabelPoint() 
	{
		return labelPoint.getCopy();
	}

	
	public void setSourcePoint( Point p )
	{
		sourcePoint = (p==null ? null : p.getCopy());
		getListeners().firePropertyChange(PROPERTY_TRANSITION, null, null);
	}

	
	public Point getSourcePoint() 
	{
		return sourcePoint==null ? null : sourcePoint.getCopy();
	}
	

	public void setTargetPoint( Point p )
	{
		targetPoint = (p==null ? null : p.getCopy());
		getListeners().firePropertyChange(PROPERTY_TRANSITION, null, null);
	}

	
	public Point getTargetPoint() 
	{
		return targetPoint==null ? null : targetPoint.getCopy();
	}
	

}
