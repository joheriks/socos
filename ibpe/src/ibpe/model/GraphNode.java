package ibpe.model;

import java.util.*;

import org.eclipse.draw2d.geometry.Point;

public abstract class GraphNode extends Node 
{
	public static final String PROPERTY_CHOICE_TYPE = "NodeChoiceType";
	public static final String CHOICE = "CHOICE";
	public static final String IF = "IF";
	protected String choiceType = CHOICE;
	

	
	public List<Transition> getIncomingTransitions()
	{
		List<Transition> l = new ArrayList<Transition>();
		if (getProcedure()!=null)
			for (Transition t : getProcedure().getTransitions())
				if (t.getTarget()==this)
					l.add(t);
		return l;
	}
	
	
	public List<Transition> getOutgoingTransitions()
	{
		List<Transition> l = new ArrayList<Transition>();
		if (getProcedure()!=null)
			for (Transition t : getProcedure().getTransitions())
				if (t.getSource()==this)
					l.add(t);
		return l;
	}
	
	
	protected void findReachableNodes( Set<GraphNode> visited )
	{
		for (Transition t : getOutgoingTransitions())
			if (!visited.contains(t.getTarget()))
			{
				visited.add(t.getTarget());
				t.getTarget().findReachableNodes(visited);
			}
	}

	/** Return all reachable nodes in the directed graph. */
	public Set<GraphNode> getReachableNodes()
	{
		Set<GraphNode> set = new HashSet<GraphNode>();
		findReachableNodes(set);
		return set;
	}
	
	
	/** Returns the procedure to which this BoxElement belongs. */
	public Procedure getProcedure()
	{
		return (Procedure)getParentOfType(Procedure.class);
	}
	
	
	public String getChoiceType()
	{
		return choiceType;
	}

	
	public void setChoiceType( String s )
	{
		String oldChoiceType = choiceType;
		choiceType = s;
		getListeners().firePropertyChange(PROPERTY_CHOICE_TYPE,oldChoiceType,choiceType);
	}
	
	
	
	
	

	public abstract Point getPosition();
	
	public abstract void setPosition( Point p );
	
}
