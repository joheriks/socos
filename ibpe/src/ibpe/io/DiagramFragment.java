package ibpe.io;

import ibpe.commands.*;
import ibpe.model.*;

import org.eclipse.gef.commands.*;

import java.util.*;


public class DiagramFragment extends Fragment<Element> 
{
	Precondition pre = null;
	List<Postcondition> posts = new ArrayList<Postcondition>();
	List<Situation> situations = new ArrayList<Situation>();
	Set<Transition> transitions = new HashSet<Transition>();
	Set<Fork> forks = new HashSet<Fork>();
	
	
	public DiagramFragment( Precondition pr,
							Collection<Postcondition> psts,
							Collection<Situation> sits,
							Collection<Transition> ts,
							Collection<Fork> fks )
	{
		pre = pr;
		posts.addAll(psts);
		situations.addAll(sits);
		transitions.addAll(ts);
		forks.addAll(fks);
		
		assert 0<getModelElements().size();
	}
	
	/** Builds a fragment by including the nested BoxElements as well as
	 *  all included Transitions and Forks.
	 */
	public DiagramFragment( Collection<BoxElement> boxElements )
	{
		for (BoxElement be: boxElements)
		{
			if (be instanceof Precondition) pre = (Precondition)be;
			else if (be instanceof Postcondition) posts.add((Postcondition)be);
			else if (be instanceof Situation) situations.add((Situation)be);
		}
		BoxElement.addIncludedTransitionArcsAndForks(boxElements,transitions,forks);
		assert 0<getModelElements().size();
	}

	@Override
	public List<Element> getModelElements() 
	{
		List<Element> l = new ArrayList<Element>();
		if (pre!=null) l.add(pre);
		l.addAll(posts);
		l.addAll(situations);
		l.addAll(transitions);
		l.addAll(forks);
		return l;
	}
	
	public List<BoxElement> getBoxElements()
	{
		List<BoxElement> l = new ArrayList<BoxElement>();
		if (pre!=null) l.add(pre);
		l.addAll(posts);
		l.addAll(situations);
		return l;
	}
	
	@Override
	public boolean canInsertAt(Node parent, Element after) 
	{
		if (!(parent instanceof BoxContainer))
			return false;
		
		if (parent.getParent() instanceof Procedure)
			return pre==null || ((Procedure)parent.getParent()).getPrecondition()==null; 

		else if (parent.getParent() instanceof Situation)
			return pre==null && posts.size()==0;
		
		else
			return false;
		
	}

	protected List<Situation> getAllSituations()
	{
		ArrayList<Situation> sits = new ArrayList<Situation>();
		for (Situation s : situations)
		{
			sits.add(s);
			sits.addAll(s.getAllNested());
		}
		return sits;
	}
	
	@Override
	public Command adapt(Node parent, Element after) 
	{
		CompoundCommand cmd = new CompoundCommand();

		// generate delete commands for all elements that have a parent,
		// i.e., those already in the model
		List<Element> delete = new ArrayList<Element>();
		for (Element e : getModelElements())
			if (e.getParent()!=null)
				delete.add(e);
		if (!delete.isEmpty()) cmd.add(new DeleteCommand(delete));
		
		// wrap commands that rename situations to ensure unique names
		Procedure proc = (Procedure)parent.getParentOfType(Procedure.class);
		List reserved = proc.getAllSituations();
		reserved.addAll(proc.getPostconditions());
		List check = getAllSituations();
		check.addAll(posts);
		Command rename = getRenameCommand(reserved,check); 
		if (rename!=null)
			cmd.add(rename);
	
		// situations go into the parent (boxcontainer)
		if (pre!=null) cmd.add(new InsertCommand(pre,parent,after));
		if (!posts.isEmpty()) cmd.add(new InsertCommand((List)posts,parent,after));
		if (!situations.isEmpty()) cmd.add(new InsertCommand((List)situations,parent,after));
		
		// forks and transitions go into the procedure 
		if (!transitions.isEmpty()) cmd.add(new InsertCommand(new ArrayList(transitions),proc,null));
		if (!forks.isEmpty()) cmd.add(new InsertCommand(new ArrayList(forks),proc,null));
		
		return cmd.unwrap();
	}
	
	@Override
	public DiagramFragment copy()
	{
		// unparse-parse to build a copy, to avoid implementing copy methods in
		// the model
		StringBuffer sb = new StringBuffer();
		(new Serializer(sb)).serialize(this);
		ParserInterface pi = new ParserInterface();
		Fragment<?> f = pi.parseAsFragment(sb.toString());
		assert f!=null;
		return (DiagramFragment)f;
	}

}
