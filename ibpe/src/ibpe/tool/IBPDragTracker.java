package ibpe.tool;

import ibpe.part.*;

import java.util.*;

import org.eclipse.gef.*;
import org.eclipse.gef.tools.DragEditPartsTracker;

public class IBPDragTracker extends DragEditPartsTracker 
{

	public IBPDragTracker(EditPart sourceEditPart) 
	{
		super(sourceEditPart);
	}
	
	@Override
	protected boolean isMove() 
	{ 
		if (getSourceEditPart() instanceof TransitionLabelPart ||
			getSourceEditPart() instanceof ForkPart)
			return true;
		// Return false to force reparenting even when the move occurs within the
		// children of a container. This is a bit of a hack to get uniform handling of
		// all moves in the editpolicies.
		return false; 
	}

	/*
	protected String getCommandName() {
		if (isCloneActive())
			return REQ_CLONE;
		else if (isMove() || getSourceEditPart() instanceof TransitionLabelPart)
			return REQ_MOVE;
		else
			return REQ_ADD;
	}
	*/
	
	
	/*
	@SuppressWarnings("unchecked")
	protected Command getCommand()
	{
		CompoundCommand command = new CompoundCommand();
		command.setDebugLabel("Drag Object Tracker");

		Request request = getTargetRequest();
		
		if (isCloneActive())
			request.setType(REQ_CLONE);
		else if (isMove() || getSourceEditPart() instanceof TransitionLabelPart)
			request.setType(REQ_MOVE);
		else
			request.setType(REQ_ORPHAN);
		
		List<AbstractIBPEditPart<?>> ibpparts = new LinkedList<AbstractIBPEditPart<?>>();
		for (Object o : getOperationSet())
		{
			if (o instanceof AbstractIBPEditPart<?>)
				ibpparts.add((AbstractIBPEditPart<?>) o);
		}
		
		List<EditPart> sortedObjects = new LinkedList<EditPart>();
		sortedObjects.addAll(getOperationSet());
		sortedObjects.removeAll(ibpparts);
		Collections.sort(ibpparts, new IBPEAbstractEditPartSortByIndex());
		sortedObjects.addAll(0, ibpparts);
		
		if (!isCloneActive() && !request.getType().equals(REQ_ORPHAN))
		{
			for (int i = sortedObjects.size()-1; i >= 0; i--)
			{
				EditPart child = (EditPart) sortedObjects.get(i);
				command.add(child.getCommand(request));
			}
		}
		else if (request.getType().equals(REQ_ORPHAN))
			command.add(getTargetEditPart().getCommand(getTargetRequest()));
		
		if (!(isMove() || getSourceEditPart() instanceof TransitionLabelPart) || isCloneActive())
		{
			if (!isCloneActive())
				request.setType(REQ_ADD);
			
			if (getTargetEditPart() == null)
				command.add(UnexecutableCommand.INSTANCE);
			else 
				command.add(getTargetEditPart().getCommand(getTargetRequest()));
		}
		
		return command;
	}
	*/
	
	@Override
	protected void updateAutoexposeHelper()
	{
		AutoexposeHelper.Search search;
		search = new AutoexposeHelper.Search(getLocation());
		getCurrentViewer().findObjectAtExcluding(getLocation(), Collections.EMPTY_LIST, search);
		setAutoexposeHelper(search.result);
	}

	
	@Override
	protected boolean updateTargetUnderMouse()
	{
		if (!isTargetLocked())
		{
			List<?> selection = getOperationSet();
			EditPart editPart = null;
			for (Object o : selection)
			{
				if (o instanceof TextRowPart)
				{
					Collection<EditPart> exclude = new LinkedList<EditPart>();
					exclude.add(getCurrentViewer().getContents());
					editPart = getCurrentViewer().findObjectAtExcluding(getLocation(),
																		exclude,
																		getTargetingConditional());
					break;
				}
			}
			if (editPart == null)
				editPart = getCurrentViewer().findObjectAtExcluding(getLocation(),
																	getExclusionSet(),
																	getTargetingConditional());
			if (editPart != null)
				editPart = editPart.getTargetEditPart(getTargetRequest());
			
			boolean changed = getTargetEditPart() != editPart;
			setTargetEditPart(editPart);
			
			return changed;
		}
		else
			return false;
	}
	

	protected void performSelection()
	{
		if (hasSelectionOccurred())
			return;
		
		setFlag(FLAG_SELECTION_PERFORMED, true);
		EditPartViewer viewer = getCurrentViewer();

		if (getCurrentInput().isShiftKeyDown() && getCurrentInput().isControlKeyDown())
			viewer.deselect(getSourceEditPart());
		else if (getCurrentInput().isShiftKeyDown())
			viewer.appendSelection(getSourceEditPart());
		else
			viewer.select(getSourceEditPart());
	}
	
	@Override
	protected boolean handleDoubleClick(int button)
	{
		if (getSourceEditPart() instanceof AbstractDirectEditPart<?>)
		{
			getSourceEditPart().performRequest(new Request(RequestConstants.REQ_DIRECT_EDIT));
			return true;
		}
		
		return super.handleDoubleClick(button);
	}
}
