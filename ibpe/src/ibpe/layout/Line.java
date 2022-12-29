package ibpe.layout;

import org.eclipse.draw2d.geometry.*;

public class Line
{
	Point p1;
	Point p2;

	public Line(Point p1, Point p2) 
	{
		this.p1 = p1;
		this.p2 = p2;
	}

	public int y(int x) 
	{
		assert p2.x!=p1.x;
		return p1.y + (x-p1.x) * (p2.y-p1.y)/(p2.x-p1.x);
	}
	
	public Point orthogonalCrossSection(Point p3) 
	{ 
		int u1 = ((p3.x - p1.x)*(p2.x - p1.x) + (p3.y - p1.y)*(p2.y - p1.y));
		int u2 = ((p2.x - p1.x)*(p2.x - p1.x) + (p2.y - p1.y)*(p2.y - p1.y));
		double u = ((double)u1)/u2;
		
		return new Point(
				p1.x + u * (p2.x - p1.x),
				p1.y + u * (p2.y - p1.y)
			);
	}
}