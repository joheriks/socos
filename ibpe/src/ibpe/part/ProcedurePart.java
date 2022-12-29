 package ibpe.part;

import java.beans.PropertyChangeEvent;
import java.util.*;

import ibpe.editpolicies.*;
import ibpe.figure.*;
import ibpe.model.*;

import org.eclipse.gef.*;
import org.eclipse.gef.editparts.*;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;

public class ProcedurePart extends AbstractDirectEditPart<Procedure> 
{
	
	@Override
	protected ProcedureFigure createFigure() 
	{
		return new ProcedureFigure();
	}
	
	@Override
	public ProcedureFigure getFigure()
	{
		return (ProcedureFigure)super.getFigure();
	}
	
	@Override
	public LabelFigure getLabel() 
	{
		return getFigure().getLabel();
	}
	
	
	@Override
	public List<Element> getModelChildren()
	{
		List<Element> l = new ArrayList<Element>();

		for (Element e : getModel().getChildren())
		{
			// transitions are added through the first encountered endpoint
			if (!(e instanceof Transition)) 
				l.add(e);
		}
		
		/*for (Element e : getModel().getChildren())
		{
			// transitions are added through the first encountered endpoint
			if (e instanceof Transition) 
				l.add(e);
		}*/
		
		return l;
	}

	
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,new EditTextPolicy());
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableEditPolicyOptionalHandles(false));
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new DeleteElementEditPolicy());
	}
	

	@Override
	public void addChildVisual( EditPart childPart, int index ) 
	{
		// Add the child into the right compartment based on its role in the model
		ProcedureFigure fig = (ProcedureFigure)getContentPane();
		AbstractGraphicalEditPart ep = (AbstractGraphicalEditPart)childPart;
		if (childPart.getModel()==getModel().signatureContainer) fig.setSignatureCompartment(ep.getFigure());
		else if (childPart.getModel()==getModel().variantContainer) fig.setVariantCompartment(ep.getFigure());
		else if (childPart.getModel()==getModel().declContainer) fig.setDeclCompartment(ep.getFigure()); 
		else if (childPart.getModel()==getModel().boxContainer) fig.setBoxCompartment(ep.getFigure());
		else if (childPart.getModel() instanceof Fork) fig.getBoxCompartment().add(ep.getFigure());
	}

	@Override
	protected void removeChildVisual( EditPart childPart ) 
	{
		IFigure fig = ((GraphicalEditPart) childPart).getFigure();
		if (childPart instanceof ForkPart) 
			((ProcedureFigure)getContentPane()).getBoxCompartment().remove(fig);
		else
			getContentPane().remove(fig);
	}

	
	@Override
	public boolean isCompartmentalized()
	{
		return true;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		this.refreshChildren();
		Transition t = null;
		if (Node.PROPERTY_REMOVE.equals(evt.getPropertyName())
			&& evt.getOldValue() instanceof Transition)
			t = (Transition)evt.getOldValue();
		else if (Node.PROPERTY_ADD.equals(evt.getPropertyName())
				 && evt.getNewValue() instanceof Transition)
			t = (Transition)evt.getNewValue();
		if (t!=null)
		{
			if (modelToPart(t.getSource())!=null)
				((GraphNodePart<?>)modelToPart(t.getSource())).refresh();
			if (modelToPart(t.getTarget())!=null)
				((GraphNodePart<?>)modelToPart(t.getTarget())).refresh();
		}
	}
}
 