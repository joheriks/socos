package ibpe.directedit;

import ibpe.io.*;
import ibpe.model.*;

import org.eclipse.jface.viewers.ICellEditorValidator;
import java.util.*;

public class NameCellEditorValidator implements ICellEditorValidator 
{
	Element elem;
	
	public NameCellEditorValidator(Element leaf) 
	{
		elem = leaf;
	}


	@SuppressWarnings({"unchecked" })
	public String isValid(Object value)
	{
		if (!(value instanceof String)) return "invalid";
		String string = (String)value;
		
		ParserInterface r = new ParserInterface();
		
		if (elem instanceof TextRow || elem instanceof MultiCall)
			return r.parseAsTextRow(string)==null ? "invalid text" : null;
		else
		{
			// In this case, we are editing a named item (procedure or situation
			
			// Empty postcondition name is allowed if there is no
			// anonymous postcondition already
			if (elem instanceof Postcondition && string.equals(""))
			{
				Postcondition p = (Postcondition)elem;
				if (p.getProcedure().hasAnonymousPostcondition())
					return "invalid name";
				else
					return null;
			}
			
			String name = r.parseAsName(string);
			if (name==null) return "invalid name";
			

			List reserved = new ArrayList();
			if (elem instanceof Procedure)
				reserved.addAll(((Procedure)elem).getContext().getProcedures());
			else if (elem instanceof BoxElement)
			{
				reserved.addAll(((BoxElement)elem).getProcedure().getAllSituations());
				reserved.addAll(((BoxElement)elem).getProcedure().getPostconditions());
			}
			else
				return "invalid";
			
			for (Object e : reserved)
				if (((Element)e).getText().equals(name) && e!=elem) 
					return "invalid name";

			return null;
		}
	}

}
