package ibpe.commands;

import ibpe.IBPEditor;
import ibpe.model.Transition;
import ibpe.part.BoxContainerPart;
import ibpe.part.TransitionPart;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.commands.Command;

public class MoveTransitionsInSelectionCommand extends Command {
	
	protected Collection<NodeEditPart> selection;

	
	protected Collection<TransitionPart> sourceTransitions;
	protected Collection<TransitionPart> targetTransitions;
	
	protected Point moveDeltaNeg;
	protected Point moveDelta;
	
	public void setMoveDelta(Point moveDelta)
	{
		this.moveDelta = moveDelta.getScaled(1/IBPEditor.manager.getZoom());
		this.moveDeltaNeg = moveDelta.getScaled(1/IBPEditor.manager.getZoom()).negate();
	}

	public Collection<NodeEditPart> getSelection() {
		return selection;
	}

	public void setSelection(Collection<NodeEditPart> selection) {
		this.selection = selection;
	}
	
	@Override
	public boolean canExecute() {
		return selection != null && moveDelta != null;
	}
	
	@Override
	public void execute()
	{
		/*
			sourceTransitions = new HashSet<TransitionPart>();
			targetTransitions = new HashSet<TransitionPart>();
			Collection<TransitionPart> movedTransitions = new HashSet<TransitionPart>();
			Collection<TransitionPart> movedTransitionsAux = new HashSet<TransitionPart>();
			Queue<NodeEditPart> nodes = new LinkedList<NodeEditPart>();
			nodes.addAll(selection);
			
			while (!nodes.isEmpty())
			{
				NodeEditPart n = nodes.poll();
				
				for (Object e : n.getSourceConnections()){
					if (e instanceof TransitionPart)
						sourceTransitions.add((TransitionPart) e);
													
				}
				
				for (Object e : n.getTargetConnections())
					if (e instanceof TransitionPart)
						targetTransitions.add((TransitionPart) e);
				
				// Some great nesting coming up:
				for (Object e : n.getChildren())
					if (e instanceof BoxContainerPart)
						for (Object o : ((BoxContainerPart)e).getChildren())
							if (o instanceof NodeEditPart)
								nodes.add((NodeEditPart) o);
			}
			//Move the transitions with source in the situation that is being moved and target in another situation 
			movedTransitions.addAll(sourceTransitions);
			movedTransitionsAux.addAll(movedTransitions);
			movedTransitions.retainAll(targetTransitions);
			movedTransitionsAux.removeAll(movedTransitions);
						
			for (TransitionPart t : movedTransitionsAux)
			{
				Transition auxTrans = (Transition)t.getModel();
				
				Point labelPoint = auxTrans.getLabelPoint();
				
				if(labelPoint!=null && auxTrans.getNearestSegmentToLabelPosition()==0)
					auxTrans.setLabelPoint(labelPoint.translate(moveDelta.scale(0.5)));
			}
			//Move the transitions with target in the situation that is being moved and source in another situation
			movedTransitions.clear();
			movedTransitionsAux.clear();
			
			movedTransitions.addAll(sourceTransitions);
			movedTransitionsAux.addAll(targetTransitions);
			movedTransitions.retainAll(targetTransitions);
			movedTransitionsAux.removeAll(movedTransitions);
			
			
			for (TransitionPart t : movedTransitionsAux)
			{
				Transition auxTrans = (Transition)t.getModel();
										
				Point labelPoint = auxTrans.getLabelPoint();	
				
				if(labelPoint!=null && auxTrans.getNearestSegmentToLabelPosition()== auxTrans.getPointList().size())
					auxTrans.setLabelPoint(labelPoint.translate(moveDelta.scale(0.5)));
			
			}
			
			//Move the transitions with source and target in the situation that is being moved
			movedTransitions.clear();

			movedTransitions.addAll(sourceTransitions);
			movedTransitions.retainAll(targetTransitions);
						
			for (TransitionPart t : movedTransitions)
			{
				Transition auxTrans = (Transition)t.getModel();
				
				
				
				for (int i=0; i < auxTrans.getPointList().size(); i++){
					
					Point p1 = moveDelta.getCopy();	
					Point p2 = auxTrans.getPoint(i).getCopy().translate(p1);

					auxTrans.removePoint(i);
					auxTrans.addPoint(i, p2);
				}
				
				Point labelPoint = auxTrans.getLabelPoint();	
				
				if(labelPoint!=null) 
					auxTrans.setLabelPoint(labelPoint.translate(moveDelta));
				
																						
				t.propertyChange(null);
			}
		*/
	}
	
	@Override
	public void undo()
	{
		/*
		Collection<TransitionPart> movedTransitions = new HashSet<TransitionPart>();
		Collection<TransitionPart> movedTransitionsAux = new HashSet<TransitionPart>();
		
		movedTransitions.addAll(sourceTransitions);
		movedTransitionsAux.addAll(movedTransitions);
		movedTransitions.retainAll(targetTransitions);
		movedTransitionsAux.removeAll(movedTransitions);
					
		for (TransitionPart t : movedTransitionsAux)
		{
			Transition auxTrans = (Transition)t.getModel();
			
			Point labelPoint = auxTrans.getLabelPoint();
			
			if(labelPoint!=null && auxTrans.getNearestSegmentToLabelPosition()==0)
				auxTrans.setLabelPoint(labelPoint.translate(moveDeltaNeg.scale(0.5)));
		}

		movedTransitions.clear();
		movedTransitionsAux.clear();
		
		movedTransitions.addAll(sourceTransitions);
		movedTransitionsAux.addAll(targetTransitions);
		movedTransitions.retainAll(targetTransitions);
		movedTransitionsAux.removeAll(movedTransitions);
		
		
		for (TransitionPart t : movedTransitionsAux)
		{
			Transition auxTrans = (Transition)t.getModel();
									
			Point labelPoint = auxTrans.getLabelPoint();	
			
			if(labelPoint!=null && auxTrans.getNearestSegmentToLabelPosition()== auxTrans.getPointList().size())
				auxTrans.setLabelPoint(labelPoint.translate(moveDeltaNeg.scale(0.5)));
		
		}
		
		movedTransitions.clear();

		movedTransitions.addAll(sourceTransitions);
		movedTransitions.retainAll(targetTransitions);
					
		for (TransitionPart t : movedTransitions)
		{
			Transition auxTrans = (Transition)t.getModel();
			
			
			
			for (int i=0; i < auxTrans.getPointList().size(); i++){
				
				auxTrans.getPoint(i).translate(moveDeltaNeg);
			}
			
			Point labelPoint = auxTrans.getLabelPoint();	
			
			if(labelPoint!=null) 
				auxTrans.setLabelPoint(labelPoint.translate(moveDeltaNeg));
			
																					
			t.propertyChange(null);
		}
		*/
	}
}


