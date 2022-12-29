package ibpe.part;

import java.beans.PropertyChangeEvent;
import java.util.List;

import ibpe.commands.*;
import ibpe.editpolicies.*;
import ibpe.figure.*;
import ibpe.model.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;


public class MultiCallPart extends ForkPart<MultiCallFigure>
{
	@Override
	protected MultiCallFigure createFigure() 
	{
		return new MultiCallFigure();
	}
	
	@Override
	protected void createEditPolicies()
	{
		super.createEditPolicies();
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,new EditTextPolicy());
	}
	
	@Override
	public LabelFigure getLabel() 
	{
		return ((MultiCallFigure)getFigure()).getLabel();
	}
	
	
	@Override
	public void refreshVisuals() {
		super.refreshVisuals();
		getLabel().setText(getModel().getText());
	}
}
