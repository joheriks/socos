package ibpe.part;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.ViewportAutoexposeHelper;

public class AutoScrollHelper extends ViewportAutoexposeHelper {
	private long lastStepTime = 0;
	private Insets threshold;
	private static final Insets DEFAULT_EXPOSE_THRESHOLD = new Insets(50);

	public AutoScrollHelper(GraphicalEditPart owner)
	{
	    super(owner);
	    threshold = DEFAULT_EXPOSE_THRESHOLD;
	}
	
	public AutoScrollHelper(GraphicalEditPart owner, Insets threshold)
	{
		super(owner);
		this.threshold = threshold;
	}
	
	@Override
	public boolean detect(Point where)
	{
		lastStepTime = 0;
	    Viewport port = findViewport(owner);
	    Rectangle rect = Rectangle.SINGLETON;
	    port.getClientArea(rect);
	    port.translateToParent(rect);
	    port.translateToAbsolute(rect);
	    
	    return rect.contains(where) && !rect.crop(threshold).contains(where);
	}
	
	@Override
	public boolean step(Point where)
	{
	    Viewport port = findViewport(owner);
	    
	    Rectangle rect = Rectangle.SINGLETON;
	    port.getClientArea(rect);
	    port.translateToParent(rect);
	    port.translateToAbsolute(rect);
		if (!rect.contains(where) || rect.crop(threshold).contains(where)){
	    	return false;
		}

	    // set scroll offset (speed factor)
	    int scrollOffset = 0;

	    // calculate time based scroll offset
	    if (lastStepTime == 0)
	    	lastStepTime = System.currentTimeMillis();
	    
	    long difference = System.currentTimeMillis() - lastStepTime;
	    
	    if (difference > 0)
	    {
			scrollOffset = ((int) difference / 3);
	        lastStepTime = System.currentTimeMillis();
	    }

		if (scrollOffset == 0)
			scrollOffset = 5;

	    rect.crop(threshold);

	    int region = rect.getPosition(where);
	    Point loc = port.getViewLocation();
	    
	    if ((region & PositionConstants.SOUTH) != 0)
	        loc.y += scrollOffset;
	    else if ((region & PositionConstants.NORTH) != 0)
	        loc.y -= scrollOffset;

	    if ((region & PositionConstants.EAST) != 0)
	        loc.x += scrollOffset;
	    else if ((region & PositionConstants.WEST) != 0)
	        loc.x -= scrollOffset;
	    
	    port.setViewLocation(loc);
	    return true;
	}

}
