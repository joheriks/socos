package ibpe;

import ibpe.model.*;
import ibpe.part.*;

import java.util.*;

import org.eclipse.gef.*;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.actions.ActionFactory;

public class IBPEditorKeyHandler extends GraphicalViewerKeyHandler 
{	
	protected IBPEditor editor;
	
	public IBPEditorKeyHandler(IBPEditor ed) 
	{
		super(ed.getGraphicalViewer());
		editor = ed; 	
	}
	
	private boolean isContext(EditPart part)
	{
		return part.getModel() instanceof Context; 
	}
	
	/**
	 *  Returns a request to start editing the focused element.
	 *  startValue may be null.
	 */
	protected Request startDirectEdit( String startValue )
	{
		// If multiple elements are selected, refuse directedit
		if (editor.getGraphicalViewer().getSelectedEditParts().size()>1)
			return null;
		
		EditPart focusedpart = getFocusEditPart();
		
		List<?> children = focusedpart.getChildren();
		assert ((children.isEmpty() || focusedpart instanceof AbstractDirectEditPart<?>));
		
		Request request = new DirectEditRequest(RequestConstants.REQ_DIRECT_EDIT);
		if (startValue!=null)
		{
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("initial", startValue);
			request.setExtendedData(map);
		}
		return request;
	}
		
	protected boolean performAction( String id ) 
	{
		IAction action = editor.getActionRegistry().getAction(id);
		if (action == null || !action.isEnabled())
			return false;
		action.run();
		return true;
	}
	
	/** Finds the first editpart for which isCompartment()==false in an inorder traversal
	  * starting from parent[index]. dir should be -1 or 1, for forward and backward traversal,
	  * respectively. Backtracks supplied amount of levels. */
	private AbstractIBPEditPart<?> nextNonCompartmentPart( AbstractIBPEditPart<?> parent, 
			                                               int index, 
			                                               int dir, 
			                                               int backtrack )
	{
		assert -1 <= index && index <= parent.getChildren().size();
		assert dir==-1 || dir==1;
		assert 0<=backtrack;
		
		for (int i=index; 0<=i && i<parent.getChildren().size(); i+=dir)
		{
			AbstractIBPEditPart<?> part = (AbstractIBPEditPart<?>)parent.getChildren().get(i);
			if (!part.isCompartment()) return part;
			part = nextNonCompartmentPart(part,dir==1?0:part.getChildren().size()-1,dir,backtrack);
			if (part!=null) return part;
		}
		if (!isContext(parent) && backtrack>0 && parent.getParent() instanceof AbstractIBPEditPart<?>)
			return nextNonCompartmentPart((AbstractIBPEditPart<?>)parent.getParent(),
										  parent.getParent().getChildren().indexOf(parent)+dir,dir,backtrack-1);
		else
			return null;
	}
		
	@Override
	public boolean keyPressed(KeyEvent event)	
	{
		EditPart focusedpart = getFocusEditPart();
		// NB: focusedpart must not be the Context part.
		
		// INSERT: Add a new declaration before the currently selected element
		if (event.keyCode==SWT.INSERT) return performAction("NEW_DECL_BEFORE");
		
		// ENTER: Add a new declaration after the currently selected element.
		// If on a transition, add new statement at end.
		if (event.keyCode==SWT.CR)
		{
			if (focusedpart instanceof TransitionPart &&
				focusedpart.getChildren().size()>0)
			{
				navigateTo((EditPart)(focusedpart.getChildren().get(0)),event);
				return performAction("NEW_DECL_AFTER");
				//System.out.println(focusedpart.getChildren().get(0).getChildren());
			}
			return performAction("NEW_DECL_AFTER");
		}

		if (event.keyCode == SWT.DEL) 
			return performAction(ActionFactory.DELETE.getId());

		// HOME: Go to first child. 
		if (event.keyCode==SWT.HOME)
		{
			navigateTo((EditPart)(focusedpart.getParent().getChildren().get(0)),event);
			return true;
		}
		
		// END: Go to last child.
		if (event.keyCode==SWT.END)
		{
			EditPart parent =(EditPart)focusedpart.getParent(); 
			navigateTo((EditPart)parent.getChildren().get(parent.getChildren().size()-1),event);
			return true;
		}

		// ARROW_RIGHT: If on a TextRow, start editing it. If on a compound item, move to the first
		// child, 
		if (event.keyCode == SWT.ARROW_RIGHT)
		{
			if (focusedpart instanceof TextRowPart)
			{
				Request request = startDirectEdit(null);
				if (request!=null)
					getFocusEditPart().performRequest(request);
			}
			else if (!focusedpart.getChildren().isEmpty())
			{
				EditPart next = nextNonCompartmentPart((AbstractIBPEditPart<?>)focusedpart,0,1,0);
				if (next!=null)	navigateTo(next,event);
			}
			
			return true;
		}

		// ARROW_LEFT: Navigate to the container of the current element.
		if (event.keyCode == SWT.ARROW_LEFT)
		{
			if (isContext(focusedpart) || isContext(focusedpart.getParent()))
				return true;
			AbstractIBPEditPart<?> navigateTo = (AbstractIBPEditPart<?>)focusedpart.getParent();
			/*if (navigateTo instanceof TransitionEditPart)
				navigateTo = ((TransitionEditPart)focusedpart).getSource();
			else
				navigateTo = navigateTo.getParent();*/
			
			while (((AbstractIBPEditPart<?>)navigateTo).isCompartment())
				navigateTo = (AbstractIBPEditPart<?>)navigateTo.getParent();
			
			navigateTo(navigateTo, event);
			
			return true;
		}
		
		// ARROW_UP: Navigate to preceding child. If on first child, focus previous uncle, unless we
		// are in a compartmentalized container, in which case we go the last child of the uncle instead.
		if (event.keyCode == SWT.ARROW_UP)
		{
			if (isContext(focusedpart) || focusedpart.getParent() == null) return true;
			EditPart parent = focusedpart.getParent();
			int index = parent.getChildren().indexOf(focusedpart);
			EditPart next = nextNonCompartmentPart((AbstractIBPEditPart<?>)parent,index-1,-1,1);
			if (next!=null)	navigateTo(next,event);
			return true;
		}
		
		// ARROW_DOWN: Navigate to succeeding child. If on last child, focus next uncle. If we are
		// on a compartmentalized container, go to the next uncle's first child (cousin).
		if (event.keyCode == SWT.ARROW_DOWN)
		{
			if (isContext(focusedpart) || focusedpart.getParent() == null) return true;
			EditPart parent = focusedpart.getParent();
			int index = parent.getChildren().indexOf(focusedpart);
			EditPart next = nextNonCompartmentPart((AbstractIBPEditPart<?>)parent,index+1,1,1);
			if (next!=null)	navigateTo(next,event);
			return true;
		}
		
		// If the key represents a valid letter or digit, start a directedit.
		// TODO: sensible to also check for special characters?
		if ( ((' '<=event.character && event.character<='~') || event.keyCode==SWT.BS) &&
			(event.stateMask==0 || event.stateMask==SWT.SHIFT) &&
			focusedpart instanceof AbstractDirectEditPart<?>)
		{
			String s = event.keyCode==SWT.BS ? "" : Character.toString(event.character);
			Request request = startDirectEdit(s);
			if (request != null) focusedpart.performRequest(request);
			return true;
		}
		
		return false;
	}	
	

	@Override
	/**
	 * Navigates to the given EditPart
	 * @param	part	the EditPart to navigate to
	 * @param	event	the KeyEvent that triggered this traversal
	 */
	protected void navigateTo(EditPart part, KeyEvent event)
	{
		if (part == null)
			return;
		if (((event.stateMask & SWT.SHIFT) != 0) &&
			((event.keyCode != SWT.ARROW_LEFT) || (event.keyCode != SWT.ARROW_RIGHT)))
		{
			getViewer().appendSelection(part);
			getViewer().setFocus(part);
		} 
		else if ((event.stateMask & SWT.CONTROL) != 0)
			getViewer().setFocus(part);
		else
			getViewer().select(part);
		
		getViewer().reveal(part);
	}
	
}