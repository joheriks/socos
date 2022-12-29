package ibpe.commands;

import ibpe.model.*;
import ibpe.part.*;

import java.util.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.*;

/**
 * This Command is used to clear an area in a BoxContainer.
 * The move-method is run, and it returns a list of the parts that will have to
 * be moved to avoid overlapping. These parts' bounds are then translated.
 */
public class BoxClearAreaCommand extends PartCommand 
{
	protected BoxContainer parent;
	protected List<BoxElement> boxes;
	protected Point moveDelta;
	protected Rectangle rect;
	
	protected List<Command> commands = new ArrayList<Command>();

	
	// This command has two modes: either one of m or r is set, exclusively. 
	public BoxClearAreaCommand( Map epr, List<BoxElement> boxlist, BoxContainer par, Point m, Rectangle r )
	{
		super(epr);
		assert boxlist.size()>0;
		assert (m!=null && r==null) ||
		       (m==null && r!=null);
		
		boxes = new LinkedList<BoxElement>(boxlist);
		parent = par;
		moveDelta = m;
		rect = r;
		partRegistry = epr;
	}
	
	
	@Override
	public boolean canExecute() 
	{
		return parent != null && (rect != null || moveDelta != null);
	}
	
	
	@Override
	public void execute()
	{
		if (commands.isEmpty())
		{
			BoxContainerPart parentpart = (BoxContainerPart)partRegistry.get(parent);
			
			List<BoxElementPart<?>> boxparts = new ArrayList<BoxElementPart<?>>();
			for (BoxElement be : boxes)
				boxparts.add((BoxElementPart<?>)partRegistry.get(be));

			Queue<BoxElementPart<?>> q = null;
			if (moveDelta != null)
			    q = parentpart.move(boxparts,moveDelta);
			else if (rect != null)
				//q = parentpart.move(boxparts,rect.getTranslated(parentpart.getRootBoxContainer().getFigure().getBounds().getLocation()));
				q = parentpart.move(boxparts,rect);
			else
				assert false;
			
			for (BoxElementPart<?> n : q)
			{
				commands.add(new BoxMoveCommand(n.getModel(),null,n.getMoveDelta()));
						     //n.getModelBounds().getLocation().getTranslated(n.getMoveDelta())));
				n.getMoveDelta().setLocation(0, 0);
				n.setMoveDirection(null);
			}
		}
		
		for (Command cmd : commands) cmd.execute();

	}
	
	public void undo()
	{
		for (int i=commands.size()-1; i>=0; i--)
			commands.get(i).undo();
	}
	
}
