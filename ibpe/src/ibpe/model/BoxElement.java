package ibpe.model;

import java.util.*;

import org.eclipse.draw2d.geometry.*;

public abstract class BoxElement extends GraphNode 
{
	protected Rectangle box;
	

	public BoxElement()
	{
		box = new Rectangle(0,0,-1,-1);
		choiceType = CHOICE;
	}
	
	
	public Rectangle getBounds() 
	{
		return new Rectangle(box);
	}
	
	
	@Override
	public Point getPosition()
	{
		return box.getTopLeft();
	}
	
	
	@Override
	public void setPosition( Point point ) 
	{
		Rectangle newbox = box.getCopy();
		newbox.setLocation(point);
		setBounds(newbox);
	}
		

	public void setBounds( Rectangle newBox )
	{
 		Rectangle oldBox = box;
		box = newBox;
		getListeners().firePropertyChange(PROPERTY_BOUNDS, oldBox, box);
	}

	
	public void translateBounds( Point moveDelta )
	{
		setBounds(box.getCopy().translate(moveDelta));
	}

	
	/**
	 *  Return all (transitively) nested BoxElements. Irreflexive.
	 */
	public List<BoxElement> getNestedBoxElements()
	{
		List<BoxElement> retval = new ArrayList<BoxElement>();
		for (Element e : this.getDescendants())
			if (e instanceof BoxElement) retval.add((BoxElement)e);
		return retval;		
	}

	/**
	 *  Return all (transitively) enclosing BoxElements. Irreflexive.
	 */
	public List<BoxElement> getEnclosingBoxElements()
	{
		List<BoxElement> retval = new ArrayList<BoxElement>();
		for (Element e : this.getAncestors())
			if (e instanceof BoxElement) retval.add((BoxElement)e);
		return retval;		
	}
	
	/**
	 * Populates the parameters with  all arcs and forks in the transition tree 
	 * rooted at this boxelement.
	 */
	public void addArcsAndForks( Set<Transition> outArcs, Set<Fork> outForks )
	{
		List<GraphNode> graphNodes = new ArrayList<GraphNode>();
		graphNodes.add(this);
		while (!graphNodes.isEmpty())
		{
			GraphNode g = graphNodes.remove(0);
			for (Transition t : g.getOutgoingTransitions())
			{
				outArcs.add(t);
				if (t.getTarget() instanceof Fork)
				{	
					outForks.add((Fork)t.getTarget());
					graphNodes.add(t.getTarget());
				}
			}
		}
	}
	
	/** Given an input set of BoxElements, populate the supplied sets with
	 *  all arcs and forks belonging to transitions within the selection.
	 *  Does NOT include nested BoxElements (unless in the input set).
	 *  Implementation is probably rather sub-optimal.
	 */
	public static void addIncludedTransitionArcsAndForks( Collection<BoxElement> inBoxElements,
														  Set<Transition> outArcs, Set<Fork> outForks )
	{
		Set<GraphNode> graphNodes = new HashSet<GraphNode>();
		
		// collect all reachable Forks
		for (BoxElement be : inBoxElements)
		{
			graphNodes.add(be);
			for (GraphNode gn : be.getReachableNodes())
				if (gn instanceof Fork)
				{
					Fork f = (Fork)gn;
					Set<BoxElement> intersection = f.getLeafBoxElements();
					intersection.retainAll(inBoxElements);
					if (inBoxElements.contains(f.getRootBoxElement()) && !intersection.isEmpty())
					{
						outForks.add((Fork)gn);
						graphNodes.add(gn);
					}
				}
		}
	
		// add all transitions target in the node set
		for (GraphNode gn : graphNodes)
			for (Transition t : gn.getOutgoingTransitions())
				if (graphNodes.contains(t.getTarget()))
					outArcs.add(t);
			
	}
	
}
