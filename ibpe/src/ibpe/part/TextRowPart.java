package ibpe.part;

import ibpe.directedit.*;
import ibpe.editpolicies.*;
import ibpe.figure.*;
import ibpe.model.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.draw2d.*;
import org.eclipse.gef.*;


public class TextRowPart extends AbstractDirectEditPart<TextRow> 
{
	
	protected ExtendedDirectEditManager manager;
	protected ErrorFigure error;
	
	@Override 
	protected IFigure createFigure()
	{
		return new TextRowFigure();
	}
	

	@Override
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new DeleteElementEditPolicy());
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableEditPolicyOptionalHandles(false));
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,new EditTextPolicy());
	}
	

	@Override
	public LabelFigure getLabel() 
	{
		return ((TextRowFigure)getFigure()).getLabel();
	}
	
	
	/*
	@Override
	public DragTracker getDragTracker(Request request)
	{
		SelectionRequest sr = (SelectionRequest) request;
		Point click = sr.getLocation();
		Rectangle textFigBounds = text.getBounds().getCopy();
		EditPart parent = getParent();
		text.translateToRelative(click);
		
		if(sr.isControlKeyPressed() && !sr.isShiftKeyPressed())
		{
			if(parent instanceof CallPart)
				return parent.getDragTracker(request);
			else
				return parent.getParent().getDragTracker(request);
		}
		else if(!textFigBounds.contains(click))
		{
			if(parent instanceof CallPart)
			{
				getViewer().select(parent);
				return new IBPDragTracker(parent)
				{
					@Override
					protected boolean updateTargetUnderMouse()
					{
						if (!isTargetLocked())
						{
							Collection<EditPart> exclude = new LinkedList<EditPart>();
							exclude.add(getCurrentViewer().getContents());
							EditPart editPart = getCurrentViewer()
													.findObjectAtExcluding(getLocation(),
																		   exclude,
																		   getTargetingConditional());
							if (editPart == null)
							{
								editPart = getCurrentViewer()
												.findObjectAtExcluding(getLocation(),
																	   getExclusionSet(),
																	   getTargetingConditional());
							}
							if (editPart != null)
								editPart = editPart.getTargetEditPart(getTargetRequest());
							
							boolean changed = getTargetEditPart() != editPart;
							setTargetEditPart(editPart);
							
							return changed;
						}
						else
							return false;
					}
				};
			}
			else
				return new IBPDragTracker(parent.getParent());
		}
		else
			return super.getDragTracker(request);
	}
	*/

}