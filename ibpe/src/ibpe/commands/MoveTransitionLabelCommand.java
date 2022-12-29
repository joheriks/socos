package ibpe.commands;

import ibpe.model.Transition;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

public class MoveTransitionLabelCommand extends Command
{
	Point oldPosition;
	Point newPosition;
	Transition transition;
	
	public MoveTransitionLabelCommand( Transition trans, Point pos )
	{
		transition = trans;
		newPosition = pos.getCopy();
	}
	
	@Override
	public boolean canExecute() {
		return newPosition != null && transition != null;
	}

	@Override
	public void execute() {
		oldPosition = transition.getLabelPoint().getCopy();
		transition.setLabelPoint(newPosition);
	}
	
	@Override
	public void undo() {
		transition.setLabelPoint(oldPosition);
	}
}
