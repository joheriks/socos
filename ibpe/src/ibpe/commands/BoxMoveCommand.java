package ibpe.commands;

import ibpe.model.*;

import java.util.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.*;


public class BoxMoveCommand extends Command 
{
	protected List<BoxElement> elements;
	protected Point position;

	protected Point box,trs;

	public BoxMoveCommand( List<BoxElement> elems, Point pos, Point delta  ) 
	{
		//assert (pos!=null || delta!=null) && !(pos==null && delta==null);
		assert elems.size()>0;
		elements = new ArrayList<BoxElement>(elems);
		position = pos==null ? null : pos.getCopy(); 
		trs = delta==null ? null : delta.getCopy();
	}
	
	public BoxMoveCommand( BoxElement elem, Point pos, Point delta  )
	{
		this(Collections.singletonList(elem),pos,delta);
	}
	

	@Override
	public boolean canExecute()
	{
		// elements must be a forest
		for (BoxElement be : elements)
			if (be.getParent()!=elements.get(0).getParent())
				return false;
		return true;
	}
	
	
	protected Rectangle boundingBox()
	{
		Rectangle r = new Rectangle(elements.get(0).getBounds().getCopy());
		for (BoxElement be : elements)
			r.union(be.getBounds());
		return r;
	}
	
	public void moveAll( Point boxDelta, Point trsDelta )
	{
		Set<BoxElement> allBoxes = new HashSet<BoxElement>(); 
		for (BoxElement be : elements)
		{
			be.setBounds(be.getBounds().getTranslated(boxDelta));
			allBoxes.add(be);
			allBoxes.addAll(be.getNestedBoxElements());
		}
		
		Set<Transition> trs = new HashSet<Transition>();
		Set<Fork> forks = new HashSet<Fork>();
		BoxElement.addIncludedTransitionArcsAndForks(allBoxes,trs,forks);
		for (Transition t : trs)
		{
			List<Point> pts = t.getWaypoints();
			for (Point p : pts) p.translate(trsDelta);
			t.setWaypoints(pts);
			t.setLabelPoint(t.getLabelPoint().getTranslated(trsDelta));
		}
		for (Fork f : forks)
			f.setPosition(f.getPosition().getTranslated(trsDelta));
		
	}
	
	@Override
	public void execute()
	{
		if (position!=null)
		{
			box = position.getTranslated(boundingBox().getTopLeft().getNegated());
			if (trs==null) trs = box;
		}
		else
			box = trs;
		moveAll(box,trs);
	}
	

	@Override
	public void undo()
	{
		/*for (BoxElement be : elements)
			be.setBounds(be.getBounds().getTranslated(moveDelta.getNegated()));*/
		moveAll(box.getNegated(),trs.getNegated());
	}

	
}
