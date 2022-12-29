package ibpe.commands;

import ibpe.model.Fork;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

public class MoveForkCommand extends Command 
{
	Fork fork;
	Point newpos;
	Point oldpos; 

	public MoveForkCommand( Fork model, Point pos )
	{
		fork = model;
		newpos = pos;
	}
	
	public void execute()
	{
		oldpos = fork.getPosition();
		fork.setPosition(newpos);
	}
		
	public void undo()
	{
		fork.setPosition(oldpos);
	}

}
