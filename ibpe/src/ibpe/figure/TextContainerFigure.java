package ibpe.figure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.*;


public class TextContainerFigure extends Figure implements MouseMotionListener, VisibleDisplayMode
{
	private boolean hilightContent;

	private DisplayMode mode = DisplayMode.NORMAL;
	
	// add debug
	DebugMode dMode = DebugMode.INACTIVE;

	public TextContainerFigure( int marginW, int marginH, boolean hiContent )
	{

		GridLayout layout = new GridLayout(1,true);
		layout.marginWidth = marginW;
		layout.marginHeight = marginH;
		layout.horizontalSpacing = layout.verticalSpacing = 1;			
		setLayoutManager(layout);

		//this.setMinimumSize(new Dimension(10,10));
		
		setOpaque(false);
		
		hilightContent = hiContent;
		if (hiContent) addMouseMotionListener(this);
		setBorder(new MarginBorder(1));
		
	}
	
	public void hilight()
	{
		setBorder(new LineBorder(ColorConstants.gray,1,1));
	}
	
	
	public void unhilight()
	{
		setBorder(new MarginBorder(1));
	}
	
	//add debug
	public void setActive( boolean yes )
	{
		if (yes)
			dMode = DebugMode.ACTIVE;
		else
			dMode = DebugMode.INACTIVE;
		repaint();
	}
	
	
	public void mouseEntered(MouseEvent me) 
	{
		if (hilightContent) hilight();
	}

	public void mouseExited( MouseEvent ev ) 
	{
		unhilight();
	}

	public void mouseDragged(MouseEvent me)	{}

	public void mouseMoved(MouseEvent me) {}

	public void mouseHover(MouseEvent me) {}

	public void setDisplayMode(DisplayMode m) 
	{
		mode = m;
		repaint();
	}
	
	public DisplayMode getDisplayMode() {
		return mode;
	}
	
	@Override
	public void paint(Graphics graphics)
	{
		graphics.pushState();

		super.paint(graphics);
		if (mode!=DisplayMode.NORMAL)
		{
			
			changeForegroundColor(graphics);
			
			Utils.drawSquiggly(graphics,
							   getClientArea().getTopLeft().getTranslated(2,0),
							   getClientArea().getBottomLeft().getTranslated(2,0));
		}
		if (dMode == DebugMode.ACTIVE)
		{
			graphics.pushState();
			graphics.setBackgroundColor(Utils.DebugColor);
			Point a = this.getBounds().getTopLeft();
			Point c = this.getBounds().getBottomLeft();
			Point b = new Point(a.x+6,(a.y+c.y)/2);
		//	Point d = new Point(a.x-6,a.y);
		//	Point e = new Point(c.x-6,c.y);
			PointList list = new PointList();
			list.addPoint(b);
			list.addPoint(c);
			list.addPoint(a);
		//	list.addPoint(d);
		//	list.addPoint(e);
			graphics.fillPolygon(list);
			
			graphics.popState();
		}
		graphics.popState();
	}
	
	public void changeForegroundColor(Graphics graphics)
	{
		switch(mode){
			case ERROR:
				graphics.setForegroundColor(Utils.ErrorColor);
				break;
			case AMBIGUOUS:
				graphics.setForegroundColor(Utils.AmbiguousColor);
				break;
			case WARNING:
				graphics.setForegroundColor(Utils.WarningColor);
				break;	
		}
	}


}
