package ibpe.model;

import java.util.*;

public class Situation extends BoxElement 
{
	public TextContainer declarationContainer;
	public TextContainer constraintContainer;
	public TextContainer variantContainer;
	public BoxContainer situationContainer;
	
	public Situation( String name,
					  Collection<TextRow> declaration,
			  		  Collection<TextRow> constraints, 
			  		  TextRow var,
			  		  Collection<Situation> nested )
	{
		add(variantContainer = new TextContainer(this));
		add(constraintContainer = new TextContainer(this));
		add(declarationContainer = new TextContainer(this));
		add(situationContainer = new BoxContainer(this));
		
		setText(name);
		
		for (TextRow c : declaration) 
			declarationContainer.add(c);
		if (var!=null) variantContainer.add(var);
		for (TextRow c : constraints) 
			constraintContainer.add(c);
		for (Situation s : nested) 
			situationContainer.add(s);
	}
	
	public TextContainer getTextContainer() { return constraintContainer; }
	
	public TextContainer getInitializeContainer() { return declarationContainer; }

	public BoxContainer getBoxContainer()	{ return situationContainer; }

	public TextRow getVariant()
	{
		if (variantContainer.getChildren().isEmpty())
			return null;
		else
			return variantContainer.getChildrenOfType(TextRow.class).get(0);
	}
	
	public  List<TextRow> getConstraints() 
	{
		return constraintContainer.getChildrenOfType(TextRow.class);
	}
	
	public  List<TextRow> getDeclaration() 
	{
		return declarationContainer.getChildrenOfType(TextRow.class);
	}

	public  List<Situation> getNested() 
	{
		return situationContainer.getChildrenOfType(Situation.class);
	}
	
	public List<Situation> getAllNested()
	{
		return getDescendantsOfType(Situation.class);
	}
	
	
	// Returns the nesting depth, starting from 0, of this situation.
	public int getNestingDepth()
	{
		int depth = -1;
		for (Node p=this; p instanceof Situation; p=p.getParent().getParent())
			depth++;
		return depth;
	}
}
