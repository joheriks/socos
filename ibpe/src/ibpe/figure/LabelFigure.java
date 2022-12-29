package ibpe.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;

public class LabelFigure extends Label implements VisibleDisplayMode
{
		
	private int[] SQUIGGLY = {-1,0,1,0};

	DisplayMode mode = DisplayMode.NORMAL;
	
	public LabelFigure() {
		setLabelAlignment(PositionConstants.LEFT);
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
	
	
	@Override
	public void paint(Graphics graphics)
	{
		graphics.pushState();
		super.paint(graphics);
		if (mode!=DisplayMode.NORMAL)
		{
	
			changeForegroundColor(graphics);
			
			Utils.drawSquiggly(graphics,
						  	   getClientArea().getTopLeft().getTranslated(0,getTextSize().height-2),
						 	   getClientArea().getTopLeft().getTranslated(getTextSize().width,getTextSize().height-2));
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
