package ibpe.commands;

import org.eclipse.gef.commands.*;
import ibpe.model.*;

public class SetChoiceTypeCommand extends Command
{
	private GraphNode node;
	private String newValue;
	
	private String oldValue;
	
	
	public SetChoiceTypeCommand( GraphNode elem, String val )
	{
		node = elem;
		newValue = val;
	}
	
	@Override
	public void execute()
	{
		oldValue = node.getChoiceType();
		node.setChoiceType(newValue);
	}
	
	@Override
	public void undo()
	{
		node.setChoiceType(oldValue);
	}
}
