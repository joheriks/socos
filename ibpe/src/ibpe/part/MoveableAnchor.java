package ibpe.part;

import ibpe.IBPEditor;
import ibpe.figure.IfChoiceFigure;
import ibpe.layout.ContainerCross;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class MoveableAnchor extends ChopboxAnchor 
{
	Point location = null;
	GraphNodePart<?> graphNodeEditPart;
	
	public MoveableAnchor(GraphNodePart<?> gnp) 
	{
		super(gnp.getContentPane());
		graphNodeEditPart = gnp;
	}

	public MoveableAnchor setLocation(Point location)
	{
		if (location == null)
			this.location = null;
		else
			this.location = location.getCopy();
		
		return this;
	}
	
	public boolean isManualAnchor()
	{
		return false;
		// 9.3.2011: Disabled manual anchors for now
		/*
		Rectangle owner = getOwner().getBounds().getCopy();
		getOwner().translateToAbsolute(owner);
		if (location != null && !(getOwner() instanceof ForkFigure))
		{
			Point p = location.getTranslated(getOwner().getBounds().getLocation());
			getOwner().translateToAbsolute(p);
			return (!owner.getCropped(new Insets(10)).contains(p));
		}
		return false;*/
	}
	
	@Override
	public Point getLocation(Point reference)
	{
		Rectangle owner = getOwner().getBounds().getCopy();
		if (owner.width==0 && owner.height==0)
			return owner.getTopLeft();
		if (isManualAnchor())
		{
			Point p = location.getTranslated(owner.getLocation());
			ContainerCross cross = new ContainerCross(owner);
			boolean snapHoriz=false,snapVert=false;
			switch (cross.getRelativeDirection(p)) 
			{
				case ContainerCross.TOP:
					p.y = owner.y;
					snapHoriz = true;
					snapVert = false;
					break;
					
				case ContainerCross.LEFT:
					p.x = owner.x;
					snapHoriz = false;
					snapVert = true;
					break;
					
				case ContainerCross.BOTTOM:
					p.y = owner.bottom();
					snapHoriz = true;
					snapVert = false;
					break;
					
				case ContainerCross.RIGHT:
					p.x = owner.right();
					snapHoriz = false;
					snapVert = true;
					break;
			}
			
			p = graphNodeEditPart.getRootBoxContainer().snapToGrid(p,snapHoriz,snapVert);
			getOwner().translateToAbsolute(p);
			return p;
		}
		
		getOwner().translateToAbsolute(owner);
		Point anchor;
		
		if (owner.x <= reference.x && owner.x + owner.width >= reference.x)
		{
			// Either the top or bottom border has a pixel in common with the reference
			int diffTop = Math.abs(owner.getTop().getDifference(reference).getArea());
			int diffBot = Math.abs(owner.getBottom().getDifference(reference).getArea());
			if (diffTop < diffBot || ((diffTop == 0 || diffBot == 0) && Math.abs(owner.getTop().y - reference.y) < Math.abs(owner.getBottom().y - reference.y)))
				anchor = new Point(reference.x, owner.y);
			else
				anchor = new Point(reference.x, owner.y + owner.height);
			
		} 
		else if (owner.y <= reference.y && owner.y + owner.height >= reference.y)
		{
			// Either the left or right border has a pixel in common with the reference
			int diffRight = Math.abs(owner.getRight().getDifference(reference).getArea());
			int diffLeft = Math.abs(owner.getLeft().getDifference(reference).getArea());
			if (diffRight < diffLeft || ((diffLeft == 0 || diffRight == 0) && Math.abs(owner.getRight().x - reference.x) < Math.abs(owner.getLeft().x - reference.x)))
				anchor = new Point(owner.x + owner.width, reference.y);
			else
				anchor = new Point(owner.x, reference.y);
		} 
		else
		{
			// Connect the closest pair of corners
			int diff = Math.abs(owner.getTopLeft().getDifference(reference).getArea());
			anchor = owner.getTopLeft();
			
			int diffTopRight = Math.abs(owner.getTopRight().getDifference(reference).getArea());
			int diffBottomLeft = Math.abs(owner.getBottomLeft().getDifference(reference).getArea());
			int diffBottomRight = Math.abs(owner.getBottomRight().getDifference(reference).getArea());
			
			if (diff > diffTopRight)
			{
				anchor = owner.getTopRight();
				diff = diffTopRight;
			}
			if (diff > diffBottomLeft)
			{
				anchor = owner.getBottomLeft();
				diff = diffBottomLeft;
			}
			if (diff > diffBottomRight)
				anchor = owner.getBottomRight();
		}
		return anchor;
	}
	
}
