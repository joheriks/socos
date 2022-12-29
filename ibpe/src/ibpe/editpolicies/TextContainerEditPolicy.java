package ibpe.editpolicies;

import ibpe.commands.*;
import ibpe.model.*;
import ibpe.part.*;
import ibpe.io.*;

import java.util.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.*;
import org.eclipse.gef.requests.*;

public class TextContainerEditPolicy extends LayoutEditPolicy
{
	private RectangleFigure transientFigure = null;
	
	@Override
	public TextContainerPart getHost() 
	{
		return (TextContainerPart)super.getHost();
	}
	
	private SequenceFragment createSequenceFragment( List<EditPart> editParts )
	{
		// Create a list of all elements in the selection, and check at the same time that they are all
		// valid SequenceFragment elements (i.e., of type Procedure or TextRow).
		if (editParts.size()==0)
			return null;

		ArrayList<Element> elms = new ArrayList<Element>();
		for (EditPart p : editParts)
		{
			if (!(p.getModel() instanceof Procedure || p.getModel() instanceof TextRow))
				return null;
			else
				elms.add((Element)p.getModel());
		}
		return new SequenceFragment(elms);
	}

	@Override
	protected Command getCreateCommand(CreateRequest request)
	{
		if (getHost()==null)
			return null;
		if (request.getNewObjectType()==Fragment.class)
		{
			Fragment<?> frag = (Fragment<?>)request.getNewObject();	
			//EditPart after = null;
			Element afterModel = null;
			if (request.getExtendedData().containsKey("after")) 
				afterModel = (Element)request.getExtendedData().get("after");
			else if (request.getLocation()!=null)
			{
				EditPart afterPart = getInsertionReference(request.getLocation(),new ArrayList<EditPart>());
				if (afterPart!=null)
					afterModel = (Element)afterPart.getModel();
			}
			
			if (frag instanceof SequenceFragment && frag.canInsertAt((Node)getHost().getModel(),afterModel))
			{
				Command cmd = frag.adapt((Node)getHost().getModel(),afterModel);
				
				BoxElement be = (BoxElement)((Element)getHost().getModel()).getParentOfType(BoxElement.class);
				if (be==null)
					return cmd;
				else
					return cmd.chain(new BoxGrowingChildCommand(getHost().getViewer().getEditPartRegistry(),be));
			}
		}
		
		return null;		
	}

	
	protected Command getAddCloneCommand( ChangeBoundsRequest req, boolean clone )
	{
		@SuppressWarnings("unchecked")
		List<EditPart> editParts = req.getEditParts();
		SequenceFragment f = createSequenceFragment(editParts);
		if (f==null)
			return null;
		@SuppressWarnings("unchecked")
		EditPart after = getInsertionReference(req.getLocation(),clone ? new ArrayList<EditPart>() : req.getEditParts());
		Element afterModel = after==null ? null : (Element)after.getModel();
		
		if (clone) f = f.copy();
		if (f.canInsertAt(getHost().getModel(),afterModel))
		{
			Command cmd1 = f.adapt(getHost().getModel(),afterModel);

			BoxElement be = (BoxElement)((Element)getHost().getModel()).getParentOfType(BoxElement.class);
			if (be==null)
				return cmd1;
			else
				return cmd1.chain(new BoxGrowingChildCommand(getHost().getViewer().getEditPartRegistry(),be));
		}
		return null;
	}
	

	@Override
	protected Command getAddCommand(Request request) 
	{
		return getAddCloneCommand((ChangeBoundsRequest)request,false);
	}
	
	
	@Override
	protected Command getCloneCommand(ChangeBoundsRequest request) 
	{
		return getAddCloneCommand((ChangeBoundsRequest)request,true);
	}
	
	
	@Override
	protected EditPolicy createChildEditPolicy(EditPart child)
	{
		return null;
	}
	

	protected EditPart getInsertionReference( Point point, List<EditPart> exclude )
	{
		EditPart retval = null;
		Point p = point.getCopy();
		((GraphicalEditPart)getHost().getRoot()).getFigure().translateFromParent(p);

		for (Object o : getHost().getChildren())
		{
			GraphicalEditPart part = (GraphicalEditPart)o;
			Rectangle r = part.getFigure().getBounds();
			
			if (r.y + r.height/2 > p.y &&
			    (part instanceof TextRowPart || part instanceof ProcedurePart)) 
			    break;
			if (!exclude.contains(part))
				retval = part;
		}
		return retval;
	}
	

	/**
	 *  Render a transient on the feedback layer to indicate where the element will be inserted.
	 *  Also handles create requests.
	 */
	@Override
	protected void showLayoutTargetFeedback(Request request)
	{
		Fragment<?> frag;
		Point location;
		List<EditPart> exclude = new ArrayList<EditPart>();
		if (request instanceof ChangeBoundsRequest)
		{
			ChangeBoundsRequest req = (ChangeBoundsRequest)request;
			frag = createSequenceFragment(req.getEditParts());
			location = req.getLocation();
			if (request.getType()==RequestConstants.REQ_CLONE)
				exclude.addAll(req.getEditParts());
		}
		else if (request instanceof CreateRequest && ((CreateRequest)request).getNewObjectType()==Fragment.class)
		{
			CreateRequest req = (CreateRequest)request;
			frag = (Fragment<?>)req.getNewObject();
			location = req.getLocation();
		}
		else
			return;
		
		if (!(frag instanceof SequenceFragment)) 
			return;
		
		
		if (transientFigure == null)
		{
			transientFigure = new RectangleFigure();
			transientFigure.setForegroundColor(ColorConstants.darkGray);
			transientFigure.setBackgroundColor(ColorConstants.darkGray);
		}
		
		IFigure fig = getHostFigure();
				
		transientFigure.setSize(Math.min(32,fig.getSize().width),2);

		IFigure layer = getLayer(LayerConstants.FEEDBACK_LAYER);
		if (!layer.getChildren().contains(transientFigure))
			layer.add(transientFigure);
		
		GraphicalEditPart after = (GraphicalEditPart)getInsertionReference(location,exclude);
		if (after==null)
			transientFigure.setLocation(fig.getBounds().getTopLeft().getCopy().getTranslated(1,1));
		else
			transientFigure.setLocation(after.getFigure().getBounds().getBottomLeft());
	}
	
	
	@Override
	protected void eraseLayoutTargetFeedback(Request request)
	{
		IFigure layer = getLayer(LayerConstants.FEEDBACK_LAYER);
		if (transientFigure != null && layer.getChildren().contains(transientFigure))
			layer.remove(transientFigure);
	}

	@Override
	protected Command getMoveChildrenCommand(Request request) 
	{
		return null;
	}
	
}