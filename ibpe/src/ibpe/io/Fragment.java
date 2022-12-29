package ibpe.io;

import ibpe.commands.UpdateTextCommand;
import ibpe.model.*;

import org.eclipse.gef.commands.*;
import java.util.*;

abstract public class Fragment<B extends Element> 
{
	
	/**
	 * An Fragment instance captures a well defined model subset,
	 * as well as the intention of inserting it into a specific
	 * parent and position in the current model. B is the most 
	 * specific base class for all elements that make up the 
	 * fragment. 
	 * 
	 * This class is also responsible for adapting the fragment for
	 * insertion at an abstract model level, e.g. by ensuring name 
	 * uniqueness. It should NOT modify the current model at all;
	 * resizing containers and clearing area is the responsibility of
	 * commands. It does not deal with coordinates and positions.
	 */
	
	/** Returns the list of model elements that make up this fragment. */
	abstract public List<B> getModelElements();
	
	/** 
	 * Determine if it is acceptable to insert. if after==null, insert as
	 * first element */
	abstract public boolean canInsertAt( Node parent, Element after );

	/** 
	 * Adapt the stored fragment to an insertion point in the model.
	 * Precondition: adapt(parent,pos)==true.
	 * Return an insertion command for inserting into parent. 
	 */
	abstract public Command adapt( Node parent, Element after );
	
	/**
	 * Create a new fragment with a copy of the model elements. The copied
	 * elements are orphans.
	 */
	abstract public Fragment<B> copy();

	/** For debugging */
	@Override
	public String toString() 
	{
		StringBuffer sb = new StringBuffer();
		Serializer s = new Serializer(sb);
		s.serialize(this);
		return sb.toString();
	}

	
	/** Common method for generating renaming commands with respect to a sequence of
	 *  reserved names. Returns null if no renamings are necessary, otherwise returns
	 *  a CompoundCommand instance. */
	public Command getRenameCommand( List reservedElems, List checkElems )
	{
		if (checkElems.size()==0)
			return null;
		
		CompoundCommand cmd = new CompoundCommand();
		HashSet<String> namespace = new HashSet<String>();
		for (Object e : reservedElems)
			if (!checkElems.contains(e))
				namespace.add(((Element)e).getText());
		ArrayList<String> check = new ArrayList<String>();
		for (Object e : checkElems)
			check.add(((Element)e).getText());
		List<String> renamed = Names.rename(check,namespace);
		for (int i=0; i<check.size(); i++)
			if (renamed.get(i)!=null)
				cmd.add(new UpdateTextCommand((Element)checkElems.get(i),renamed.get(i)));
		return cmd.isEmpty() ? null : cmd;
	}

}

