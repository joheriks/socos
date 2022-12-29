package ibpe.figure;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

public class CallBorder extends AbstractBorder {
	
	final int roundness = 6;
	final int thickness = 2;
	
	public Insets getInsets(IFigure fig) {
		return new Insets(thickness, thickness, thickness, thickness);
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
		r.width = r.width - thickness + 1;
		r.height = r.height - 1;
		g.drawRoundRectangle(r, roundness, roundness);
	}
}
