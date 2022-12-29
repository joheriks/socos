package ibpe.part;

import java.util.*;

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

public class TransitionRouter extends BendpointConnectionRouter {
	
	private static final PrecisionPoint S_POINT = new PrecisionPoint();
	private static final PrecisionPoint T_POINT = new PrecisionPoint();

	public void route(Connection conn)
	{
		PointList points = conn.getPoints();
		points.removeAllPoints();

		@SuppressWarnings("unchecked")
		List<Bendpoint> bendpoints = (List<Bendpoint>)getConstraint(conn);

		if (bendpoints == null) bendpoints = new ArrayList<Bendpoint>();

		Point ref1, ref2;
		boolean s_set = false, t_set = false, t_rel = true, s_rel = true;
		
		if (bendpoints.isEmpty())
		{
			ConnectionAnchor sourceAnchor = conn.getSourceAnchor();
			ConnectionAnchor targetAnchor = conn.getTargetAnchor();
			
			if (sourceAnchor == null || sourceAnchor.getOwner() == null || targetAnchor == null || targetAnchor.getOwner() == null)
			{
				S_POINT.setLocation(conn.getSourceAnchor().getLocation(conn.getTargetAnchor().getReferencePoint()));
				T_POINT.setLocation(conn.getTargetAnchor().getLocation(conn.getSourceAnchor().getReferencePoint()));
			} 
			else
			{
				Rectangle source = sourceAnchor.getOwner().getBounds();
				Rectangle target = targetAnchor.getOwner().getBounds();
	
				if ((source.x <= target.x && source.x + source.width >= target.x) || (target.x <= source.x && target.x + target.width >= source.x) || // Vertical coordinates in common
					(source.y <= target.y && source.y + source.height>= target.y) || (target.y <= source.y && target.y + target.height >= source.y))  // Horizontal coordinates in common
				{
					if (conn.getSourceAnchor() instanceof MoveableAnchor && ((MoveableAnchor)conn.getSourceAnchor()).isManualAnchor())
					{
						S_POINT.setLocation(conn.getSourceAnchor().getLocation(null));
						s_set = true;
					}
					if (conn.getTargetAnchor() instanceof MoveableAnchor && ((MoveableAnchor)conn.getTargetAnchor()).isManualAnchor())
					{
						T_POINT.setLocation(conn.getTargetAnchor().getLocation(null));
						t_set = true;
					}
					if (s_set || t_set)
					{
						if (!s_set)
							S_POINT.setLocation(conn.getSourceAnchor().getLocation(T_POINT));
						if (!t_set)
							T_POINT.setLocation(conn.getTargetAnchor().getLocation(S_POINT));
					} 
					else
					{
						if (target.getSize().getArea() > source.getSize().getArea())
						{
							T_POINT.setLocation(conn.getTargetAnchor().getLocation(conn.getSourceAnchor().getReferencePoint()));
							S_POINT.setLocation(conn.getSourceAnchor().getLocation(T_POINT));
							
						} 
						else
						{
							S_POINT.setLocation(conn.getSourceAnchor().getLocation(conn.getTargetAnchor().getReferencePoint()));
							T_POINT.setLocation(conn.getTargetAnchor().getLocation(S_POINT));
						}
					}
				} 
				else
				{
					// Connect the closest pair of corners or midpoints
					List<Point> sourcePoints = new ArrayList<Point>();
					List<Point> targetPoints = new ArrayList<Point>();
					
					sourcePoints.add(source.getTopLeft());
					sourcePoints.add(source.getTop());
					sourcePoints.add(source.getTopRight());
					sourcePoints.add(source.getLeft());
					sourcePoints.add(source.getRight());
					sourcePoints.add(source.getBottomLeft());
					sourcePoints.add(source.getBottom());
					sourcePoints.add(source.getBottomRight());
					
					targetPoints.add(target.getTopLeft());
					targetPoints.add(target.getTop());
					targetPoints.add(target.getTopRight());
					targetPoints.add(target.getLeft());
					targetPoints.add(target.getRight());
					targetPoints.add(target.getBottomLeft());
					targetPoints.add(target.getBottom());
					targetPoints.add(target.getBottomRight());
					
					int diff = Integer.MAX_VALUE;
					for (Point sourcePoint : sourcePoints)
					{
						for (Point targetPoint : targetPoints)
						{
							int tempDiff = Math.abs(sourcePoint.getDifference(targetPoint).getArea());
							if (tempDiff < diff)
							{
								diff = tempDiff;
								S_POINT.setLocation(sourcePoint);
								T_POINT.setLocation(targetPoint);
							}	
						}
					}
					
					if (sourceAnchor instanceof MoveableAnchor && ((MoveableAnchor)sourceAnchor).isManualAnchor())
						S_POINT.setLocation(sourceAnchor.getLocation(S_POINT));
					else 
						s_rel = false;
					
					if (targetAnchor instanceof MoveableAnchor && ((MoveableAnchor)targetAnchor).isManualAnchor())
						T_POINT.setLocation(targetAnchor.getLocation(T_POINT));
					else
						t_rel = false;
				}
			}
		} 
		else
		{
			ref1 = new Point(((Bendpoint)bendpoints.get(0)).getLocation());
			ref2 = new Point(((Bendpoint)bendpoints.get(bendpoints.size() - 1)).getLocation());
			
			conn.translateToAbsolute(ref1);
			conn.translateToAbsolute(ref2);
			
			S_POINT.setLocation(conn.getSourceAnchor().getLocation(ref1));
			T_POINT.setLocation(conn.getTargetAnchor().getLocation(ref2));
		}

		if (s_rel)
			conn.translateToRelative(S_POINT);
		if (t_rel)
			conn.translateToRelative(T_POINT);
		points.addPoint(S_POINT);
		
		for (Bendpoint bp : bendpoints)
			points.addPoint(bp.getLocation().getCopy());
		
		points.addPoint(T_POINT);
		conn.setPoints(points);
		
	}
}
