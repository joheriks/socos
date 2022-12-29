package ibpe.figure;

import ibpe.IBPEditor;
import ibpe.model.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;


public class MultiCallFigure extends LabelFigure implements VisibleDisplayMode
{
	
	private DisplayMode mode = DisplayMode.NORMAL;
	
	protected  LabelFigure label;
	
	// add debug
	DebugMode dMode = DebugMode.INACTIVE;
	
	public MultiCallFigure()
	{
		
		GridLayout layout = new GridLayout(1,false);
		layout.marginWidth = 5;
		layout.marginHeight = 3;
		layout.horizontalSpacing = layout.verticalSpacing = 2;	
		setLayoutManager(layout);	
		
		add(label=new LabelFigure(),new GridData(SWT.BEGINNING,SWT.CENTER,false,false,1,1));
		setBorder(new MulticallBorder(false));
		
		setForegroundColor(ColorConstants.darkBlue);
		setBackgroundColor(ColorConstants.white);
		
		label.setLabelAlignment(PositionConstants.CENTER);
		
		setOpaque(true);
		setVisible(true);
	}
	

	public DisplayMode getDisplayMode()
	{
		return mode;
	}
	
	// add debug
	public DebugMode getDebugMode()
	{
		return dMode;
	}
	
	
	public void setDisplayMode( DisplayMode m )
	{
		mode = m;
		repaint(); 
	}
	
	
	public LabelFigure getLabel() 
	{
		return label;	
	}
	
	public void setActive( boolean yes )
	{
		dMode = yes ? DebugMode.ACTIVE : DebugMode.INACTIVE; 
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
							   getClientArea().getTopLeft().getTranslated(2,0),
							   getClientArea().getBottomLeft().getTranslated(2,0));
		}
		if (dMode == DebugMode.ACTIVE)
			setBorder(new MulticallBorder(true));
		else
			setBorder(new MulticallBorder(false));

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
