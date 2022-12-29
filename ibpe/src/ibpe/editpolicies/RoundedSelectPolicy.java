package ibpe.editpolicies;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;

public class RoundedSelectPolicy extends ResizableEditPolicy
{
	@Override
	protected IFigure createDragSourceFeedbackFigure()
	{
		// Create a ghost rectangle for feedback
		RoundedRectangle r = new RoundedRectangle();
		r.setCornerDimensions(new Dimension(12,12));
		r.setFill(false);
		r.setOutlineXOR(true);
		r.setLineStyle(Graphics.LINE_DOT);
		r.setForegroundColor(ColorConstants.gray);
		r.setBounds(getInitialFeedbackBounds());
		addFeedback(r);
		return r;
	}

}
