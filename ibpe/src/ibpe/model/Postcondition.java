package ibpe.model;

import java.util.*;

public class Postcondition extends BoxElement 
{
	public TextContainer constraintContainer;
	
	public Postcondition( List<TextRow> contents ) 
	{
		add(constraintContainer = new TextContainer(this));
		for (TextRow c:contents)
			constraintContainer.add(c);
	}
	
	public Postcondition()
	{
		this(new ArrayList<TextRow>());
	}

	public boolean isAnonymous()
	{
		return getText().equals("");
	}
	
	public  List<TextRow> getConstraints() 
	{
		return constraintContainer.getChildrenOfType(TextRow.class);
	}
	
}
