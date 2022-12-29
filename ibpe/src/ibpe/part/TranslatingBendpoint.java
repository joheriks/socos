package ibpe.part;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;

public class TranslatingBendpoint implements Bendpoint 
{
	private IFigure relative;
	private Point position;
	
	
	public TranslatingBendpoint(IFigure rel, Point pos) 
	{
		relative = rel;
		position = pos.getCopy();

	}

	
	public Point getLocation() 
	{
		return position.getTranslated(relative.getBounds().getLocation());
	}

}
