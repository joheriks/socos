package ibpe.part;

import ibpe.figure.*;
import ibpe.editpolicies.*;
import ibpe.layout.LabelLocator;
import ibpe.model.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.editparts.*;


public class TransitionPart extends AbstractIBPEditPart<Transition> 
       implements ConnectionEditPart, PropertyChangeListener
{
	private static final ConnectionAnchor ANCHOR1 = new XYAnchor(new Point(0,0)),
	                                      ANCHOR2 = new XYAnchor(new Point(10,10));

	int labelSegment = -1;
	Point labelSegmentStart, labelSegmentEnd;
	
	private GraphNodePart<?> source,target;

	@Override public Transition getModel() { return (Transition)super.getModel(); }
		
	public GraphNodePart<?> getSource() { return source; }
	
	public GraphNodePart<?> getTarget() { return target; }
	
	@Override public TransitionFigure getFigure() { return (TransitionFigure)super.getFigure(); }
	

	public void setSource( EditPart p )
	{
		source = (GraphNodePart<?>)p;
		if (source!=null)
			setParent(getRootBoxContainer().getRoot());
		else if (target==null)
			setParent(null);
		if (source!= null && target != null)
			refresh();

	}
	
	public void setTarget( EditPart p )
	{
		target = (GraphNodePart<?>)p;
		if (target!=null)
			setParent(getRootBoxContainer().getRoot());
		else if (source==null)
			setParent(null);
		if (source!= null && target != null)
			refresh();
	}

	
	public void updateRoutingConstraint()
	{
		List<Bendpoint> list = new ArrayList<Bendpoint>();
        for (Point p : getModel().getWaypoints())
        	list.add(new TranslatingBendpoint(getRootBoxContainer().getFigure(),p));
        getFigure().setRoutingConstraint(list);
	}
	
	public TransitionLabelPart getTransitionLabelPart() 
	{
		if (getChildren().size()==0)
			return null;
		else
			return (TransitionLabelPart)getChildren().get(0);
	}
	
	@Override
	protected TransitionFigure createFigure()
	{
		return new TransitionFigure();
	}
	
	
	public BoxContainerPart getRootBoxContainer()
	{
		if (getSource()!=null)
			return getSource().getRootBoxContainer();
		if (getTarget()!=null)
			return getTarget().getRootBoxContainer();
		return null;
	}
	

	
	public void setParent(EditPart parent) {
		boolean wasNull = getParent() == null;
		boolean becomingNull = parent == null;
		if (becomingNull && !wasNull)
			removeNotify();
		super.setParent(parent);
		if (wasNull && !becomingNull)
			addNotify();
	}

	
	
	public void addNotify() 
	{
		if (getRootBoxContainer()!=null)
		{
			assert !(getRootBoxContainer().getFigure().getChildren().contains(getFigure()));
			getRootBoxContainer().getFigure().add(getFigure());
		}
		super.addNotify();
	}

	
	@Override
	public void removeNotify() 
	{
		if (getFigure().getParent()!=null && getFigure().getParent().getChildren().contains(getFigure()))
			getFigure().getParent().remove(getFigure());
		getFigure().setSourceAnchor(null);
		getFigure().setTargetAnchor(null);
		super.removeNotify();
	}
	

	
	public ConnectionAnchor getTargetConnectionAnchor()
	{
		//ConnectionAnchor targetAnchor = super.getTargetConnectionAnchor();
		if (target==null) return ANCHOR2;
		ConnectionAnchor targetAnchor = target.getTargetConnectionAnchor(this);
		if (targetAnchor instanceof MoveableAnchor)
			((MoveableAnchor)targetAnchor).setLocation(getModel().getTargetPoint());
		return targetAnchor;
	}
	
	
	public ConnectionAnchor getSourceConnectionAnchor()
	{
		//ConnectionAnchor sourceAnchor = super.getSourceConnectionAnchor();
		if (source==null) return ANCHOR1;
		ConnectionAnchor sourceAnchor = source.getSourceConnectionAnchor(this);
		if (sourceAnchor instanceof MoveableAnchor)
			((MoveableAnchor) sourceAnchor).setLocation(getModel().getSourcePoint());
		
		return sourceAnchor;
	}
	
	
	@Override
	public void activate()
	{ 
		super.activate();
	}
	
	
	@Override
	public void deactivate()
	{ 
		super.deactivate();
		//getModel().removePropertyChangeListener(this);
	} 
	
	
	public void propertyChange( PropertyChangeEvent evt ) 
	{
		GraphNodePart<?> newSource = (GraphNodePart<?>)getViewer().getEditPartRegistry().get(getModel().getSource());
		GraphNodePart<?> newTarget = (GraphNodePart<?>)getViewer().getEditPartRegistry().get(getModel().getTarget());
		if (newSource!=getSource())
			setSource(newSource);
		if (newTarget!=getTarget())
			setTarget(newTarget);
		refresh();
	}
	
	
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new TransitionEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new DeleteTransitionEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new TransitionEndpointEditPolicy());
	}
	
	
	@Override
	protected EditPart createChild(Object model) 
	{
		// override the EditFactory to create a TransitionLabelPart instead of a TextContainerPart
		TransitionLabelPart part = new TransitionLabelPart();
		part.setModel(model);
		return part;
	}
	
	
	@Override
	public void refresh()
	{
		getFigure().setSourceAnchor(getSourceConnectionAnchor());
		getFigure().setTargetAnchor(getTargetConnectionAnchor());
		super.refresh();	
	}

	@Override
	protected void refreshVisuals()
	{
		if (getSource()==null || 
			getRootBoxContainer()==null || 
			getRootBoxContainer().getFigure()==null)
			return;

		updateRoutingConstraint();

		// If the source is a boxelement, the choice type indicator (rectangle) should be shown
        getFigure().showChoiceIndicator(getSource() instanceof BoxElementPart<?>);

        // if the fork is of IfChoiceFigure type, update the rectangle to reflect Choice/If
        Figure forkFigure = getFigure().getForkFigure();
        if (forkFigure instanceof IfChoiceFigure)
        	((IfChoiceFigure)forkFigure).setType(getSource().getModel().getChoiceType());
        
        if (getTransitionLabelPart()!=null)
        {
        	
	        IFigure sourcePointLabel = getTransitionLabelPart().getFigure();
	        
	        getFigure().setConstraint(sourcePointLabel,
	        						  new LabelLocator(getRootBoxContainer().getFigure(),
	        								  		   getModel().getLabelPoint()));
	        
	        getFigure().layout();
	        
        }
        	
        
        // PolyLine in GEF is broken with regards to layouting: it stores all points in absolute coordinates 
        // instead of relative. Hence, to get the correct bounds in rootbox coordinates we do translation.
        Rectangle bounds = getFigure().getBounds().getCopy();
        bounds.translate(getRootBoxContainer().getFigure().getBounds().getLocation().getNegated());
        getRootBoxContainer().getFigure().setConstraint(getFigure(),bounds);

        // Invalidate labelSegment
		labelSegment = -1;
    }

	
	@Override
	public List<Element> getModelChildren()
	{
		return getModel().getChildren();
	}
	

	/**
	 * This method calculates the segment that is closest to the transition label
	 * and updates the members labelSegment, labelSegmentStart, and labelSegmentEnd.
	 */
	public void calculateLabelSegment()
	{		
		Point p = getModel().getLabelPoint().getCopy();
		p = getRootBoxContainer().toAbsolute(p);
		//p.translate(getRootBoxContainer().getFigure().getBounds().getLocation());
		Point s,e;
		double u;
		PointList points = getFigure().getPoints();
		s = points.getFirstPoint();
		labelSegment = 0;
		double mindist = Double.MAX_VALUE;
		for (int i=1; i<points.size(); i++)
		{
			e = points.getPoint(i);
			u = (double)( (e.x-s.x) * (p.x-s.x ) + (e.y-s.y) * (p.y-s.y) ) / (double)( (e.x -s.x)*(e.x -s.x) + (e.y-s.y)*(e.y-s.y) );
			u = (double) Math.min(Math.max(0.0,u),1.0);
			Point m = new Point( (int)(s.x + u*(e.x-s.x)), (int) (s.y + u*(e.y-s.y)) );
			double l = (m.x-p.x)*(m.x-p.x) + (m.y-p.y)*(m.y-p.y);
			if (l<mindist)
			{
				mindist = l;
				labelSegment = i-1;
			}

			s = e;
		}
		labelSegmentStart = getRootBoxContainer().fromAbsolute(points.getPoint(labelSegment).getCopy());
		labelSegmentEnd = getRootBoxContainer().fromAbsolute(points.getPoint(labelSegment+1).getCopy());
	}
	
	
	public int getLabelSegment( Point outStart, Point outEnd )
	{
		if (labelSegment==-1)
			calculateLabelSegment();
		outStart.setLocation(labelSegmentStart);
		outEnd.setLocation(labelSegmentEnd);
		return labelSegment;
	}

	@Override
	public void clearMarkers()
	{
		super.clearMarkers();
		getFigure().setToolTip(null);
		getFigure().setDisplayMode(DisplayMode.NORMAL);
	}

	@Override
	public void showMarker( IMarker mr )
	{
		super.showMarker(mr);

		if (getMarkerCombinedSeverity()==IMarker.SEVERITY_ERROR)
			getFigure().setDisplayMode(DisplayMode.ERROR);
		else
			getFigure().setDisplayMode(DisplayMode.WARNING);
		getFigure().setToolTip(getMarkerCombinedToolTip());
		
		refreshVisuals();
	}

	
	/**
	 * Assuming the label is associated with the segment A--B, calculates a new
	 * position of the label for segment Aprime--Bprime. The perpendicular distance
	 * to the segment and the proportional intersection point of the normal are kept.
	 */
	public Point getNewLabelPosition( Point A, Point B, Point Aprime, Point Bprime )
	{
		Point labelPoint = getModel().getLabelPoint().getTranslated(getTransitionLabelPart().getFigure().getSize().getScaled(0.5));
		
		double oslen,nslen,cosine,sine = 0,newx,newy;
		double ratio;
		Point oldmid = new Point(), 
		      newmid = new Point(), 
		      os = new Point(), 
		      ns = new Point(), 
		      perp = new Point(),
		      ot = new Point();
		
		ratio = (double) ( (B.x-A.x) * (labelPoint.x-A.x ) + (B.y-A.y) * (labelPoint.y-A.y) ) / 
		        (double) ( (B.x-A.x)*(B.x-A.x) + (B.y-A.y)*(B.y-A.y) );
		ratio = Math.min(Math.max(0.0,ratio),1.0);
		
		oldmid.x = (int) (A.x+(B.x-A.x)*ratio);
		oldmid.y = (int) (A.y+(B.y-A.y)*ratio);
		
		newmid.x = (int) (Aprime.x+(Bprime.x-Aprime.x)*ratio);
		newmid.y = (int) (Aprime.y+(Bprime.y-Aprime.y)*ratio);
		
		ot.x = labelPoint.x-oldmid.x;
		ot.y = labelPoint.y-oldmid.y;
		
		os.x = B.x-A.x;
		os.y = B.y-A.y;
			
		ns.x = Bprime.x-Aprime.x;
		ns.y = Bprime.y-Aprime.y;
		
		oslen = Math.sqrt(os.x*os.x+os.y*os.y);
		nslen = Math.sqrt(ns.x*ns.x+ns.y*ns.y);
		
		cosine = (double)(os.x*ns.x + os.y*ns.y) / (double)(oslen * nslen);
		cosine = Math.max(Math.min(cosine, 1.0), -1.0);
		sine = Math.sqrt(1.0-cosine*cosine);
		
		perp.x = -os.y;
		perp.y = os.x;
		if (perp.x*ns.x + perp.y*ns.y < 0.0)
			sine = -sine;
				
		newx = newmid.x + cosine*ot.x - sine*ot.y;
		newy = newmid.y + sine*ot.x + cosine*ot.y;

		return new Point(newx,newy).getTranslated(getTransitionLabelPart().getFigure().getSize().getScaled(0.5).getNegated());
	}

}
