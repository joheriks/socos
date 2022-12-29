package ibpe.commands;

import ibpe.model.*;

import org.eclipse.draw2d.geometry.*;


public class InsertBendpointCommand extends BendpointCommand 
{
	protected Point position;
	
	public InsertBendpointCommand( Transition trans, int index, Point pos )
	{
		super(trans,index);
		position = pos;
	}
	
	@Override
	public boolean canExecute() 
	{
		return 0<=index && transition != null && position != null;
	}
	
	@Override
	public void execute()
	{
		assert canExecute();
		transition.addPoint(index, position);
	}
	
	@Override
	public void undo() 
	{	
		transition.removePoint(index);
	}
}
