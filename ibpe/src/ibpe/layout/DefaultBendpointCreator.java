package ibpe.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class DefaultBendpointCreator 
{
	private static final int OFFSET = 50; 
	
	static public List<Point> getPoints(Rectangle bounds, Point source, Point target) 
	{
		// TODO: This method seems overly complex. Refactor?

		ContainerCross cross = new ContainerCross(bounds);
		
		// What borders are closest to the source and target points.
		int sourceBorder = cross.getRelativeDirection(source);
		int targetBorder = cross.getRelativeDirection(target);
		
		ArrayList<Point> points = new ArrayList<Point>();
		
		// If they're the same, make two bendpoints which are indented in the corresponding direction
		if (sourceBorder == targetBorder) {
			switch (sourceBorder) {
				case ContainerCross.TOP:
					points.add(new Point(source.x, bounds.y-OFFSET));
					points.add(new Point(target.x, bounds.y-OFFSET));
					break;
					
				case ContainerCross.LEFT:
					points.add(new Point(bounds.x-OFFSET, source.y));
					points.add(new Point(bounds.x-OFFSET, target.y));
					break;
					
				case ContainerCross.BOTTOM:
					points.add(new Point(source.x, bounds.bottom()+OFFSET));
					points.add(new Point(target.x, bounds.bottom()+OFFSET));
					break;
					
				case ContainerCross.RIGHT:
					points.add(new Point(bounds.right()+OFFSET, source.y));
					points.add(new Point(bounds.right()+OFFSET, target.y));
					break;
			}
		} else {
			// If they're neighboring borders, make three bendpoints
			int borderDiff = Math.abs(sourceBorder - targetBorder % 4);
			if (borderDiff == 1 || borderDiff == 3) {
				
				// Decide from the target border which direction to make the bendpoints
				switch (sourceBorder) {
					case ContainerCross.TOP:
						points.add(new Point(source.x, bounds.y-OFFSET));
						if (targetBorder == ContainerCross.LEFT) {
							points.add(new Point(bounds.x-OFFSET, bounds.y-OFFSET));
							points.add(new Point(bounds.x-OFFSET, target.y));
						} else {
							points.add(new Point(bounds.right()+OFFSET, bounds.y-OFFSET));
							points.add(new Point(bounds.right()+OFFSET, target.y));
						}
						break;
						
					case ContainerCross.BOTTOM:
						points.add(new Point(source.x, bounds.bottom()+OFFSET));
						if (targetBorder == ContainerCross.LEFT) {
							points.add(new Point(bounds.x-OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(bounds.x-OFFSET, target.y));
						} else {
							points.add(new Point(bounds.right()+OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(bounds.right()+OFFSET, target.y));
						}
						break;

					case ContainerCross.LEFT:
						points.add(new Point(bounds.x-OFFSET, source.y));
						if (targetBorder == ContainerCross.BOTTOM) {
							points.add(new Point(bounds.x-OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(target.x, bounds.bottom()+OFFSET));
						} else {
							points.add(new Point(bounds.x-OFFSET, bounds.y-OFFSET));
							points.add(new Point(target.x, bounds.y-OFFSET));
						}
						break;
						
					case ContainerCross.RIGHT:
						points.add(new Point(bounds.right()+OFFSET, source.y));
						if (targetBorder == ContainerCross.BOTTOM) {
							points.add(new Point(bounds.right()+OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(target.x, bounds.bottom()+OFFSET));
						} else {
							points.add(new Point(bounds.right()+OFFSET, bounds.y-OFFSET));
							points.add(new Point(target.x, bounds.y-OFFSET));
						}
						break;
				}
			}
			
			// Else they're opposing borders and we need four bendpoints
			
			else {
				// Decide from the position of the source point relative to the
				// box width/height which direction to make the bendpoints
				switch (sourceBorder) {
					case ContainerCross.TOP:
						points.add(new Point(source.x, bounds.y-OFFSET));
						if (source.x < bounds.width/2) {
							points.add(new Point(bounds.x-OFFSET, bounds.y-OFFSET));
							points.add(new Point(bounds.x-OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(target.x, bounds.bottom()+OFFSET));
						} else {
							points.add(new Point(bounds.right()+OFFSET, bounds.y-OFFSET));
							points.add(new Point(bounds.right()+OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(target.x, bounds.bottom()+OFFSET));
						}
						break;
						
					case ContainerCross.BOTTOM:
						points.add(new Point(source.x, bounds.bottom()+OFFSET));
						if (source.x < bounds.width/2) {
							points.add(new Point(bounds.x-OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(bounds.x-OFFSET, bounds.y-OFFSET));
							points.add(new Point(target.x, bounds.y-OFFSET));
						} else {
							points.add(new Point(bounds.right()+OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(bounds.right()+OFFSET, bounds.y-OFFSET));
							points.add(new Point(target.x, bounds.y-OFFSET));
						}
						break;
						
					case ContainerCross.LEFT:
						points.add(new Point(bounds.x-OFFSET, source.y));
						if (source.y < bounds.height/2) {
							points.add(new Point(bounds.x-OFFSET, bounds.y-OFFSET));
							points.add(new Point(bounds.right()+OFFSET, bounds.y-OFFSET));
							points.add(new Point(bounds.right()+OFFSET, target.y));
						} else {
							points.add(new Point(bounds.x-OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(bounds.right()+OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(bounds.right()+OFFSET, target.y));
						}
						break;
						
					case ContainerCross.RIGHT:
						points.add(new Point(bounds.right()+OFFSET, source.y));
						if (source.y < bounds.height/2) {
							points.add(new Point(bounds.right()+OFFSET, bounds.y-OFFSET));
							points.add(new Point(bounds.x-OFFSET, bounds.y-OFFSET));
							points.add(new Point(bounds.x-OFFSET, target.y));
						} else {
							points.add(new Point(bounds.right()+OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(bounds.x-OFFSET, bounds.bottom()+OFFSET));
							points.add(new Point(bounds.x-OFFSET, target.y));
						}
						break;
				}
			}
		}
		return points;
	}

}
