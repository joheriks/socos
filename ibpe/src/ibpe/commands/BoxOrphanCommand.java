package ibpe.commands;

import ibpe.model.BoxContainer;
import ibpe.model.BoxElement;
import ibpe.part.BoxContainerPart;
import ibpe.part.BoxElementPart;

import java.util.List;

import org.eclipse.gef.commands.Command;


public class BoxOrphanCommand extends Command {
	
	private List<BoxElementPart<?>> parts;
	private BoxContainerPart parent;

	public void setParts(List<BoxElementPart<?>> editParts) {
		parts = editParts;
	}
	
	public void setParent(BoxContainerPart parent) {
		this.parent = parent;
	}
	
	public boolean canExecute()
	{
		for (BoxElementPart<?> n: parts)
		{
			if (!(n instanceof BoxElementPart<?>))
				return false;
		}
		return parent instanceof BoxContainerPart;
	}
	
	public void execute()
	{
		for (BoxElementPart<?> n: parts)
			((BoxContainer)parent.getModel()).remove((BoxElement)n.getModel());
	}
	
	public void undo()
	{
		for (BoxElementPart<?> n: parts)
			((BoxContainer)parent.getModel()).add((BoxElement)n.getModel());
	}

}
