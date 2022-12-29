package ibpe.editpolicies;

import ibpe.commands.*;
import ibpe.io.*;
import ibpe.model.*;
import ibpe.part.*;

import java.util.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.*;
import org.eclipse.gef.requests.*;


public class BoxContainerEditPolicy extends LayoutEditPolicy  
{
	@Override
	public BoxContainerPart getHost() 
	{
		return (BoxContainerPart)super.getHost();
	}

	@Override
	public Command getCommand(Request request)
	{
		if (REQ_RESIZE_CHILDREN.equals(request.getType()))
			return getResizeCommand((ChangeBoundsRequest)request);
			
		else
			return super.getCommand(request);
	}
	
	@Override
	public boolean understandsRequest( Request request )
	{
		return REQ_CREATE.equals(request.getType()) ||
			   REQ_RESIZE_CHILDREN.equals(request.getType());
	}


	@Override
	protected Command getCreateCommand(CreateRequest request)
	{
		if (!(request.getNewObject() instanceof DiagramFragment))
			return null;
		
		BoxContainer bc = (BoxContainer)getHost().getModel();
		DiagramFragment frag = (DiagramFragment)request.getNewObject();
		
		if (frag==null || !frag.canInsertAt(bc,null))
			return null;
	
		CompoundCommand cmd = new CompoundCommand();
		
		cmd.add(frag.adapt(bc,null));
		
		Point pos = request.getLocation().getCopy();
		// translate to coordinates relative to figure bounds 
		getHost().getContentPane().translateToRelative(pos);
		// translate to figure local coordinates
		pos = pos.getTranslated(getHost().getContentPane().getBounds().getTopLeft().getNegated());

		cmd.add(new BoxClearAreaCommand(getHost().getViewer().getEditPartRegistry(),
                                        frag.getBoxElements(),getHost().getModel(),pos,null));

		cmd.add(new BoxMoveCommand(frag.getBoxElements(),pos,null));
		cmd.add(new BoxMoveIntoPositiveCommand(getHost().getViewer().getEditPartRegistry(),getHost().getModel()));

		BoxElement be = (BoxElement)bc.getParentOfType(BoxElement.class);
		if (be!=null)
			cmd.add(new BoxGrowingChildCommand(getHost().getViewer().getEditPartRegistry(),be));
		
		return cmd;
	}


	private Command getResizeCommand(ChangeBoundsRequest request)
	{
		if (request.getEditParts().size()!=1)
			return null;
		
		BoxElementPart<?> target = (BoxElementPart<?>)request.getEditParts().get(0);
		
		CompoundCommand cmd = new CompoundCommand();
		
		Rectangle rect = request.getTransformedRectangle(target.getModelBounds());
		Dimension minSize = target.getFigure().getMinimumSize();
		rect.union(minSize);
		
		List<BoxElement> l = new ArrayList<BoxElement>();
		l.add(target.getModel());
		cmd.add(new BoxClearAreaCommand(getHost().getViewer().getEditPartRegistry(),l,getHost().getModel(),null,rect));

		cmd.add(new BoxResizeCommand(target.getModel(),rect));
		
		cmd.add(new BoxMoveIntoPositiveCommand(getHost().getViewer().getEditPartRegistry(),getHost().getModel()));
		
		if (getHost().getParent() instanceof BoxElementPart<?>)
			cmd.add(new BoxGrowingChildCommand(getHost().getViewer().getEditPartRegistry(),
					(BoxElement)getHost().getParent().getModel()));

		return cmd;
	}


	public Command getAddCommand(Request req)
	{
		ChangeBoundsRequest request = (ChangeBoundsRequest)req;
		
		CompoundCommand cmd = new CompoundCommand();
		
		if (request.getEditParts().size()==0)
			return null;

		BoxContainerPart parentPart = null;
		BoxContainer newParent = ((BoxContainerPart)getHost()).getModel();
		Rectangle rect = null;
		List<BoxElement> l = new ArrayList<BoxElement>();
		for (Object o : request.getEditParts())
		{
			EditPart p = (EditPart)o;
			if (p instanceof BoxElementPart<?>)
			{
				// may not reparent pre/postcondition
				if ((p instanceof PostconditionPart || p instanceof PreconditionPart)
					&& newParent!=((BoxElement)p.getModel()).getProcedure().boxContainer)
					return null;

				if (parentPart==null)
					parentPart = (BoxContainerPart)p.getParent();
				else if (p.getParent()!=parentPart)
					return null;
				if (rect==null)
					rect = ((BoxElementPart<?>)p).getModelBounds().getCopy();
				else
					rect = rect.union(((BoxElementPart<?>)p).getModelBounds().getCopy());
				l.add(((BoxElementPart<?>)p).getModel());
			}
		}
		
		if (parentPart==null) return null;
		
		if (newParent!=parentPart.getModel())
		{
			if (newParent.getProcedure()!=parentPart.getModel().getProcedure())
				return null;
			
			// reparenting
			ArrayList<Element> elems = new ArrayList<Element>(l);
			cmd.add(new DeleteCommand(elems));
			cmd.add(new InsertCommand(elems,newParent,null));
		}

		Point refpos = request.getLocation().getTranslated(request.getMoveDelta().getNegated());
		Point p1 = parentPart.toAbsolute(rect.getLocation());
		Point diff = p1.getTranslated(refpos.getNegated());
		Point pos = request.getLocation().getTranslated(diff);
		pos = getHost().snapToGrid(pos);
		
		Point oldpos = getHost().getRootBoxContainer().fromAbsolute(p1);
		Point newpos = getHost().getRootBoxContainer().fromAbsolute(pos);
		Point globalDelta = newpos.getTranslated(oldpos.getNegated()); 
		
		pos = getHost().fromAbsolute(pos);
		rect.setLocation(pos);
		cmd.add(new BoxClearAreaCommand(getHost().getViewer().getEditPartRegistry(),l,newParent,null,rect));
		cmd.add(new BoxMoveCommand(l,pos,globalDelta));
		
		cmd.add(new BoxMoveIntoPositiveCommand(getHost().getViewer().getEditPartRegistry(),getHost().getModel()));

		BoxElement be = (BoxElement)(newParent).getParentOfType(BoxElement.class);
		if (be!=null)
			cmd.add(new BoxGrowingChildCommand(getHost().getViewer().getEditPartRegistry(),be));

		return cmd;
	}
	

	protected Command getMoveChildrenCommand(Request request)
	{
		return null;
	}
	
	
	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		return null;
	}
}
