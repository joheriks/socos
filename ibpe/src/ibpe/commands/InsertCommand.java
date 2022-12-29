package ibpe.commands;

import ibpe.model.*;

import org.eclipse.gef.commands.*;
import java.util.*;

public class InsertCommand extends Command 
{ 
	public ArrayList<Element> elements; 
	public Node insertInto;
	public Element insertAfter;
	
	public InsertCommand( List<Element> elms, Node into, Element after ) 
	{
		elements = new ArrayList<Element>(elms);
		insertInto = into;
		insertAfter = after;
	}

	public InsertCommand( Element elm, Node into, Element after )
	{
		this(Collections.singletonList(elm),into,after);
	}
	
	@Override
	public boolean canExecute() 
	{
		return true; 
	}
	
	@Override
	public void execute()
	{
		assert insertAfter==null || insertInto.getChildren().contains(insertAfter);
		int pos = insertAfter==null ? 0 : insertInto.getChildren().indexOf(insertAfter)+1;
		for (Element elm : elements)
		{
			insertInto.add(elm,pos);
			pos++;
		}
	}
	
	@Override
	public void undo()
	{
		for (Element elm : elements)
			insertInto.remove(elm);
	}
	

}
