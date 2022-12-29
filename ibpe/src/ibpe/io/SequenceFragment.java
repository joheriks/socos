package ibpe.io;

import ibpe.commands.*;
import ibpe.model.*;

import java.util.*;

import org.eclipse.gef.commands.*;


public class SequenceFragment extends Fragment<Element> {
	
	/* contents is a list of TextRow:s and Procedure:s */
	protected ArrayList<Element> contents;
	
	public SequenceFragment( List<Element> rows )
	{
		assert 0<rows.size();
		//assert: rows is a forest
		contents = new ArrayList<Element>(rows);
	}
	
	@Override
	public List<Element> getModelElements()
	{
		return contents;
	}
	
	@Override
	public boolean canInsertAt( Node parent, Element after ) 
	{
		for (Element e : contents)
			if (e instanceof Procedure) return parent instanceof Context;
		return parent instanceof TextContainer;
	}

	@Override
	public Command adapt( Node parent, Element after ) 
	{
		CompoundCommand cmd = new CompoundCommand();

		// delete all elements that have a parent
		List<Element> delete = new ArrayList<Element>();
		for (Element e : contents)
			if (e.getParent()!=null)
				delete.add(e);
		if (!delete.isEmpty()) cmd.add(new DeleteCommand(delete));
		
		// * create commands for renaming procedures to ensure unique names
		if (parent instanceof Context)
		{
			ArrayList<Procedure> procs = new ArrayList<Procedure>();
			for (Element e : getModelElements())
				if (e instanceof Procedure)
					procs.add((Procedure)e);
			
			Command rename = getRenameCommand(((Context)parent).getProcedures(),procs);
			if (rename!=null)
				cmd.add(rename);
		}

		cmd.add(new InsertCommand((List)contents,parent,after));
		return cmd.unwrap();
	}
	
	
	@Override
	public SequenceFragment copy()
	{
		// unparse-parse to build a copy, to avoid implementing copy methods in
		// the model
		StringBuffer sb = new StringBuffer();
		(new Serializer(sb)).serialize(this);
		ParserInterface pi = new ParserInterface();
		Fragment<?> f = pi.parseAsFragment(sb.toString());
		assert f!=null;
		return (SequenceFragment)f;
	}
}
