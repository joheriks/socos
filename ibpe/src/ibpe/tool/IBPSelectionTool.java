/** , 
 *  extension of the default selection tool, 
 *  that uses a sort of marquee selection when dragging 
 *  in the left side of a container 
 */
package ibpe.tool;

import ibpe.IBPGraphicalViewer;
import ibpe.figure.IfChoiceFigure;
import ibpe.part.*;

import java.util.*;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Handle;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.ConnectionEndpointTracker;
import org.eclipse.gef.tools.PanningSelectionTool;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MouseEvent;


public class IBPSelectionTool extends PanningSelectionTool
{
	static final int DEFAULT_MODE = 0;
	static final int TOGGLE_MODE = 1;
	static final int APPEND_MODE = 2;
	static final int DRAG_MARGIN = 10;

	private List<EditPart> selectedEditParts;
	private List<EditPart> oldSelected = new ArrayList<EditPart>();
	private Request targetRequest;
	private int mode;
	private boolean dragSelect = false;

	private ArrayList<EditPart> selectedPart = new ArrayList<EditPart>();

	public IBPSelectionTool() {
		setUnloadWhenFinished(false);
	}

	@SuppressWarnings("unchecked")
	private void calculateNewSelection()
	{
		oldSelected.clear();
		
		if (selectedEditParts !=  null)
			oldSelected.addAll(selectedEditParts);
		if (selectedEditParts == null || mode == DEFAULT_MODE)
			selectedEditParts = new ArrayList<EditPart>();
		
		GraphicalEditPart container = getCurrentContainerPart();
		if (container != null)
		{
			for (Iterator<EditPart> itr = container.getChildren().iterator(); itr.hasNext();)
			{
				GraphicalEditPart child = (GraphicalEditPart) itr.next();
				if (isValid(child))
					selectedEditParts.add(child);
			}
		}
	}

	/**
	 * Erases feedback if necessary and puts the tool into the terminal state.
	 */
	public void deactivate()
	{
		if (isInState(STATE_DRAG_IN_PROGRESS))
			eraseTargetFeedback();
		
		super.deactivate();
		setState(STATE_TERMINAL);
	}

	@Override
	protected void showTargetFeedback()
	{
		if (selectedEditParts == null)
			return;
		for (Iterator<EditPart> itr = selectedEditParts.iterator(); itr.hasNext();)
		{
			EditPart editPart = (EditPart) itr.next();
			editPart.showTargetFeedback(getTargetRequest());
		}
	}


	@Override
	protected void eraseTargetFeedback()
	{
		if (selectedEditParts == null)
			return;
		
		Iterator<EditPart> oldEditParts = selectedEditParts.iterator();
		while (oldEditParts.hasNext())
		{
			EditPart editPart = (EditPart) oldEditParts.next();
			editPart.eraseTargetFeedback(getTargetRequest());
		}
		selectedEditParts = null;
	}

	// Returns the container of the currently selected part
	private GraphicalEditPart getCurrentContainerPart()
	{
		GraphicalViewer viewer = (GraphicalViewer) getCurrentViewer();
		GraphicalEditPart part = (GraphicalEditPart) viewer.findObjectAt(getLocation());
		while (part != null && !(part instanceof TextContainerPart))
			part = (GraphicalEditPart) part.getParent();
		
		return part;
	}

	private Rectangle getSelectionRectangle() {
		return new Rectangle(getStartLocation(), getLocation());
	}

	@Override
	protected Request getTargetRequest()
	{
		if (targetRequest == null)
			targetRequest = createTargetRequest();
		
		return targetRequest;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#handleButtonDown(int)
	 */
	@SuppressWarnings("unchecked")
	protected boolean handleButtonDown(int button)
	{
		if (!(getCurrentViewer() instanceof IBPGraphicalViewer) || button!=1)
		{
			super.handleButtonDown(button);
		}

		EditPart target = getTargetEditPart();

		// special code to allow immediate dragging of fork figures (no selection by clicking required)   
		if(((IBPGraphicalViewer)getCurrentViewer()).forkFigureAt(getLocation()))
		{
			if(target instanceof TransitionPart)
			{
				ConnectionEndpointTracker tracker = new ConnectionEndpointTracker((TransitionPart)target);
				tracker.setCommandName(RequestConstants.REQ_RECONNECT_SOURCE);
				setDragTracker(tracker);
				return true;
			}
		}

		// Handle multiple selection with shift key down
		if (getCurrentInput().isShiftKeyDown())
		{
			if (getCurrentInput().isControlKeyDown())
			{
				getCurrentViewer().deselect(target);
				return super.handleButtonDown(button);
			}
				
			dragSelect = false;
			/*if (!(target instanceof BoxContainerPart) &&
				!(target instanceof TransitionPart) &&
				!(target instanceof BoxElementPart))
			{
				mode = APPEND_MODE;
				EditPartViewer viewer = getCurrentViewer();
				if (calculateMultipleSelection(viewer.getSelectedEditParts(), viewer))
					viewer.setSelection(new StructuredSelection(selectedPart));
				
				return true;
			}
			else*/
				
			return super.handleButtonDown(button);
		}
		if (inDragSelectArea() &&
			stateTransition(STATE_INITIAL, STATE_DRAG_IN_PROGRESS))
		{
			if (getCurrentInput().isShiftKeyDown())
				mode = APPEND_MODE;
			else
			{
				mode = DEFAULT_MODE;
				getCurrentViewer().setSelection(new StructuredSelection());
			}
			dragSelect = true;
			return true;
		}
		dragSelect = false;
		selectedEditParts = new ArrayList<EditPart>();
		
		return super.handleButtonDown(button);
	}

	@SuppressWarnings("unchecked")
	private boolean calculateMultipleSelection(List<EditPart> currentSelecteds, EditPartViewer viewer)
	{
		int start, end, selectionPartIndex, previousSelectionPartIndex;
		EditPart parentPart = null;
		EditPart previousSelectedPart = currentSelecteds.get(0);
		List<EditPart> children = null;

		selectedPart.clear();

		EditPart selectionPart = (GraphicalEditPart) viewer.findObjectAt(getLocation());

		// Check if current selection is on a higher level than previous selected 
		// Set selection to same level if that is the case.
		do
		{
			parentPart = (GraphicalEditPart) selectionPart.getParent();
			if (!(previousSelectedPart.getParent().equals(parentPart)))
				selectionPart = selectionPart.getParent();
		} while (parentPart != null &&
				 !(previousSelectedPart.getParent().equals(parentPart)));
		
		// Check if selection is on a lower level than the old selection break if true  
		if (parentPart == null)
			return false;
		
		children = parentPart.getChildren();
		selectionPartIndex = children.indexOf(selectionPart);
		previousSelectionPartIndex = children.indexOf(previousSelectedPart);
		
		// Calculate new part to be selected 
		if (previousSelectionPartIndex > selectionPartIndex)
		{
			EditPart tPart = currentSelecteds.get(currentSelecteds.size() - 1);
			start = selectionPartIndex;
			end = children.indexOf(tPart);
		}
		else
		{
			start = previousSelectionPartIndex;
			end = selectionPartIndex;
		}
		for (int i = start; i <= end; i++)
			selectedPart.add(children.get(i));
		
		return true;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#handleButtonUp(int)
	 */
	protected boolean handleButtonUp(int button)
	{
		if (stateTransition(STATE_DRAG_IN_PROGRESS, STATE_TERMINAL) && dragSelect)
		{
			eraseTargetFeedback();
			performSelect();
			handleFinished();
			dragSelect = false;
			
			return true;
		}
		else
			return super.handleButtonUp(button);
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#handleDragInProgress()
	 */
	protected boolean handleDragInProgress()	
	{
		if (isInState(STATE_DRAG | STATE_DRAG_IN_PROGRESS) && dragSelect)
		{
			showDragFeedBack();
			return true;
		}
		else
			return false;
	}

	protected boolean handleInvalidInput()
	{
		eraseTargetFeedback();
		return true;
	}

	// Checks if the user is dragging in the left part of the current ContainerPart
	private boolean inDragSelectArea()
	{
		GraphicalEditPart part = getCurrentContainerPart();
		
		// We skip left-pane dragging for transitions, since it would interact
		// annoyingly with label movement.
		if (part instanceof TextContainerPart && !(part.getParent() instanceof TransitionPart)) 
		{
			IFigure figure = part.getFigure();
			Rectangle f = figure.getBounds().getCopy();
			figure.translateToAbsolute(f);
			Rectangle r = new Rectangle(f.x, f.y, DRAG_MARGIN, f.height);
			
			return r.contains(getLocation());
		}
		else
			return false;
	}

	private boolean isFigureVisible(IFigure fig)
	{
		Rectangle figBounds = fig.getBounds().getCopy();
		IFigure walker = fig.getParent();
		while (!figBounds.isEmpty() && walker != null)
		{
			walker.translateToParent(figBounds);
			figBounds.intersect(walker.getBounds());
			walker = walker.getParent();
		}
		
		return !figBounds.isEmpty();
	}

	private boolean isGraphicalViewer() {
		return getCurrentViewer() instanceof GraphicalViewer;
	}

	// Checks if the editpart should be selected by checking if they're inside the drag
	private boolean isValid(EditPart editPart)
	{
		if (editPart instanceof GraphicalEditPart)
		{
			Rectangle marqueeRect = getSelectionRectangle();
			IFigure figure = ((GraphicalEditPart) editPart).getFigure();

			// Can't copy invisible or otherwise unselectable parts
			if (!editPart.isSelectable() ||
				editPart.getTargetEditPart(getTargetRequest()) != editPart ||
				!isFigureVisible(figure) ||
				!figure.isShowing())
			{
				return false;
			}

			Rectangle r = figure.getBounds().getCopy();
			figure.translateToAbsolute(r);
			boolean included = false;
			
			// If the EditPart isn't completely above or below the drag
			// and amongst the currently selected part's children, return true.
			if (!(marqueeRect.y > r.y + r.height) &&
				!(marqueeRect.y + marqueeRect.height < r.y))
			{
				return getCurrentContainerPart().getChildren().contains(editPart);
			}
			else
				included = false;

			return included;
		}
		return false;
	}

	private void performSelect()
	{
		EditPartViewer viewer = getCurrentViewer();
		calculateNewSelection();
		viewer.setSelection(new StructuredSelection(selectedEditParts));
	}

	@Override
	public void setViewer(EditPartViewer viewer)
	{
		if (viewer == getCurrentViewer())
			return;
		
		super.setViewer(viewer);
		if (viewer instanceof GraphicalViewer)
			setDefaultCursor(SharedCursors.ARROW);
		else
			setDefaultCursor(SharedCursors.NO);
	}

	private void showDragFeedBack()
	{
		calculateNewSelection();

		oldSelected.removeAll(selectedEditParts);
		for (Iterator<EditPart> itr = selectedEditParts.iterator(); itr.hasNext();)
		{
			EditPart editPart = (EditPart) itr.next();
			Request r = new Request();
			r.setType("DRAG_SELECT");
			editPart.showTargetFeedback(r);
		}
		for (Iterator<EditPart> itr = oldSelected.iterator(); itr.hasNext();)
		{
			EditPart editPart = (EditPart) itr.next();
			editPart.eraseTargetFeedback(getTargetRequest());
		}
	}

}
