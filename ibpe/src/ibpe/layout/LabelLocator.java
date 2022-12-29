package ibpe.layout;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;

public class LabelLocator implements Locator
{ 
	private IFigure relative;
	private Point position;
	
	
	public LabelLocator( IFigure rel, Point pos )
	{
		super();
		relative = rel;
		position = pos;
	}
	

	public void relocate( IFigure f ) 
	{
		Rectangle r = new Rectangle(position.getTranslated(relative.getBounds().getLocation()),f.getPreferredSize());
		//r.translate(-r.width/2,-r.height/2);
		f.setBounds(r);
	}

}
