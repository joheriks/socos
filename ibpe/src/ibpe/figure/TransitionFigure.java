package ibpe.figure;

import ibpe.part.TransitionRouter;

import org.eclipse.core.resources.IMarker;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.SWT;

public class TransitionFigure extends PolylineConnection implements VisibleDisplayMode
{
	public static final PointList SINGLE_ARROWHEAD = new PointList();
	public static final PointList DOUBLE_ARROWHEAD = new PointList();
	public static final PointList SQUARE = new PointList();
	
	private IfChoiceFigure choiceIndicator;
	
	static {
		SINGLE_ARROWHEAD.addPoint(-1, 1);
		SINGLE_ARROWHEAD.addPoint(0, 0);
		SINGLE_ARROWHEAD.addPoint(-1, -1);

		DOUBLE_ARROWHEAD.addPoint(-1, 1);
		DOUBLE_ARROWHEAD.addPoint(0, 0);
		DOUBLE_ARROWHEAD.addPoint(-1, -1);
		DOUBLE_ARROWHEAD.addPoint(0, 0);
		DOUBLE_ARROWHEAD.addPoint(-1, 0);
		DOUBLE_ARROWHEAD.addPoint(-2, 1);
		DOUBLE_ARROWHEAD.addPoint(-1, 0);
		DOUBLE_ARROWHEAD.addPoint(-2, -1);
	}

	DisplayMode mode = DisplayMode.NORMAL;
	
	// add debug
	DebugMode dMode = DebugMode.INACTIVE;
	
	boolean end = false;
	

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
	
	
	public IfChoiceFigure getChoiceIndicator() {
		return choiceIndicator;
	}
	
	public class ChoiceIndicatorLocator implements Locator
	{ 
		public void relocate( IFigure f ) 
		{
			Point p = ((TransitionFigure)f.getParent()).getPoints().getFirstPoint().getCopy();
			p.translate(-f.getBounds().width/2,-f.getBounds().height/2);
			f.setLocation(p);
		}
	}
	
	@Override
	public void removeNotify() {
		// keep the old layout constraint
		Object c = getConnectionRouter().getConstraint(this);
		super.removeNotify();
		getConnectionRouter().setConstraint(this,c);
	}
	
	public TransitionFigure()
	{
		super();
		setForegroundColor(ColorConstants.black);
		
		add(choiceIndicator=new IfChoiceFigure(),new ChoiceIndicatorLocator());
		
		// make it a bit smaller
		 choiceIndicator.setSize(choiceIndicator.getSize().getScaled(0.75));
		
		
	//	add(callchoiceIndicator = new  MultiCallFigure(),new ChoiceIndicatorLocator());
		
		// make it a bit smaller
	//	callchoiceIndicator.setSize(callchoiceIndicator.getSize().getScaled(0.75));
		

		


		showArrowhead(true);
		showChoiceIndicator(false);
		
		setConnectionRouter(new TransitionRouter());
	}
	

	public Figure getForkFigure()
	{
		return choiceIndicator;
	}
	

	public void showArrowhead( boolean yes )
	{
		PolylineDecoration dec;  
		if (yes)
		{
			dec = new PolylineDecoration(); 
			dec.setTemplate(SINGLE_ARROWHEAD);
			dec.setScale(8,4);
		}
		else
			dec = null;
		setTargetDecoration(dec);
	}
	
	
	public void showChoiceIndicator( boolean yes )
	{
		 choiceIndicator.setVisible(yes);
	}
	
	
	public void setMessage( String msg, Object severity )
	{
		if (msg==null)
		{
			setToolTip(null);
			mode = DisplayMode.NORMAL;
		}
		else
		{
			setToolTip(new Label(msg));
			if (severity.equals(IMarker.SEVERITY_WARNING))
				mode = DisplayMode.WARNING;
			else 
				mode = DisplayMode.ERROR;

		}
	}
	

	public void setActiveEnd( boolean end )
	{
		this.end = end;
		repaint();
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
		graphics.setAntialias(SWT.ON);
		
		PolylineDecoration dec = (PolylineDecoration)getTargetDecoration();
		
		
		
		//add debug
		if (dMode == DebugMode.ACTIVE)
		{
			graphics.pushState();
			graphics.setForegroundColor(Utils.DebugColor);
			graphics.setLineWidth(6);
			graphics.drawPolyline(getPoints());
			if (end)
				graphics.drawPolyline(dec.getPoints());
			graphics.popState();
		}
		
		graphics.setForegroundColor(ColorConstants.black);
		//setLineWidth(1);
		
		super.paint(graphics);
		
		if (mode!=DisplayMode.NORMAL)
		{
			
			changeForegroundColor(graphics);
			
			Utils.drawSquiggly(graphics,getPoints());
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

	
	public void hilight()
	{
		setBorder(new LineBorder(ColorConstants.gray,1,1));
	}
	
	
	public void unhilight()
	{
		setBorder(new MarginBorder(1));
	}
	

}
