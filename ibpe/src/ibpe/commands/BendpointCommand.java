package ibpe.commands;

import ibpe.model.Transition;

import org.eclipse.gef.commands.Command;


public class BendpointCommand extends Command 
{
	protected int index = -1;
	protected Transition transition;
	
	BendpointCommand( Transition trans, int idx )
	{
		transition = trans;
		index = idx;
	}

}
