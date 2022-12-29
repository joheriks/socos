package ibpe.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.SWT;

public class IfFigure extends IfChoiceFigure {

	public IfFigure()
	{
		setPreferredSize(FORK_SIZE, FORK_SIZE);
		setBackgroundColor(ColorConstants.black);
	}
	
	@Override
	public void paintFigure(Graphics graphics)
	{
		graphics.setAntialias(SWT.ON);
		graphics.translate(getLocation());
		graphics.fillRectangle(0, 0, FORK_SIZE, FORK_SIZE);
	}
}
