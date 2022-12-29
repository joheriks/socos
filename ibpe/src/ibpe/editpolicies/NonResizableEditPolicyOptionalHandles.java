package ibpe.editpolicies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.*;
import org.eclipse.gef.editpolicies.*;
import org.eclipse.gef.handles.*;
import org.eclipse.gef.tools.*;

public class NonResizableEditPolicyOptionalHandles extends NonResizableEditPolicy
{
	private boolean showCornerHandles;
	
	
	public NonResizableEditPolicyOptionalHandles( boolean handles )
	{
		showCornerHandles = handles;
	}
	
	
	/* Removes the selection handles for non-resizable selection rectangles */
	
	@Override
	protected List<Handle> createSelectionHandles()
	{
	 	if (showCornerHandles)
	 		return super.createSelectionHandles();

	 	// Code adapted from base class
	 	List<Handle> list = new ArrayList<Handle>();
	 	NonResizableHandleKit.addMoveHandle((GraphicalEditPart)getHost(),
	 									    list,
	 									    new SelectEditPartTracker(getHost()), 
	 									    SharedCursors.ARROW);
	 	
	 	return list;
	}   
	

}
