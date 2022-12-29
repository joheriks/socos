package ibpe.model;

public class BoxContainer extends Node 
{
	
	public BoxContainer(Node p)
	{
		super();
		setParent(p);
	}
	
	/** Returns the procedure to which this BoxElement belongs. */
	public Procedure getProcedure()
	{
		return (Procedure)getParentOfType(Procedure.class);
	}

}
