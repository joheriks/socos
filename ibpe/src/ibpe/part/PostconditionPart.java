package ibpe.part;

import org.eclipse.draw2d.*;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

import ibpe.editpolicies.DeleteElementEditPolicy;
import ibpe.editpolicies.EditTextPolicy;
import ibpe.editpolicies.NonResizableEditPolicyOptionalHandles;
import ibpe.figure.*;
import ibpe.model.*;

public class PostconditionPart extends BoxElementPart<Postcondition> 
{

	@Override
	protected Class<PostconditionFigure> getFigureClass() 
	{
		return PostconditionFigure.class;
	}
	
	@Override
	public PostconditionFigure getFigure()
	{
		return (PostconditionFigure)super.getFigure();
	}
	
	@Override
	public LabelFigure getLabel() 
	{
		return ((PostconditionFigure)getFigure()).getLabel();
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
		IFigure childFig = ((AbstractIBPEditPart<?>)childPart).getFigure();
		if (childPart.getModel()==getModel().constraintContainer) getFigure().setConstraintCompartment(childFig); 
		else assert false; // shouldn't happen
	}

	@Override
	public boolean isCompartmentalized() { return true;	}
	
	
	@Override
	public void refreshVisuals()
	{
		super.refreshVisuals();
		getFigure().setNameCompartmentVisible(!getModel().isAnonymous());
	}

	@Override
	public void performDirectEditRequest( DirectEditRequest request )
	{
		getFigure().setNameCompartmentVisible(true);
		getFigure().invalidate();
		getFigure().revalidate();
		super.performDirectEditRequest(request);
	}

}
