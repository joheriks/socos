package ibpe.layout;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class ContainerCross 
{
	public static final int TOP = 0;
	public static final int LEFT = 1;
	public static final int BOTTOM = 2;
	public static final int RIGHT = 3;
	Line l1, l2;
	
	public ContainerCross(Rectangle figRect) 
	{
		l1 = new Line(figRect.getTopLeft(), figRect.getBottomRight());
		l2 = new Line(figRect.getBottomLeft(), figRect.getTopRight());
	}

	public boolean inUpperTriangle(Point p) {
		return isAboveDecr(p) && isAboveIncr(p);
	}

	public boolean inLowerTriangle(Point p) {
		return isBelowDecr(p) && isBelowIncr(p);
	}

	public boolean inLeftTriangle(Point p) {
		return isBelowDecr(p) && isAboveIncr(p);
	}

	public boolean inRightTriangle(Point p) {
		return isAboveDecr(p) && isBelowIncr(p);
	}

	public boolean isBelowDecr(Point p) {
		return p.y > l1.y(p.x);
	}

	public boolean isAboveDecr(Point p) {
		return !isBelowDecr(p);
	}

	public boolean isBelowIncr(Point p) {
		return p.y > l2.y(p.x);
	}

	public boolean isAboveIncr(Point p) {
		return !isBelowIncr(p);
	}
	
	public int getRelativeDirection(Point compareCenter) 
	{
		int direction;
		if (inUpperTriangle(compareCenter))
			direction = TOP;
		else if (inLowerTriangle(compareCenter))
			direction = BOTTOM;
		else if (inLeftTriangle(compareCenter))
			direction = LEFT;
		else
			direction = RIGHT;
		return direction;
	}
}
