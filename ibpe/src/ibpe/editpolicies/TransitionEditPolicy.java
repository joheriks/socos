package ibpe.editpolicies;

import ibpe.commands.*;
import ibpe.figure.MultiCallFigure;
import ibpe.figure.TransitionFigure;
import ibpe.model.*;
import ibpe.part.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.*;
import org.eclipse.gef.handles.*;
import org.eclipse.gef.requests.*;

/**
 * Used to add bendpoint handles on a {@link ConnectionEditPart}.
 * <P>
 * BendpointEditPolicy will automatically observe the {@link
 * org.eclipse.draw2d.Connection} figure. If the number of bends in the
 * <code>Connection</code> changes, the handles will be updated.
 */
public class TransitionEditPolicy extends SelectionHandlesEditPolicy
								  implements PropertyChangeListener
{
	private static final List NULL_CONSTRAINT = new ArrayList();
	
	private List originalConstraint;
	private RectangleFigure labelFeedback;;
	
	private boolean isDeleting = false;
	
	private static final Point ref1 = new Point();
	private static final Point ref2 = new Point();
	
	
	@Override
	public TransitionPart getHost() 
	{
		return (TransitionPart)super.getHost();
	}


	/** Takes a {@link BendpointRequest} and a {@link BoxContainerPart} as arguments.
	 * Returns a point in absolute coordinate snapped to the {@link BoxContainer} grid.
	 * @param request the {@link BendpointRequest}
	 * @return snapped point
	 */
	protected Point snapToGrid( BendpointRequest request ) 
	{
		Point point = request.getLocation().getCopy();
		getConnection().translateToRelative(point);
		return getRootBoxContainer().snapToGrid(point);
	}


	/** Takes a {@link BendpointRequest} and returns the {@link BoxContainerPart} 
	 * at {@link Procedure} level containing the connection.
	 * @param request the {@link BendpointRequest}
	 * @return root box container
	 */
	protected BoxContainerPart getRootBoxContainer(/*BendpointRequest request*/) 
	{
		return getHost().getSource().getRootBoxContainer();
	}
	

	protected Command getCreateBendpointCommand(BendpointRequest request) 
	{
		Point position = snapToGrid(request);
		position.translate(getRootBoxContainer().getFigure().getBounds().getLocation().getNegated());
		return new InsertBendpointCommand(getHost().getModel(),request.getIndex(),position);
	}
	

	protected Command getDeleteBendpointCommand(BendpointRequest request)
	{
		return new DeleteBendpointCommand(getHost().getModel(),request.getIndex());
	}
	
	
	/** Returns the new position of the label. Returns null if the label shouldn't be moved. */
	private Point getNewLabelPosition( BendpointRequest request )
	{
		Point newpoint = getRootBoxContainer().fromAbsolute(snapToGrid(request));
		Transition trans = getHost().getModel();
		Point oldpoint = trans.getPoint(request.getIndex()).getCopy();
		PointList newpoints = getHost().getFigure().getPoints();
		Point nearestStart = new Point();
		Point nearestEnd = new Point();
		int nearestSegment = getHost().getLabelSegment(nearestStart,nearestEnd);
		if (request.getIndex()==nearestSegment)
		{
			// endpoint of segment manipulated
			if (nearestSegment>=newpoints.size()) return null; // this may happen if a segment is deleted.
			Point newstart = getRootBoxContainer().fromAbsolute(newpoints.getPoint(nearestSegment));
			return getHost().getNewLabelPosition(nearestStart,oldpoint,newstart,newpoint);
		}
		else if (request.getIndex()+1==nearestSegment)
		{
			// startpoint of segment manipulated
			if (nearestSegment+1>=newpoints.size()) nearestSegment = newpoints.size()-2; // this may happen if a segment is deleted.
			Point newstart = getRootBoxContainer().fromAbsolute(newpoints.getPoint(nearestSegment+1));
			return getHost().getNewLabelPosition(nearestEnd,oldpoint,newstart,newpoint);
		}
		else
			return null;
	}
	

	protected Command getMoveBendpointCommand(BendpointRequest request) 
	{
		Point location = getRootBoxContainer().fromAbsolute(snapToGrid(request));
		Transition trans = getHost().getModel();
		Command cmd = new MoveBendpointCommand(trans,request.getIndex(),location);
		
		Point labelpos = getNewLabelPosition(request);
		if (labelpos!=null)
			cmd = cmd.chain(new MoveTransitionLabelCommand(trans,labelpos));
		return cmd;
	}

		
	@Override
	public void activate()
	{
		super.activate();
		getConnection().addPropertyChangeListener(Connection.PROPERTY_POINTS, this);
	}
	
	
	@SuppressWarnings("unchecked")
	private List createHandlesForAutomaticBendpoints() 
	{
		List list = new ArrayList();
		ConnectionEditPart connEP = getHost();
		PointList points = getConnection().getPoints();
		for (int i = 0; i < points.size() - 1; i++)
			list.add(new BendpointCreationHandle(connEP, 0, i));
		
		return list;
	}
	
	
	@SuppressWarnings("unchecked")
	private List createHandlesForUserBendpoints() 
	{
		List list = new ArrayList();
		ConnectionEditPart connEP = getHost();
		PointList points = getConnection().getPoints();
		List bendPoints = (List)getConnection().getRoutingConstraint();
		int bendPointIndex = 0;
		Point currBendPoint = null;
		
		if (bendPoints == null)
			bendPoints = NULL_CONSTRAINT;
		else if (!bendPoints.isEmpty())
			currBendPoint = ((Bendpoint)bendPoints.get(0)).getLocation();
		
		for (int i = 0; i < points.size() - 1; i++) {
			//Put a create handle on the middle of every segment
			list.add(new BendpointCreationHandle(connEP, bendPointIndex, i));
			
			//If the current user bendpoint matches a bend location, show a move handle
			if (i < points.size() - 1
					&& bendPointIndex < bendPoints.size()
					&& currBendPoint.equals(points.getPoint(i + 1))) {
				list.add(new BendpointMoveHandle(connEP, bendPointIndex, i + 1));
				
				//Go to the next user bendpoint
				bendPointIndex++;
				if (bendPointIndex < bendPoints.size())
					currBendPoint = ((Bendpoint)bendPoints.get(bendPointIndex)).getLocation();
			}
		}
	
		return list;
	}
	
	
	@SuppressWarnings("unchecked")
	protected List createSelectionHandles() 
	{
		List list = new ArrayList();
		if (isAutomaticallyBending())
			list = createHandlesForAutomaticBendpoints();
		else
			list = createHandlesForUserBendpoints();
	 	return list;
	}
	
	
	@Override
	public void deactivate() 
	{
		getConnection().removePropertyChangeListener(Connection.PROPERTY_POINTS, this);
		super.deactivate();
	}
	
	
	@Override
	public void eraseSourceFeedback( Request request ) 
	{
		if (REQ_MOVE_BENDPOINT.equals(request.getType())
		  || REQ_CREATE_BENDPOINT.equals(request.getType()))
		{
			restoreOriginalConstraint();
			originalConstraint = null;

			if (labelFeedback!=null)
			{
				getLayer(LayerConstants.FEEDBACK_LAYER).remove(labelFeedback);
				labelFeedback = null;
			}
			getHost().getTransitionLabelPart().getFigure().setVisible(true);

		}
	}
	
	
	public Command getCommand(Request request) 
	{
	
		if (REQ_MOVE_BENDPOINT.equals(request.getType())) 
		{
			if (isDeleting)
				return getDeleteBendpointCommand((BendpointRequest)request);
			return getMoveBendpointCommand((BendpointRequest)request);
		}
		if (REQ_CREATE_BENDPOINT.equals(request.getType()))
			return getCreateBendpointCommand((BendpointRequest)request);
		return null;
	}
	
	
	protected TransitionFigure getConnection() 
	{
		return getHost().getFigure();
	}
	
	
	@SuppressWarnings("unchecked")
	private boolean isAutomaticallyBending() 
	{
		List constraint = (List)getConnection().getRoutingConstraint();
		PointList points = getConnection().getPoints();
		return ((points.size() > 2) && (constraint == null || constraint.isEmpty()));
	}
	
	
	private boolean lineContainsPoint(Point p1, Point p2, Point p) 
	{
		int tolerance = 7;
		Rectangle rect = Rectangle.SINGLETON;
		rect.setSize(0, 0);
		rect.setLocation(p1.x, p1.y);
		rect.union(p2.x, p2.y);
		rect.expand(tolerance, tolerance);
		if (!rect.contains(p.x, p.y))
			return false;
	
		int v1x, v1y, v2x, v2y;
		int numerator, denominator;
		double result = 0.0;
	
		if (p1.x != p2.x && p1.y != p2.y) {
			
			v1x = p2.x - p1.x;
			v1y = p2.y - p1.y;
			v2x = p.x - p1.x;
			v2y = p.y - p1.y;
			
			numerator = v2x * v1y - v1x * v2y;
			denominator = v1x * v1x + v1y * v1y;
	
			result = ((numerator << 10) / denominator * numerator) >> 10;
		}
		
		// if it is the same point, and it passes the bounding box test,
		// the result is always true.
		return result <= tolerance * tolerance;
	}
	
	
	public void propertyChange(PropertyChangeEvent evt) 
	{
		//$TODO optimize so that handles aren't added constantly.
		if (getHost().getSelected() != EditPart.SELECTED_NONE)
			addSelectionHandles();	
	}
	
	
	protected void restoreOriginalConstraint() 
	{
		if (originalConstraint != null) 
		{
			if (originalConstraint == NULL_CONSTRAINT)
				getConnection().setRoutingConstraint(null);
			else
				getConnection().setRoutingConstraint(originalConstraint);
		}
	}
	
	
	/**
	 * Since the original figure is used for feedback, this method saves the 
	 * original constraint, so that is can be restored when the feedback is
	 * erased.
	 */
	@SuppressWarnings("unchecked")
	protected void saveOriginalConstraint()
	{
		originalConstraint = (List)getConnection().getRoutingConstraint();
		if (originalConstraint == null)
			originalConstraint = NULL_CONSTRAINT;
		getConnection().setRoutingConstraint(new ArrayList(originalConstraint));
	}
	
	
	@SuppressWarnings("unchecked")
	private void setReferencePoints(BendpointRequest request) 
	{
		PointList points = getConnection().getPoints();
		int bpIndex = -1;
		List bendPoints = (List)getConnection().getRoutingConstraint();
		Point bp = ((Bendpoint)bendPoints.get(request.getIndex())).getLocation();
		
		int smallestDistance = -1;
		
		for (int i = 0; i < points.size(); i++) {
			if (smallestDistance == -1
					|| points.getPoint(i).getDistance2(bp) < smallestDistance) {
				bpIndex = i;
				smallestDistance = points.getPoint(i).getDistance2(bp);
				if (smallestDistance == 0)
					break;
			}
		}
		
		bpIndex = Math.min(Math.max(1,bpIndex),points.size());
		
		points.getPoint(ref1, bpIndex - 1);
		getConnection().translateToAbsolute(ref1);
		points.getPoint(ref2, bpIndex + 1);
		getConnection().translateToAbsolute(ref2);
	}
	
	
	/**
	 * Shows feedback when a bendpoint is being created.  The original figure is used for
	 * feedback and the original constraint is saved, so that it can be restored when feedback
	 * is erased.
	 * @param request the BendpointRequest
	 */
	@SuppressWarnings("unchecked")
	protected void showCreateBendpointFeedback(BendpointRequest request) 
	{
		List constraint;
		Bendpoint bp = new AbsoluteBendpoint(snapToGrid(request));
		if (originalConstraint == null) {
			saveOriginalConstraint();
			constraint = (List)getConnection().getRoutingConstraint();
			constraint.add(request.getIndex(), bp);
		} else {
			constraint = (List)getConnection().getRoutingConstraint();
		}
		constraint.set(request.getIndex(), bp);
		getConnection().setRoutingConstraint(constraint);
	}
	
	
	/**
	 * Shows feedback when a bendpoint is being deleted.  This method is only called once when
	 * the bendpoint is first deleted, not every mouse move.  The original figure is used for
	 * feedback and the original  constraint is saved, so that it can be restored when
	 * feedback is erased.
	 * @param request the BendpointRequest
	 */
	@SuppressWarnings("unchecked")
	protected void showDeleteBendpointFeedback(BendpointRequest request) 
	{
		if (originalConstraint == null) {
			saveOriginalConstraint();
			List constraint = (List)getConnection().getRoutingConstraint();
			constraint.remove(request.getIndex());
			getConnection().setRoutingConstraint(constraint);
		}
	}
	
	
	/**
	 * Shows feedback when a bendpoint is being moved.  Also checks to see if the bendpoint 
	 * should be deleted and then calls {@link #showDeleteBendpointFeedback(BendpointRequest)}
	 * if needed.  The original figure is used for feedback and the original constraint is 
	 * saved, so that it can be restored when feedback is erased.
	 * @param request the BendpointRequest
	 */
	@SuppressWarnings("unchecked")
	protected void showMoveBendpointFeedback(BendpointRequest request) 
	{
		if (!isDeleting)
			setReferencePoints(request);
		Point point = snapToGrid(request); //request.getLocation().getCopy();
		
		if (lineContainsPoint(ref1, ref2, request.getLocation()) && ((List)getConnection().getRoutingConstraint()).size() > 1)
		{
			if (!isDeleting)
			{
				isDeleting = true;
				eraseSourceFeedback(request);

				showDeleteBendpointFeedback(request);
			}
			return;
		}
		if (isDeleting) 
		{
			isDeleting = false;
			eraseSourceFeedback(request);
		}
		if (originalConstraint == null)
			saveOriginalConstraint();

		showLabelFeedback(request);
		
		List constraint = (List)getConnection().getRoutingConstraint();
		Bendpoint bp = new AbsoluteBendpoint(point);
		constraint.set(request.getIndex(), bp);
		getConnection().setRoutingConstraint(constraint);
	}
	
	
	/**
	 * Shows feedback when appropriate. Calls a different method depending on the request
	 * type.
	 */
	public void showSourceFeedback(Request request)
	{
		if (REQ_MOVE_BENDPOINT.equals(request.getType()))
			showMoveBendpointFeedback((BendpointRequest)request);
		else if (REQ_CREATE_BENDPOINT.equals(request.getType()))
			showCreateBendpointFeedback((BendpointRequest)request);
	}

	
	private TransitionLabelPart getTransitionLabelPart()
	{
		for (Object part : getHost().getChildren())
			if (part instanceof TransitionLabelPart)
				return (TransitionLabelPart)part;
		return null;
	}
	
	
	@Override
	public void showSelection()
	{
		super.showSelection();
		if (getTransitionLabelPart()!=null)
			getTransitionLabelPart().getFigure().hilight();
	}

	
	@Override
	public void hideSelection()
	{
		super.hideSelection();
		if (getTransitionLabelPart()!=null)
			getTransitionLabelPart().getFigure().unhilight();
	}
	

	public void showLabelFeedback( BendpointRequest request )
	{
		Point labelpos = getNewLabelPosition(request);
		if (labelpos==null) return;
		labelpos = getHost().getRootBoxContainer().toAbsolute(labelpos);
		getHost().getTransitionLabelPart().getFigure().setVisible(false);
		
		if (labelFeedback==null) 
		{
			labelFeedback = new RectangleFigure();
			labelFeedback.setLineStyle(Graphics.LINE_DOT);
			labelFeedback.setOutlineXOR(true);
			labelFeedback.setFill(false);
			labelFeedback.setForegroundColor(ColorConstants.gray);
			getLayer(LayerConstants.FEEDBACK_LAYER).add(labelFeedback);
		}
		
		IFigure fig = getHost().getTransitionLabelPart().getFigure();
		labelFeedback.setSize(fig.getSize());
		//labelFeedback.setLocation(labelpos.getTranslated(fig.getSize().getNegated().getScaled(0.5)));
		labelFeedback.setLocation(labelpos);
	}
	
}


