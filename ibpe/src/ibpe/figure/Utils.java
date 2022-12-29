package ibpe.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.Color;

public class Utils 
{
	public final static Color ErrorColor = ColorConstants.red;
	public final static Color WarningColor = new Color(null,255,128,0);
	public final static Color DebugColor = new Color(null,200,80,200);
	public final static Color AmbiguousColor = ColorConstants.gray;
	
	public static void drawSquiggly( Graphics graphics, PointList points )
	{
		PointList squiggly = new PointList();
		
		for (int i = 0; i<points.size()-1; i++)
		{
			Point p1 = points.getPoint(i);
			Point p2 = points.getPoint(i+1);
			int pts = ((int)p1.getDistance(p2)+1) / 3;
			if (pts==0)
				continue;
			Point p = p1.getCopy();
			Point[] displace = {
				new Point((p2.y-p1.y)/pts/2,-(p2.x-p1.x)/pts/2),
				new Point(-(p2.y-p1.y)/pts/2,(p2.x-p1.x)/pts/2),
			};
			for (int j=0; j<pts; j++)
			{
				squiggly.addPoint(p.getCopy());
				p.setLocation(p1.x+j*(p2.x-p1.x)/pts + displace[j%2].x,
							  p1.y+j*(p2.y-p1.y)/pts + displace[j%2].y);
			}
			squiggly.addPoint(p.getCopy());
		}
		graphics.drawPolyline(squiggly);
	}
	
	
	
	public static void drawSquiggly( Graphics graphics, Point p1, Point p2 )
	{
		PointList p = new PointList();
		p.addPoint(p1);
		p.addPoint(p2);
		drawSquiggly(graphics,p);
	}
}
