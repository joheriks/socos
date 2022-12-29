package ibpe.commands;

import ibpe.model.*;

import java.util.*;

import org.eclipse.gef.commands.Command;

public class DeleteCommand extends Command
{
	class DeletedEntry
	{
		public Element element;
		public Node parent;
		public int index;
		DeletedEntry( Element e, Node p, int i ) { element=e; parent=p; index=i; };
	}

	public List<DeletedEntry> entries = new ArrayList<DeletedEntry>();
	
	
	public DeleteCommand( Collection<Element> elms )
	{
		assert 0<elms.size();
		for ( Element e: elms )
			entries.add(new DeletedEntry(e,e.getParent(),-1));
	}

	public DeleteCommand( Element e )
	{
		this(Collections.singletonList(e));
	}
	

	@Override
	public void execute()
	{
		for (DeletedEntry entry : entries)
		{
			assert entry.parent!=null;
			// entries may not be present, e.g. if they are being deleted more than once.
			// in such a case we record the index as -1
			entry.index = entry.element.getIndex();
			if (entry.index!=-1)
				entry.parent.remove(entry.element);
		}
	} 
	
	
	@Override
	public void undo()
	{
		for (int i=entries.size()-1; i>=0; i--)
		{
			DeletedEntry entry = entries.get(i);	
			assert entry.index==-1 || (0<=entry.index && entry.index<=entry.parent.getChildren().size());
			if (entry.index!=-1)
			{
				entry.parent.add(entry.element,entry.index);
				entry.index = -1;
			}
		}
	} 
	

	@Override
	public boolean canExecute()
	{
		for (DeletedEntry entry : entries)
			if (entry.parent==null) 
				return false;
		return true; 
	}
} 
