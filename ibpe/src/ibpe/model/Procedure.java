package ibpe.model;

import java.util.*;

public class Procedure extends Node
{
	public TextContainer signatureContainer;
	public TextContainer variantContainer;
	public TextContainer declContainer;
	public BoxContainer boxContainer;
	
	public Procedure( String name, 
					  List<TextRow> signature, 
					  Precondition pre, 
					  List<Postcondition> posts, 
					  TextRow var,
					  List<TextRow> decls,
					  List<Situation> toplevels,
					  List<Transition> transitions,
					  List<Fork> forks ) 
	{
		add(signatureContainer = new TextContainer(this));
		add(variantContainer = new TextContainer(this));
		add(declContainer = new TextContainer(this));
		add(boxContainer = new BoxContainer(this));
		setText(name);
		for (TextRow sig : signature)
			signatureContainer.add(sig);
		if (var!=null)
			variantContainer.add(var);
		for (TextRow decl : decls)
			declContainer.add(decl);
		if (pre!=null)
			boxContainer.add(pre);
		for (Postcondition p : posts) 
			boxContainer.add(p);
		for (Situation s : toplevels)
			boxContainer.add(s);
		
		for (Transition t : transitions) add(t);
		for (Fork f : forks) add(f);
	}
		
	public TextContainer getSignatureContainer() { return signatureContainer; }
	
	public TextContainer getVariantContainer() { return variantContainer; }
	
	public TextContainer getDeclContainer() { return declContainer; }

	public BoxContainer getBoxContainer()	{ return boxContainer; }

	
	public List<TextRow> getSignature()
	{
		return signatureContainer.getChildrenOfType(TextRow.class);
	}
	
	
	public TextRow getVariant()
	{
		if (variantContainer.getChildren().isEmpty())
			return null;
		else
			return variantContainer.getChildrenOfType(TextRow.class).get(0);
	}


	public List<TextRow> getDeclarations()
	{
		return declContainer.getChildrenOfType(TextRow.class);
	}
	
	
	public List<Situation> getToplevelSituations()
	{
		return boxContainer.getChildrenOfType(Situation.class);
	}
	
	
	public List<Situation> getAllSituations()
	{
		return getDescendantsOfType(Situation.class);
	}
	
	
	public Precondition getPrecondition()
	{
		List<Precondition> l = boxContainer.getChildrenOfType(Precondition.class);
		assert(l.size()<=1);
		return l.size()==0 ? null : l.get(0);
	}
	
	
	public List<Postcondition> getPostconditions()
	{
		return boxContainer.getChildrenOfType(Postcondition.class);
	}
	
	
	public boolean hasAnonymousPostcondition()
	{
		for (Postcondition p : getPostconditions())
			if (p.isAnonymous()) return true;
		return false;
	}
	
	
	public List<Transition> getTransitions()
	{
		return getChildrenOfType(Transition.class);
	}

	
	public Context getContext()
	{
		return (Context)getParent();
	}
	
	
	public HashMap<String,Node> getNamespace()
	{
		HashMap<String,Node> retval = new HashMap<String,Node>();
		for (BoxElement s : getDescendantsOfType(BoxElement.class))
		{
			if (s instanceof Postcondition && ((Postcondition)s).isAnonymous())
				retval.put("post__",s);
			else
				retval.put(s.getText(),s);
		}
		return retval;
	}

}
	



