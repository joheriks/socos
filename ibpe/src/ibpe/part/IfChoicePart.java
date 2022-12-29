package ibpe.part;

import ibpe.figure.IfChoiceFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class IfChoicePart extends ForkPart<IfChoiceFigure> {

	@Override
	protected IfChoiceFigure createFigure() {
		return new IfChoiceFigure();
	}
	
	@Override
	protected void refreshVisuals()
	{
		super.refreshVisuals();
		getFigure().setType(getModel().getChoiceType());
	}
	
}
