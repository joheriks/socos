package ibpe.commands;

import ibpe.model.*;
import ibpe.part.*;

import java.util.*;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;



/**
 * BoxElements that might have ended up in negative coordinates are "pushed"
 * back into positive coordinates.
 */
public class BoxMoveIntoPositiveCommand extends PartCommand 
{
	protected BoxContainer parent;
	protected List<Command> commands;
	
	
	
	public BoxMoveIntoPositiveCommand( Map epr, BoxContainer bc )
	{
		super(epr);
		parent = bc;
	}
	
	
	public void execute()
	{
		if (commands==null)
		{
			commands = new ArrayList<Command>();
			
			HashMap<BoxElement, Point> moveDeltas = new HashMap<BoxElement, Point>();

			//allMoved = new LinkedList<BoxElementPart<?>>();
			BoxContainerPart parentPart = (BoxContainerPart)partRegistry.get(parent);
			for(int i=0; i<2; i++)
			{
				Queue<BoxElementPart<?>> toBeMoved = null;
				switch(i)
				{
					// Two large rectangles in negative coordinates are used to "push back" 
					// BoxElements into positive coordinates.
					case 0: toBeMoved = parentPart.move(null, new Rectangle(Integer.MIN_VALUE, Integer.MIN_VALUE/2, Integer.MAX_VALUE, Integer.MAX_VALUE)); break;
					case 1: toBeMoved = parentPart.move(null, new Rectangle(Integer.MIN_VALUE/2, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)); break;
				}

				for (BoxElementPart<?> n: toBeMoved)
				{
					if(moveDeltas.containsKey(n.getModel()))
						moveDeltas.get(n.getModel()).translate(n.getMoveDelta());
					else
						moveDeltas.put(n.getModel(),n.getMoveDelta().getCopy());
					n.getMoveDelta().setLocation(0, 0);
					n.setMoveDirection(null);
				}
			}

			HashMap<Point,List<BoxElement>> groups = new HashMap<Point,List<BoxElement>>();
			for (BoxElement b : moveDeltas.keySet())
			{
				Point p = moveDeltas.get(b);
				if (!groups.containsKey(p))
						groups.put(p,new ArrayList<BoxElement>());
				groups.get(p).add(b);
			}
			
			for (Point p : groups.keySet())
				commands.add(new BoxMoveCommand(groups.get(p),null,p));


		}
		
		for (Command cmd : commands) cmd.execute();

		// This does not equal null if we're doing a "redo".
		/*
		if (moveDeltas == null)
		{
			moveDeltas = new HashMap<BoxElementPart<?>, Point>();
			allMoved = new LinkedList<BoxElementPart<?>>();
			BoxContainerPart parentPart = (BoxContainerPart)partRegistry.get(parent);
			for(int i = 0; i < 2; i++)
			{
				switch(i)
				{
					// Two large rectangles in negative coordinates are used to "push back" 
					// BoxElements into positive coordinates.
					case 0: toBeMoved = parentPart.move(null, new Rectangle(Integer.MIN_VALUE, Integer.MIN_VALUE/2, Integer.MAX_VALUE, Integer.MAX_VALUE)); break;
					case 1: toBeMoved = parentPart.move(null, new Rectangle(Integer.MIN_VALUE/2, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)); break;
				}

				for (BoxElementPart<?> n: toBeMoved)
				{
					if(moveDeltas.containsKey(n))
						moveDeltas.get(n).translate(n.getMoveDelta());
					else
					{
						moveDeltas.put(n, n.getMoveDelta().getCopy());
						allMoved.add(n);
					}
					n.getMoveDelta().setLocation(0, 0);
					n.setMoveDirection(null);
				}
			}
		}
		
		boolean firstTime = true;
		Point delta = new Point();
		
		for(BoxElementPart<?> n : allMoved) {
			((BoxElement)n.getModel()).translateBounds(moveDeltas.get(n).getCopy());
			boolean flag = false;
			if((((BoxElement) n.getModel()).getBounds().x<0 && n.getMoveDelta().getCopy().x<=0) || (((BoxElement) n.getModel()).getBounds().y<0 && n.getMoveDelta().getCopy().y<=0))
				flag = true;
			

			if(firstTime){
				delta=moveDeltas.get(n).getCopy();
				firstTime= false;
			}
				
			Collection<BoxElement> descendants = ((BoxElement)n.getModel()).getNestedBoxElements();
			descendants.add((BoxElement)n.getModel());
			
			/* TODO: Commented 5.1.2011 
			Set<Transition> transitions = new HashSet<Transition>();
			Set<Transition> transitionsAux = new HashSet<Transition>();
			
			for (BoxElement s: descendants){
				
				for (Transition t: s.getSourceTransitions())
					if(descendants.contains(t.getTarget()) && descendants.contains(t.getSource()))
						transitionsAux.add(t);
					else
						transitions.add(t);
				
				for (Transition t: s.getTargetTransitions())
					if(descendants.contains(t.getTarget()) && descendants.contains(t.getSource()))
						transitionsAux.add(t);
					else
						transitions.add(t);
					
			}
			
			for (Transition t : transitionsAux){
				for (int k=0; k<t.getPointList().size();k++){
					if (!flag){
						Point p1 = t.getPoint(k).getCopy();
						Point p2 = delta.getCopy();
						p1.translate(p2);
						t.removePoint(t.getPoint(k));
						t.addPoint(k, p1);
					}		
				}
					
			}
		
		}*/
	}
	
	public void undo()
	{
		for (int i=commands.size()-1; i>=0; i--)
			commands.get(i).undo();
		/*
		boolean firstTime = true;
		Point delta = new Point();
		
		
		if (parent == null)
			return;
		for (BoxElementPart<?> n: allMoved){
			((BoxElement)n.getModel()).translateBounds(moveDeltas.get(n).getCopy().getNegated());
			
			boolean flag = false;
			if((((BoxElement) n.getModel()).getBounds().x<0 && n.getMoveDelta().getCopy().x<=0) || (((BoxElement) n.getModel()).getBounds().y<0 && n.getMoveDelta().getCopy().y<=0))
				flag = true;
			
			
			if(firstTime){
				delta=moveDeltas.get(n).getCopy().getNegated();
				firstTime= false;
			}
				
			Collection<BoxElement> descendants = ((BoxElement)n.getModel()).getNestedBoxElements();
			descendants.add((BoxElement)n.getModel());
			
			*/
			/*
			Set<Transition> transitions = new HashSet<Transition>();
			Set<Transition> transitionsAux = new HashSet<Transition>();
			
			 TODO: Commented 5.1.2011
			for (BoxElement s: descendants){
				
				for (Transition t: ((BoxElement)s).getSourceTransitions())
					if(descendants.contains(t.getTarget()) && descendants.contains(t.getSource()))
						transitionsAux.add(t);
					else
						transitions.add(t);
				
				for (Transition t: ((BoxElement)s).getTargetTransitions())
					if(descendants.contains(t.getTarget()) && descendants.contains(t.getSource()))
						transitionsAux.add(t);
					else
						transitions.add(t);
					
			}
			
			for (Transition t : transitionsAux){
				for (int k=0; k<t.getPointList().size();k++){
					if (!flag){
						Point p1 = t.getPoint(k).getCopy();
						Point p2 = delta.getCopy().getNegated();
						p1.translate(p2);
						t.removePoint(t.getPoint(k));
						t.addPoint(k, p1);
					}		
				}
					
			}
		}
			*/
			
	}
}
