package ibpe.commands;

import ibpe.model.*;

import org.eclipse.draw2d.geometry.Point;


public class DeleteBendpointCommand extends BendpointCommand 
{
	Point point;

	public DeleteBendpointCommand( Transition trans, int index )
	{
		super(trans,index);
	}
	
	
	@Override
	public void execute()
	{
		assert canExecute();
		point = transition.getPoint(index).getCopy();
		transition.removePoint(index);
	}
	
	
	@Override
	public void undo() 
	{
		transition.addPoint(index, point.getCopy());
	}
	
	
	@Override
	public boolean canExecute() 
	{
		return transition != null;
	}


}
