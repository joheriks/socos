package ibpe.action;

import ibpe.*;
import ibpe.model.*;
import ibpe.part.*;
import java.util.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.gef.ui.actions.*;

public abstract class IBPESelectionAction extends SelectionAction 
{
	IBPESelectionAction(IWorkbenchPart part) 
	{
		super(part);
	}
	
	/** Convenience method, returns the IBPEditor instance */
	protected IBPEditor getEditor() 
	{
		return (IBPEditor)this.getWorkbenchPart();
	}

	
	/** Returns a list of selected AbstractIBPEEditPart:s */
	protected List<AbstractIBPEditPart<?>> getSelectedIBPParts()
	{
		List<AbstractIBPEditPart<?>> parts = new ArrayList<AbstractIBPEditPart<?>>();
		for (Object o : getSelectedObjects())
			if (o instanceof AbstractIBPEditPart<?>)
				parts.add((AbstractIBPEditPart<?>)o);
		return parts;
	}
	
	/** Returns a list of selected Element:s */
	protected List<Element> getSelectedElements()
	{
		List<Element> parts = new ArrayList<Element>();
		for (AbstractIBPEditPart<?> p : getSelectedIBPParts())
			parts.add(p.getModel());
		return parts;
	}
	
	/**
	 *  Returns the one Element selected. If none or more than one elements are
	 *  return null.
	 */
	protected AbstractIBPEditPart<?> getSelectedOne()
	{ 
		return (getSelectedIBPParts().size()==1) ? getSelectedIBPParts().get(0) : null;
	}
	
	/**
	 * Return the one Transition selected, or null if no transition is slected.
	 */
	protected TransitionPart getSelectedTransition()
	{
		if (getSelectedObjects().size()!=1) return null;
		if (getSelectedObjects().get(0) instanceof TransitionPart)
			return (TransitionPart)getSelectedObjects().get(0);
		else
			return null;
	}

}