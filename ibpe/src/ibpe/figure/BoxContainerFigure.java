package ibpe.figure;

import org.eclipse.draw2d.*;

public class BoxContainerFigure extends Figure 
{
	public BoxContainerFigure()
	{
		setOpaque(false);
		setVisible(true);
		setLayoutManager(new XYLayout());
		setBorder(new MarginBorder(2));
	}

}
