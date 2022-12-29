package ibpe.commands;

import ibpe.model.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.Command;

public class ReconnectTransitionCommand extends Command 
{
	private boolean reconnectSource = true;
	private Transition transition;
	private GraphNode oldnode;
	private GraphNode node;
	private Point oldanchor;
	private Point anchor;

	public ReconnectTransitionCommand( Transition trans, boolean sourceYes, 
									   GraphNode newnode, Point newanchor )
	{
		transition = trans;
		reconnectSource = sourceYes;
		anchor = newanchor;
		node = newnode;
	}

	@Override
	public void execute()
	{
		assert canExecute();
		if (reconnectSource)
		{
			oldnode = transition.getSource();
			oldanchor = transition.getSourcePoint();
			if (oldnode!=node) transition.setSource(node);
			transition.setSourcePoint(anchor);
		}
		else
		{
			oldnode = transition.getTarget();
			oldanchor = transition.getTargetPoint();
			if (oldnode!=node) transition.setTarget(node);
			transition.setTargetPoint(anchor);
		}
	}

	
	@Override
	public void undo()
	{
		if (reconnectSource)
		{
			transition.setSource(oldnode);
			transition.setSourcePoint(oldanchor);
		} 
		else
		{
			transition.setTarget(oldnode);
			transition.setTargetPoint(oldanchor);
		}
	}

	public boolean canExecute()
	{
		return transition != null && 
			   node != null &&
			   (reconnectSource || !(node instanceof Precondition)) && 
			   (!reconnectSource || !(node instanceof Postcondition));
	}
} 
