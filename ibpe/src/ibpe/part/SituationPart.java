package ibpe.part;

import org.eclipse.draw2d.*;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

import ibpe.editpolicies.EditTextPolicy;
import ibpe.figure.*;
import ibpe.model.*;

public class SituationPart extends BoxElementPart<Situation>
{
	@Override
	protected Class<IntermediateSituationFigure> getFigureClass() 
	{
		return IntermediateSituationFigure.class;
	}

	
	@Override
	public IntermediateSituationFigure getFigure()
	{
		return (IntermediateSituationFigure)super.getFigure();
	}
	
	
	@Override
	public LabelFigure getLabel() 
	{
		return getFigure().getLabel();
	}

	
	@Override
	protected void refreshVisuals()
	{
		super.refreshVisuals();
		getFigure().updateColor(getModel().getNestingDepth());
	}
	

	@Override
	protected void createEditPolicies()
	{
		super.createEditPolicies();
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,new EditTextPolicy());
	}

	
	@Override
	public void addChildVisual( EditPart childPart, int index ) 
	{
		// Add the child into the right compartment based on its role in the model
		IFigure childFig = ((AbstractIBPEditPart<?>)childPart).getFigure();
		if (childPart.getModel()==getModel().variantContainer) getFigure().setVariantCompartment(childFig);
		else if (childPart.getModel()==getModel().constraintContainer) getFigure().setConstraintCompartment(childFig); 
		else if (childPart.getModel()==getModel().declarationContainer) getFigure().setDeclarationCompartment(childFig);
		else if (childPart.getModel()==getModel().situationContainer) getFigure().setSituationCompartment(childFig);
		else
			assert false; // shouldn't happen
	}

	@Override
	public boolean isCompartmentalized() { return true;	}

}
