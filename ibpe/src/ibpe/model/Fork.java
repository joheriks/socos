package ibpe.model;

import org.eclipse.draw2d.geometry.*;

import java.util.*;

public class Fork extends GraphNode 
{
	/** A Fork object represents a node in a transition tree. */

	public static final String PROPERTY_FORK_POSITION = "NodeForkPosition";
	
	protected Point position = new Point(); 
	
	public Fork( Point pos  )
	{
		super();
		setPosition(pos);
	}
	
	
	@Override
	public Point getPosition() { return position.getCopy(); }

	
	@Override
	public void setPosition( Point p ) 
	{ 
		Point oldPosition = getPosition();
		position = p.getCopy();
		getListeners().firePropertyChange(PROPERTY_FORK_POSITION,oldPosition,position);
	}
	
	/**
	 * Returns the root BoxElement of this fork. Relies on transition graphs being trees.
	 */
	public BoxElement getRootBoxElement()
	{
		GraphNode prev = getIncomingTransitions().get(0).getSource(); 
		if (prev instanceof BoxElement)
			return (BoxElement)prev;
		else 
			return ((Fork)prev).getRootBoxElement();
	}
	
	
	/**
	 * Return all leaf BoxElements for the transition. Relies on Fork graph being acyclic
	 * (transition graph should actually always be a tree).
	 */
	public Set<BoxElement> getLeafBoxElements()
	{
		Set<BoxElement> set = new HashSet<BoxElement>();
		
		for (Transition t : getOutgoingTransitions())
			if (t.getTarget() instanceof BoxElement)
				set.add((BoxElement)t.getTarget());
			else if (t.getTarget() instanceof Fork)
				set.addAll(((Fork)t.getTarget()).getLeafBoxElements());
		
		return set;
	}

	
	/** Returns whether the given fork is reachable from this one in the same transition
	 *  tree. Reflexive.
	 *  */
	public boolean isReachable( Fork f  )
	{
		if (this==f) return true;
		for (Transition t : getOutgoingTransitions() )
		{
			if (t.getTarget() instanceof Fork)
			{
				boolean b = ((Fork)t.getTarget()).isReachable(f);
				if (b) return true;
			}
		}
		
		return false;
	}
	

}