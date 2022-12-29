package ibpe.figure;

import java.awt.Font;

import javax.swing.text.StyleConstants.FontConstants;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

public class MulticallBorder extends AbstractBorder {

	final int roundness = 6;
	final int thickness = 2;
	
	// add debug
	DebugMode dMode = DebugMode.INACTIVE;
	
	public MulticallBorder(boolean active){
		if(active)
			dMode = DebugMode.ACTIVE;
		else
			dMode = DebugMode.INACTIVE;
	}
	
	public Insets getInsets(IFigure fig) {
		return new Insets(thickness, thickness, thickness, thickness);
	}
	
	@Override
	public boolean isOpaque() {
		return true;
	}
	
	// add debug
	public DebugMode getDebugMode()
	{
		return dMode;
	}
	
	public void paint(IFigure fig, Graphics g, Insets ins)
	{
		g.setAntialias(SWT.ON);
		Rectangle r = fig.getBounds().getCropped(ins);
		g.clipRect(r);
		
		r.width = r.width - thickness + 1;
		r.height = r.height - 1;
		g.setLineWidth(thickness);
		if(dMode == DebugMode.ACTIVE)
			g.setForegroundColor(Utils.DebugColor);
		else
			g.setForegroundColor(ColorConstants.darkGray);
		g.drawRoundRectangle(r, roundness, roundness);
	}
}
