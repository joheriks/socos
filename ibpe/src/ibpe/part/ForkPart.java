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


public abstract class ForkPart<F extends IFigure & VisibleDisplayMode> extends GraphNodePart<Fork> 
{
	@Override
	abstract protected F createFigure(); 
	
	
	@Override
	public F getFigure() { return (F)super.getFigure(); }
	
	
	@Override
	protected void createEditPolicies()
	{
		super.createEditPolicies();
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,new MoveForkEditPolicy());
	
		// This is a bit of a hack: direct edit allows double clicking on the fork to toggle between
		// CHOICE and IF type.
		//installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,new EditTextPolicy());
		/*
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
				new  DirectEditPolicy ()
				{
					@Override
					protected Command getDirectEditCommand(DirectEditRequest request) 
					{
						Fork f = ((ForkPart)super.getHost()).getModel();
						return new SetChoiceTypeCommand(f,f.getChoiceType().equals(Fork.CHOICE) ? Fork.IF : Fork.CHOICE);
					}

					@Override
					protected void showCurrentEditValue(DirectEditRequest request) {}
				});
				*/
	}
	
	
	@Override
	public List<Element> getModelChildren()
	{
		return getModel().getChildren();
	}
	
	
	@Override
	protected void refreshVisuals()
	{
		super.refreshVisuals();
		
		Point p = getRootBoxContainer().toAbsolute(getModel().getPosition());
		Dimension d = getFigure().getPreferredSize();
		Rectangle r = new Rectangle(p.getTranslated(d.getScaled(0.5).getNegated()),d);
		getFigure().setBounds(r);

		Rectangle bounds = new Rectangle(r);
        bounds.translate(getRootBoxContainer().getFigure().getBounds().getLocation().getNegated());
		getRootBoxContainer().getFigure().setConstraint(getFigure(),bounds);

	}

	
	@Override
	public LabelFigure getLabel() 
	{
		return null;
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(Fork.PROPERTY_FORK_POSITION))
			refreshVisuals();
	}

	
	@Override
	public void performDirectEditRequest( DirectEditRequest request )
	{
		super.performDirectEditRequest(request);
	}
	
	
	@Override
	public void clearMarkers()
	{
		super.clearMarkers();
		getFigure().setToolTip(null);
		getFigure().setDisplayMode(DisplayMode.NORMAL);
		refreshVisuals();
	}

	@Override
	public void setToolTip( IFigure f )
	{
		getFigure().setToolTip(f);
	}

	
	@Override
	public void showMarker( IMarker mr )
	{
		super.showMarker(mr);
		
		int sev = getMarkerCombinedSeverity();
		if (sev==IMarker.SEVERITY_ERROR)
			getFigure().setDisplayMode(DisplayMode.ERROR);
		else
			getFigure().setDisplayMode(DisplayMode.WARNING);
			
		refreshVisuals();
	}
}
