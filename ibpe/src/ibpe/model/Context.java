package ibpe.model;

import java.util.*;


public class Context extends TextContainer 
{
	public Context(String name, List<Element> contents)
	{
		super(null);
		for (Element e : contents)
			add(e);
		setText(name);
	}

	public List<Procedure> getProcedures() 
	{
		return getChildrenOfType(Procedure.class);
	}
	

	public HashMap<String,Node> getNamespace()
	{
		HashMap<String,Node> retval = new HashMap<String,Node>();
		for (Procedure p : getProcedures())
			retval.put(p.getText(),p);
		return retval;
	}

}