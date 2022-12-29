package ibpe.figure;

import ibpe.IBPEditor;
import ibpe.model.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.graphics.Color;


public class IfChoiceFigure extends RectangleFigure implements VisibleDisplayMode
{
	public static final int FORK_SIZE = IBPEditor.FONT_SIZE+2;
	
	public static final Color FORK_COLOR = ColorConstants.blue;
	
	private DisplayMode mode = DisplayMode.NORMAL;
	
	// add debug
	DebugMode dMode = DebugMode.INACTIVE;
	
	public IfChoiceFigure()
	{
		setPreferredSize(FORK_SIZE, FORK_SIZE);
		
		setBorder(new LineBorder(2));
		
		setVisible(true);
		setType(Fork.CHOICE);
	}
	

	public DisplayMode getDisplayMode()
	{
		return mode;
	}
	
	
	public void setDisplayMode( DisplayMode m )
	{
		mode = m;
		repaint();
	}
	
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
	
	
	public void setType( String t )
	{
		setOpaque(true);
		if (t.equals(Fork.CHOICE))
		{
			setForegroundColor(ColorConstants.blue);
			setBackgroundColor(ColorConstants.white);
		}
		else if (t.equals(Fork.IF))
		{
			setForegroundColor(ColorConstants.blue);
			setBackgroundColor(ColorConstants.blue);
		}
		else
		{
			setForegroundColor(ColorConstants.red);
			setBackgroundColor(ColorConstants.red);
		}
		setSize(FORK_SIZE,FORK_SIZE);
		repaint();
	}

	@Override
	public void paint(Graphics graphics)
	{
		graphics.pushState();
		
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.setForegroundColor(getForegroundColor());
		graphics.setLineWidth(2);
		
		graphics.fillRectangle(getBounds());
		graphics.drawRectangle(getBounds().getShrinked(new Insets(1,1,1,1)));
		Rectangle rect = getClientArea().expand(3,3);			
		
		if (dMode==DebugMode.ACTIVE)
		{
			graphics.setLineWidth(4);
			graphics.setClip(rect);
			graphics.setForegroundColor(Utils.DebugColor);
			graphics.drawRectangle(getBounds().getExpanded(new Insets(1,1,1,1)));
		}
		
		if (mode!=DisplayMode.NORMAL)
		{
			changeForegroundColor(graphics);
			
			graphics.drawLine(rect.x,rect.y,rect.getRight().x,rect.getBottom().y);
			graphics.drawLine(rect.getRight().x,rect.y,rect.x,rect.getBottom().y);
		}
	
		/*
		while (d<rect.width || d<rect.height)
		{
			graphics.drawLine(rect.x, rect.y+d,
							  rect.x+d,rect.y);
			graphics.drawLine(rect.x+d,rect.getBottom().y,
						 	  rect.getRight().x,rect.y+d);
			d += 4;
		}*/
		
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
	
	public void setActive( boolean yes )
	{
		dMode = yes ? DebugMode.ACTIVE : DebugMode.INACTIVE; 
		repaint();
	}
}
