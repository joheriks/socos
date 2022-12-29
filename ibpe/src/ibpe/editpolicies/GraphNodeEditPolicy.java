package ibpe.editpolicies;

import ibpe.commands.*;
import ibpe.figure.*;
import ibpe.layout.DefaultBendpointCreator;
import ibpe.model.*;
import ibpe.part.*;

import java.util.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.*;
import org.eclipse.gef.requests.*;


public class GraphNodeEditPolicy extends GraphicalNodeEditPolicy
{
	protected RectangleFigure feedbackFigure;

	
	@Override
	public AbstractIBPEditPart<?> getHost()
	{
		return (AbstractIBPEditPart<?>)super.getHost();
	}
	
	
	@Override
	public Command getCommand( Request r )
	{
		if (REQ_DELETE.equals(r.getType()) && getHost() instanceof GraphNodePart<?>)
		{
			List<GraphNode> gns = new ArrayList<GraphNode>();
			GraphNodePart<?> gp = (GraphNodePart<?>)getHost();
			gns.add(gp.getModel());
			List<Element> elems = new ArrayList<Element>();
			
			while (!gns.isEmpty())
			{
				GraphNode gn = gns.remove(0);
				if (!elems.contains(gn))
					elems.add(gn);

				for (Transition t : gn.getIncomingTransitions())
					if (!elems.contains(t))
						elems.add(t);

				for (Transition t : gn.getOutgoingTransitions())
					if (t.getTarget() instanceof Fork)
						gns.add(t.getTarget());
					else if (!elems.contains(t))
						elems.add(t);
			}
			return new DeleteCommand(elems);
		}
		return super.getCommand(r);
	}

	
	/** Returns the BoxContainer of the procedure that contains this part */
	public BoxContainerPart getRootBoxContainer()
	{
		if (getHost() instanceof GraphNodePart<?>)
			return ((GraphNodePart<?>)getHost()).getRootBoxContainer();
		else if (getHost() instanceof BoxContainerPart)
			return ((BoxContainerPart)getHost()).getRootBoxContainer();
		else
			return null;
	}

	
	@Override
	protected Connection createDummyConnection(Request req) 
	{
		TransitionFigure f = new TransitionFigure();
		f.showArrowhead(false);
		return f;
	}

	
	protected List<Point> getDefaultBendpoints( CreateConnectionRequest request )
	{
		BoxContainerPart root = getRootBoxContainer();
		
		Object bendpointModifier = request.getExtendedData().get("bendpointModifier");
		boolean bpf = bendpointModifier instanceof Boolean && ((Boolean)bendpointModifier) == true; 
		
        List<Point> list = new ArrayList<Point>();
        CreateTransitionCommand command = (CreateTransitionCommand)request.getStartCommand();
		if (request.getSourceEditPart() == request.getTargetEditPart()
			&& request.getSourceEditPart() instanceof BoxElementPart<?>
			&& !request.getExtendedData().containsKey("bendpoints")
			&& !bpf) 
		{
			Rectangle bounds = ((BoxElementPart<?>)request.getSourceEditPart()).getFigure().getBounds().getCopy();
			bounds.translate(root.getFigure().getBounds().getTopLeft().getNegated());

			Point source = command.getSourcePoint().getTranslated(bounds.getLocation());
			source = root.fromAbsolute(root.snapToGrid(root.toAbsolute(source)));

			Point target = request.getLocation().getCopy();
			target = target.getTranslated(((GraphicalEditPart)request.getTargetEditPart()).getFigure().getBounds().getTopLeft());
			target = target.getTranslated(root.getFigure().getBounds().getTopLeft().getNegated());
			target = root.fromAbsolute(root.snapToGrid(root.toAbsolute(target)));
			
			for (Point p : DefaultBendpointCreator.getPoints(bounds, source, target))
	        	list.add(p.getCopy());
	        
		} 
        else if (request.getExtendedData().containsKey("bendpoints"))
			for (Point p : (List<Point>)request.getExtendedData().get("bendpoints"))
				list.add(p.getCopy());
		return list;
	}
	
	protected boolean isValidTarget( GraphNode src, GraphNode trg )
	{
		// Cannot relink across procedures
		if (src.getProcedure() != trg.getProcedure())
			return false;
		
		// A Fork cannot be target
		if (trg instanceof Fork) return false;
		
		// May never link to precondition
		if (trg instanceof Precondition)
			return false;
		
		return true;
	}
	
	
	/*
	protected Point getDefaultLabelPoint( CreateConnectionRequest request )
	{
		
        //command.setLabelPoint(getRootBoxContainer().fromAbsolute(figure.getPoints().getMidpoint()));
		return new Point(0,0);
	}
	*/
	
	
	@Override
	protected Command getConnectionCompleteCommand( CreateConnectionRequest request )
	{
		if (request.getTargetEditPart()==null) return null;

		CreateTransitionCommand cmd = (CreateTransitionCommand) request.getStartCommand();
		cmd.setBendpoints(getDefaultBendpoints(request));

		if (request.getTargetEditPart().getModel() instanceof GraphNode) 
		{
			/*if (cmd.getSource().getProcedure() != ((GraphNode)request.getTargetEditPart().getModel()).getProcedure())
				return null;
			if (request.getTargetEditPart().getModel() instanceof Fork)
				return null;*/
			if (!isValidTarget((GraphNode)request.getSourceEditPart().getModel(),
							   (GraphNode)request.getTargetEditPart().getModel()))
				return null;
			cmd.setTarget((GraphNode)request.getTargetEditPart().getModel());
			cmd.setTargetPoint(request.getLocation());
			return cmd;
		}
		else if (request.getTargetEditPart().getModel() instanceof BoxContainer)
		{
			// A boxcontainer is being targeted, create a new Fork
			BoxContainerPart cp = (BoxContainerPart)request.getTargetEditPart();
			Procedure proc = cp.getModel().getProcedure();
			
			if (cmd.getSource().getProcedure()!=proc)
				return null;
			
			Point location = request.getLocation().getCopy();
			cp.getFigure().translateToRelative(location);
			location = cp.getRootBoxContainer().snapToGrid(location);
			location = cp.getRootBoxContainer().fromAbsolute(location);
			
			CompoundCommand cmd2 = new CompoundCommand();
			Fork f = (Fork)request.getExtendedData().get("fork");
			f.setPosition(location);
			cmd2.add(new InsertCommand(f,proc,proc.getChildren().get(proc.getChildren().size()-1)));
			cmd.setTarget(f);
			cmd.setTargetPoint(null);
			cmd2.add(cmd);
			return cmd2;
		}
		return null;
	}
	

	@Override
	protected Command getConnectionCreateCommand( CreateConnectionRequest request )
	{
		EditPart target = request.getTargetEditPart();
		
		if (!(target instanceof GraphNodePart))
			return null;
		
		if (target instanceof PostconditionPart)
			return null;
		
		request.setTargetEditPart(target);
		CreateTransitionCommand cmd = new CreateTransitionCommand();
		cmd.setSource((GraphNode)target.getModel());
		Point location = request.getLocation().getCopy();
		if (target instanceof NodeEditPart)
		{
			IFigure figure = ((NodeEditPart)target).getFigure();
			figure.translateToRelative(location);
			location.translate(figure.getBounds().getLocation().getNegated());
		}
		cmd.setSourcePoint(location);
		request.setStartCommand(cmd);
		return cmd;
	}
	
	
	protected Command getReconnectCommand( ReconnectRequest request  )
	{
		TransitionPart part = ((TransitionPart)request.getConnectionEditPart());
		Transition trans = part.getModel();
		
		CompoundCommand cmd = new CompoundCommand();
		Point location; 
		Point newpoint;
		
		// disallow orphaning a Fork with outgoing transitions
		if (!request.isMovingStartAnchor() 
			&& trans.getTarget() instanceof Fork 
			&& ((Fork)trans.getTarget()).getOutgoingTransitions().size()>0)
			return null;
		
		GraphNode gn;
		if (request.getTarget() instanceof GraphNodePart<?>)
		{
			gn = (GraphNode)request.getTarget().getModel();

			// disallow reconnection across procedures
			if (trans.getTarget().getProcedure() != ((GraphNode)request.getTarget().getModel()).getProcedure())
				return null;

			// NEEDED TO ENSURE THAT FORK GRAPH REMAINS A TREE ROOTED AT A SITUATION
			
			// preconditions and forks cannot be set to the target of a transition
			if (!request.isMovingStartAnchor() && !isValidTarget(trans.getSource(),gn))
				return null;
			
			// if the source is being moved, we must not make a cycle of forks
			if (request.isMovingStartAnchor())
				if (gn instanceof Fork && trans.getTarget() instanceof Fork
					&& ((Fork)trans.getTarget()).isReachable((Fork)gn))
					return null;

			IFigure figure = ((AbstractIBPEditPart<?>)request.getTarget()).getFigure();
			location = request.getLocation().getCopy();
			figure.translateToRelative(location);
			location.translate(figure.getBounds().getLocation().getNegated());
			
			MoveableAnchor anchor = (MoveableAnchor)(request.isMovingStartAnchor() ? 
									part.getSourceConnectionAnchor() :
									part.getTargetConnectionAnchor());
			anchor.setLocation(location);
			anchor.setOwner(figure);
			newpoint = anchor.isManualAnchor() ? location : null;
		}
		
		else if (request.getTarget() instanceof BoxContainerPart && !request.isMovingStartAnchor())
		{
			BoxContainerPart cp = (BoxContainerPart)request.getTarget();
			Procedure proc = cp.getModel().getProcedure();
			
			// disallow reconnection across procedures
			if (trans.getTarget().getProcedure() != proc)
				return null;

			if (trans.getTarget() instanceof Fork)
				gn = trans.getTarget();
			else
			{
				gn = new IFChoice(new Point());
				cmd.add(new InsertCommand(gn,proc,proc.getChildren().get(proc.getChildren().size()-1)));
			}
			location = request.getLocation().getCopy();
			cp.getFigure().translateToRelative(location);
			location = cp.getRootBoxContainer().snapToGrid(location);
			location = cp.getRootBoxContainer().fromAbsolute(location);
			cmd.add(new MoveForkCommand((Fork)gn,location));
			newpoint = null;
		}
		else
			return null;

		cmd.add(new ReconnectTransitionCommand(part.getModel(),request.isMovingStartAnchor(),gn,newpoint));

		Point nearestStart = new Point();
		Point nearestEnd = new Point();
		int nearest = part.getLabelSegment(nearestStart,nearestEnd);
		
		if (nearest==0 && request.isMovingStartAnchor())
		{
			Point anchorPos = part.getFigure().getPoints().getFirstPoint();
			anchorPos.translate(getRootBoxContainer().getFigure().getBounds().getTopLeft().getNegated());
			cmd.add(new MoveTransitionLabelCommand(trans,
												   part.getNewLabelPosition(nearestEnd,nearestStart,nearestEnd,anchorPos)));
		}
													        
		else if (nearest==part.getModel().getWaypoints().size() && !request.isMovingStartAnchor())
		{
			Point anchorPos = part.getFigure().getPoints().getLastPoint();
			anchorPos.translate(getRootBoxContainer().getFigure().getBounds().getTopLeft().getNegated());
			cmd.add(new MoveTransitionLabelCommand(trans,
												   part.getNewLabelPosition(nearestStart,nearestEnd,nearestStart,anchorPos)));
		}
		
		
		// add a command to delete orphaned Fork
		if (!request.isMovingStartAnchor() 
			&& trans.getTarget() instanceof Fork && gn!=trans.getTarget())
			cmd.add(new DeleteCommand(trans.getTarget()));

		return cmd;
	}
	
	
	@Override
	protected Command getReconnectSourceCommand( ReconnectRequest request )
	{
		return getReconnectCommand(request);
	}


	@Override
	protected Command getReconnectTargetCommand( ReconnectRequest request )
	{
		return getReconnectCommand(request);
	}
	

	@Override
	protected void showCreationFeedback( CreateConnectionRequest request ) 
	{
		Point location = request.getLocation().getCopy();
		getRootBoxContainer().getFigure().translateToRelative(location);
		location = getRootBoxContainer().snapToGrid(location);
		getRootBoxContainer().getFigure().translateToAbsolute(location);
		Object bendpointModifier = request.getExtendedData().get("bendpointModifier");
		boolean bpf = bendpointModifier instanceof Boolean && ((Boolean)bendpointModifier) == true; 
		FeedbackHelper helper = getFeedbackHelper(request);
		helper.update(bpf ? null : getTargetConnectionAnchor(request), location.getCopy());

		TransitionFigure figure = (TransitionFigure)connectionFeedback;
		CreateTransitionCommand command = (CreateTransitionCommand)request.getStartCommand();
		
		if (figure!=null)
		{
			List<Bendpoint> list = new ArrayList<Bendpoint>();
			for (Point p : getDefaultBendpoints(request))
				list.add(new TranslatingBendpoint(getRootBoxContainer().getFigure(),p.getCopy()));
			figure.setConnectionRouter(new TransitionRouter());
			figure.setRoutingConstraint(list);
			figure.layout();
			figure.showArrowhead(request.getTargetEditPart() instanceof GraphNodePart<?>);
			
			figure.setVisible(getConnectionCompleteCommand(request)!=null);
			
			command.setLabelPoint(getRootBoxContainer().fromAbsolute(figure.getPoints().getMidpoint()));
		}
	}
	

	@Override
	public void showTargetFeedback( Request request ) 
	{ 
		super.showTargetFeedback(request);

		if (REQ_CONNECTION_END.equals(request.getType()) 
			|| REQ_RECONNECT_TARGET.equals(request.getType()))
		{
			Point location;
			EditPart target;
			IFigure layer = getLayer(LayerConstants.FEEDBACK_LAYER);		
			if (feedbackFigure==null)
				feedbackFigure = new RectangleFigure();
			if (!layer.getChildren().contains(feedbackFigure))
				layer.add(feedbackFigure);
			Command cmd;
			
			if (request instanceof ReconnectRequest)
			{
				location = ((ReconnectRequest)request).getLocation().getCopy();
				target = ((ReconnectRequest)request).getTarget();
				cmd = getReconnectCommand((ReconnectRequest)request);
				if (cmd==null)
				{
					feedbackFigure.setVisible(false);
					// restore the figure to original constraints if the command is disabled
					TransitionPart tp =(TransitionPart)((ReconnectRequest)request).getConnectionEditPart();
					tp.refresh();
				}
			}
			else if (request instanceof CreateConnectionRequest)
			{
				location = ((CreateConnectionRequest)request).getLocation().getCopy();
				target = ((CreateConnectionRequest)request).getTargetEditPart();
				cmd = getConnectionCompleteCommand((CreateConnectionRequest)request);
			}
			else
				return;
			
			boolean bpf = (request.getExtendedData().get("bendpointModifier") instanceof Boolean 
					       && ((Boolean)request.getExtendedData().get("bendpointModifier"))==true);
			if (cmd==null || (!bpf && target instanceof GraphNodePart<?>))
				feedbackFigure.setVisible(false);
			else
			{
				int s = bpf ? 6 : IfChoiceFigure.FORK_SIZE;
				feedbackFigure.setSize(s,s);
				getRootBoxContainer().getFigure().translateToRelative(location);
				location = getRootBoxContainer().snapToGrid(location);
				location.translate(-s/2,-s/2);
				feedbackFigure.setLocation(location);
				feedbackFigure.setVisible(true);
			}
		}
		
	}

	
	@Override
	public void eraseTargetFeedback(Request request)
	{
		super.eraseTargetFeedback(request);
		if (feedbackFigure != null && getLayer(LayerConstants.FEEDBACK_LAYER).getChildren().contains(feedbackFigure))
			getLayer(LayerConstants.FEEDBACK_LAYER).remove(feedbackFigure);
	}

}
