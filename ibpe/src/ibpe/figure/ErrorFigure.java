package ibpe.figure;

import org.eclipse.draw2d.*;


public class ErrorFigure extends Figure 
{
	//Label popUp;
	//ImageFigure image;
	 
	public ErrorFigure()
	{
		// Initialize error figure
		setVisible(true);
		setOpaque(false);
		setPreferredSize(2,8);
		//setBackgroundColor(ColorConstants.green);
		
		/*popUp = new Label();
		popUp.setText("Open the Error View to check the details of the unproved correctness conditions");	
		setToolTip(popUp);*/
	}	
}
