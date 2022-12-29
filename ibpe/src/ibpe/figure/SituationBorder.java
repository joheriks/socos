package ibpe.figure;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

public class SituationBorder extends AbstractBorder {
	
	private int thickness = 1;
	private final int roundness = 12;
	private final int inset = 2;
	
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
	}	
	
	//add debug
	public void setActive( boolean yes )
	{
		if (yes)
			dMode = DebugMode.ACTIVE;
		else
			dMode = DebugMode.INACTIVE;
	}
	
	SituationBorder() {	
		
	}
	
	SituationBorder( int thick ) {
		thickness = thick;
	}
	
	public Insets getInsets(IFigure fig) {
		return new Insets(inset, inset, inset, inset);
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	public void paint(IFigure fig, Graphics g, Insets ins)
	{			
		g.setAntialias(SWT.ON);
		Rectangle r = fig.getBounds().getCropped(ins);
		g.clipRect(r);
		r.width = r.width - inset + 1;
		r.height = r.height - 1;
		g.setLineWidth(thickness);
		g.setForegroundColor(ColorConstants.gray);
		g.drawRoundRectangle(r, roundness, roundness);
		
		//add debug
		if (dMode == DebugMode.ACTIVE)
		{
			g.pushState();
			g.setAntialias(SWT.ON);
			Rectangle r1 = fig.getBounds().getCropped(ins);
			g.clipRect(r1);
			r1.width = r1.width - inset + 1;
			r1.height = r1.height - 1;
			g.setLineWidth(7);
			g.setForegroundColor(Utils.DebugColor);		
			g.drawRoundRectangle(r1, roundness, roundness);
			g.popState();
		}
		g.setLineWidth(thickness);
		g.setForegroundColor(ColorConstants.gray);
		g.drawRoundRectangle(r, roundness, roundness);
	}
	

}
