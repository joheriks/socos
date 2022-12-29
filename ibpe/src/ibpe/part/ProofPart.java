package ibpe.part;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;

import ibpe.directedit.NameCellEditorValidator;
import ibpe.directedit.ProofDirectEditManager;
import ibpe.editpolicies.DeleteElementEditPolicy;
import ibpe.editpolicies.NonResizableEditPolicyOptionalHandles;
import ibpe.figure.LabelFigure;
import ibpe.figure.ProofFigure;
import ibpe.model.Node;
import ibpe.model.Proof;

// TODO: want to inherit from AbstractIBPEditPart<Proof>, not extends AbstractDirectEditPart<Proof>
// but performRequest() never gets called if AbstractIBPEditPart<Proof> is inherited

public class ProofPart extends AbstractDirectEditPart<Proof> //extends AbstractIBPEditPart<Proof> 
{
	protected ProofDirectEditManager manager=new ProofDirectEditManager(this,new NameCellEditorValidator(getModel()));

	protected ProofFigure proofFigure;
	
	@Override 
	protected IFigure createFigure()
	{
		proofFigure=new ProofFigure();
		return proofFigure;
	}

	@Override
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new DeleteElementEditPolicy());
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableEditPolicyOptionalHandles(false));
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new EditProofPolicy());
	}

	public LabelFigure getLabel() 
	{
		return proofFigure.getLabel();
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(Node.PROPERTY_TEXT))
			refreshVisuals();
	}

	@Override
	protected void refreshVisuals()
	{
		if (getLabel()!=null) 
		{
			getLabel().setVisible(true);
			getLabel().setText(getModel().getText());
		}
	}

	@Override
	public void performRequest(Request request)
	{
		if (request.getType().equals(RequestConstants.REQ_DIRECT_EDIT))
		{
			if (request instanceof DirectEditRequest)
				performDirectEditRequest((DirectEditRequest) request);
		}
		else
			super.performRequest(request);
	}
	

	public void performDirectEditRequest( DirectEditRequest request )
	{
		//if (!directEditHitTest(request.getLocation().getCopy()))
		//	return;

		// if text==null, refuse directedit
		if (getModel().getText()==null)
			return;
		
		if (getLabel()!=null)
			getLabel().setVisible(false);

		/*
		if(manager==null)
		{
			manager=new ProofDirectEditManager(this,new NameCellEditorValidator(getModel()));
		}
		*/
			
		/*
		manager.setInitialValue(null);
		manager.setUndoExtraIfCancel(false);
		if (request.getExtendedData()!= null)
		{
			if (request.getExtendedData().get("initial") != null)
				manager.setInitialValue((String)request.getExtendedData().get("initial"));
			if (request.getExtendedData().get("undolast") != null)
				manager.setUndoExtraIfCancel(true);
		}
		*/

		manager.show();
		//manager.initSelection();
	}

}


