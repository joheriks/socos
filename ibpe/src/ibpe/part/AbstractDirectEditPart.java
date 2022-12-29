package ibpe.part;

import ibpe.directedit.*;
import ibpe.figure.*;
import ibpe.model.*;

import java.beans.PropertyChangeEvent;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.*;
import org.eclipse.gef.requests.DirectEditRequest;


public abstract class AbstractDirectEditPart<E extends Element> extends AbstractIBPEditPart<E> 
{
	protected ExtendedDirectEditManager manager;

	// Should return the interaction label figure for directedit.
	// May return null, in which case directedit is refused.
	public abstract LabelFigure getLabel();


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
		
		if (manager == null)
			manager = new ExtendedDirectEditManager(this,new NameCellEditorValidator(getModel()));
		manager.setInitialValue(null);
		manager.setUndoExtraIfCancel(false);
		if (request.getExtendedData()!= null)
		{
			if (request.getExtendedData().get("initial") != null)
				manager.setInitialValue((String)request.getExtendedData().get("initial"));
			if (request.getExtendedData().get("undolast") != null)
				manager.setUndoExtraIfCancel(true);
		}
		manager.show();
		manager.initSelection();
	}


	public boolean directEditHitTest(Point requestLoc)
	{
		if (getLabel() == null) return false;
		Point p = requestLoc.getCopy();
		getLabel().translateToRelative(p);
		return getLabel().containsPoint(p);
	}
	
	@Override
	public void clearMarkers()
	{
		super.clearMarkers();
		if (getLabel()!=null)
		{
			getLabel().setToolTip(null);
			getLabel().setDisplayMode(DisplayMode.NORMAL);
		}
		refreshVisuals();
	}
	

	public void setToolTip( IFigure f )
	{
		System.out.println("Override setTooltip");
		if (getLabel()!=null)
			getLabel().setToolTip(f);
	}

	
	@Override
	public void showMarker( IMarker mr )
	{
		super.showMarker(mr);
		
		if (getLabel()!=null)
		{
			int sev = getMarkerCombinedSeverity();
			if (sev==IMarker.SEVERITY_ERROR)
				getLabel().setDisplayMode(DisplayMode.ERROR);
			else
				getLabel().setDisplayMode(DisplayMode.WARNING);
		}
		setToolTip(getMarkerCombinedToolTip());
		refreshVisuals();
	}
}
