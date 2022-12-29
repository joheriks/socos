package ibpe.tool;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.SnapToGrid;

public class IBPSnapToGrid extends SnapToGrid {

	public IBPSnapToGrid(GraphicalEditPart container)
	{
		super(container);
		origin = container.getFigure().getBounds().getLocation();
	}

}
