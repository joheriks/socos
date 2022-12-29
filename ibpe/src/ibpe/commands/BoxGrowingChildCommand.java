package ibpe.commands;

import ibpe.IBPEditor;
import ibpe.part.*;
import ibpe.model.*;

import java.util.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.*;

/**
 * This Command handles resize propagation.
 * More here.
 */
public class BoxGrowingChildCommand extends PartCommand 
{

	private BoxElement element;
	
	private List<Command> cmdList = null;
	
	public BoxGrowingChildCommand( Map epr, BoxElement elem )
	{
		super(epr);
		element = elem;
	}
	
		
	public void execute()
	{
		if (cmdList!=null)
		{
			for (Command cmd : cmdList) cmd.execute();
			return;
		}
		
		cmdList = new LinkedList<Command>();
		
		List<BoxElement> boxes = new ArrayList<BoxElement>();
		boxes.add(element);
		for (BoxElement e : element.getEnclosingBoxElements())
			boxes.add(e);
		Dimension oldSize, newSize;
		
		for (BoxElement be : boxes)
		{
			// oldSize = the currently stored size data in the model.
			oldSize = be.getBounds().getSize().getCopy(); //.getScaled(1/IBPEditor.manager.getZoom());
			// newSize = the new size of the part derived from the getMinimumSize algorithm.
			//           newSize may be larger than oldSize if BoxElements have moved within
			//           a Situation, or if TextRows have increased in size in a Situation or CallPart.
			
			BoxElementPart<?> part = (BoxElementPart<?>)partRegistry.get(be);
			
			newSize = part.getFigure().getMinimumSize();
			
			// We don't want the new size to be smaller than the original size. We therefore union newSize 
			// with	oldSize.
			newSize = part.getModelBounds().getCopy().union(newSize).getSize();
		
			
			// If there is a difference in size, we resize the the part.
			if(!oldSize.equals(newSize))
			{
				BoxResizeCommand cmd1 = new BoxResizeCommand(part.getModel(),part.getModelBounds().getResized(newSize.getDifference(oldSize)));
				/*cmd1.setParts(part);
				cmd1.setSizeDelta(newSize.getDifference(oldSize));
				cmd1.setMoveDelta(new Point());*/
				
				if(cmd1.canExecute())
				{
					cmdList.add(cmd1);
					cmd1.execute();
				}
			}
			
			// After resizing, we also want to clear the area that the resized part will occupy.
			LinkedList<BoxElement> l = new LinkedList<BoxElement>();
			l.add(part.getModel());
			BoxClearAreaCommand cmd2 = new BoxClearAreaCommand(partRegistry,l,
															   (BoxContainer)part.getParent().getModel(),
															   null,
															   part.getModelBounds().getCopy().setSize(newSize));
			/*cmd2.setParent((BoxContainerPart) part.getParent());
			cmd2.setParts(part);
			cmd2.setRect(part.getModelBounds().getCopy().setSize(newSize));*/

			if(cmd2.canExecute())
			{
				cmdList.add(cmd2);
				cmd2.execute();
			}

			// Finally, we make sure that no BoxElements will end up in negative coordinates.
			BoxMoveIntoPositiveCommand cmd3 = new BoxMoveIntoPositiveCommand(partRegistry,(BoxContainer)be.getParent());
			//cmd3.setParent((BoxContainerPart) part.getParent());
			
			if(cmd3.canExecute())
			{
				cmdList.add(cmd3);
				cmd3.execute();
			}
		}

	}

	
	public void undo()
	{
		for (int i = cmdList.size() - 1; i >= 0; i--)
			cmdList.get(i).undo();
	}
	
}
