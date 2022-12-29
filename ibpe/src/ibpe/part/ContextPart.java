package ibpe.part;

import ibpe.*;
import ibpe.figure.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.draw2d.*;
import org.eclipse.gef.*;

public class ContextPart extends TextContainerPart 
{
	@Override
	protected TextContainerFigure createFigure()
	{
		TextContainerFigure f = new TextContainerFigure(4,4,false);

		// A ContainerFigure is transparent by default, but since this is the topmost figure we set it
		// to be opaque.
		f.setBackgroundColor(ColorConstants.white);
		f.setOpaque(true);

		// Set the font 
		f.setFont(((IBPEditor)((DefaultEditDomain)this.getViewer().getEditDomain()).getEditorPart()).getFont());
		
		return f;
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
	public void showMarker( IMarker mr )
	{
		super.showMarker(mr);
		
		int sev = getMarkerCombinedSeverity();
		if (sev==IMarker.SEVERITY_ERROR)
			getFigure().setDisplayMode(DisplayMode.ERROR);
		else
			getFigure().setDisplayMode(DisplayMode.WARNING);
			
		final IFigure f = getMarkerCombinedToolTip();
		getFigure().setToolTip(f);
		
		refreshVisuals();
	}
	
	
	
}
