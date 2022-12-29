package ibpe.figure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;

public class SeparatorFigure extends Figure {

	public SeparatorFigure()
	{
		setVisible(true);
		setBorder(new LineBorder(ColorConstants.lightGray));
	}
	
	public Dimension getPreferredSize(int wHint, int hHint)
	{
		return new Dimension(1,1);
		/*
		// is it safe to query the parent's size here? 
		if (getParent()!=null)
			return new Dimension(getParent().getSize().width-8,1);
		else
			return super.getPreferredSize(wHint,hHint);
			*/
	}

}
