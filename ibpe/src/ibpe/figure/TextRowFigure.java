package ibpe.figure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.text.*;
import org.eclipse.swt.SWT;

public class TextRowFigure extends Figure implements VisibleDisplayMode
{
	//private ErrorFigure error;
	protected  LabelFigure label;
	
	// add debug
	DebugMode dMode = DebugMode.INACTIVE;
	
	// add debug
	public DebugMode getDebugMode()
	{
		return dMode;
	}
	
	public void setDebugMode( DebugMode m )
	{
		dMode = m;
		repaint();
	}	
	
	public TextRowFigure( )
	{
		setForegroundColor(ColorConstants.black);
		GridLayout layout = new GridLayout(2,false);
		layout.marginWidth = layout.marginHeight = 0;
		layout.horizontalSpacing = layout.verticalSpacing = 2;	
		setLayoutManager(layout);	
		
		//add(error=new ErrorFigure(),new GridData(SWT.BEGINNING,SWT.CENTER,false,false,1,1));
		//add(label=new Label(),new GridData(SWT.BEGINNING,SWT.CENTER,false,false,1,1));
		
		add(label=new LabelFigure(),new GridData(SWT.BEGINNING,SWT.CENTER,false,false,1,1));
		
		hilight();
	}
	
	//add debug
	public void hilight()
	{
		setOpaque(false);
	}
	
	//add debug
	public void setActive( boolean yes )
	{
		if (yes)
			dMode = DebugMode.ACTIVE;
		else
			dMode = DebugMode.INACTIVE;
		hilight();
	}
	
	public void paint(Graphics graphics)
	{
		if (dMode == DebugMode.ACTIVE)
		{
			graphics.pushState();
			graphics.setClip(getBounds().getExpanded(10,10));
			graphics.setBackgroundColor(Utils.DebugColor);
			Point a = this.getBounds().getTopLeft();
			Point c = this.getBounds().getBottomLeft();
			Point b = new Point(a.x,(a.y+c.y)/2);
			Point d = new Point(a.x-6,a.y);
			Point e = new Point(c.x-6,c.y);
			PointList list = new PointList();
			list.addPoint(b);
		//	list.addPoint(c);
		//	list.addPoint(a);
			list.addPoint(d);
			list.addPoint(e);
			graphics.fillPolygon(list);
			
			graphics.popState();
		}
		
		super.paint(graphics);
	}

	
	public void setText( String txt )
	{
		label.setText(txt);
	}
	
	public LabelFigure getLabel() 
	{
		return label;	
	}

	public void setDisplayMode(DisplayMode m) {
		label.setDisplayMode(m);
		
	}

	public DisplayMode getDisplayMode() {
		return label.getDisplayMode();
	}

}
