package ibpe.commands;

import ibpe.model.Transition;

import org.eclipse.draw2d.geometry.Point;


public class MoveBendpointCommand extends BendpointCommand 
{
	Point newlocation;
	Point oldlocation;
	
	public MoveBendpointCommand( Transition trans, int index, Point loc )
	{
		super(trans,index);
		newlocation = loc;
	}

	@Override
	public boolean canExecute()
	{
		return newlocation != null && transition!=null && transition.getPoint(index) != null;
	}
	
	@Override
	public void execute()
	{
		assert canExecute();
		oldlocation = transition.getPoint(index);
		transition.removePoint(index);
		transition.addPoint(index, newlocation);		
	}
	
	@Override
	public void undo()
	{
		transition.removePoint(index);
		transition.addPoint(index, oldlocation);
	}
	
}
