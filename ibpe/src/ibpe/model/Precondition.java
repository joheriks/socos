package ibpe.model;

import java.util.*;

public class Precondition extends BoxElement 
{
	public TextContainer constraintContainer;
	
	public Precondition( List<TextRow> contents ) 
	{
		add(constraintContainer = new TextContainer(this));
		for (TextRow c:contents)
			constraintContainer.add(c);
	}

	public Precondition()
	{
		this(new ArrayList<TextRow>());
	}
	
	public  List<TextRow> getConstraints() 
	{
		return constraintContainer.getChildrenOfType(TextRow.class);
	}

}
