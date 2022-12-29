package ibpe.commands;

import ibpe.model.BoxElement;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.*;

public class BoxResizeCommand extends Command
{
	
	BoxElement element;
	protected Rectangle oldBounds,newBounds;
	
	
	public BoxResizeCommand( BoxElement elem, Rectangle bounds )
	{
		element = elem;
		
		newBounds = bounds;
	}
	
	public boolean canExecute()
	{
		return true;	
	}
	
	public void execute()
	{
		oldBounds = element.getBounds().getCopy();
		element.setBounds(newBounds);
	}
	
	public void undo()
	{
		element.setBounds(oldBounds);
	}
}
