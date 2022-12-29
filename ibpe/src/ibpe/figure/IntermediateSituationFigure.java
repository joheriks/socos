package ibpe.figure;

import org.eclipse.draw2d.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class IntermediateSituationFigure extends SituationFigure 
{
	private IFigure boxCompartment;
	protected Color baseColor = new Color(null,255,255,220);
	
	/*
	 * Grid layout of an intermediate situation.
	 * 
	 * R
	 * O          
	 * W  COLS             0               1         2
	 * S   __________________________________________________
	 *    |                             |     |               |
	 * 0  | <label>                     |<sep>|   <variant>   |
	 *    |_____________________________|_____|_______________|
     *    |                                                   |
	 * 1  |                    <separator>                    |
	 *    |___________________________________________________|
     *    |                                                   |
	 * 2  |                   <constraints>                   |
	 *    |___________________________________________________|
     *    |                                                   |
	 * 3  |                    <separator>                    |
	 *    |___________________________________________________|
     *    |                                                   |
	 * 4  |                    <situations>                   |
	 *    |___________________________________________________|
	 * 
	 */

	public IntermediateSituationFigure()
	{
		setBorder(new SituationBorder());		
		add(new SeparatorFigure(),new GridData(SWT.FILL,SWT.BEGINNING,true,false,3,1));
		add(boxCompartment=new Label("<boxCompartment>"));
	}

	public void setSituationCompartment( IFigure f )
	{
		int index = getChildren().indexOf(boxCompartment);
		remove(boxCompartment);
		add(boxCompartment=f,new GridData(SWT.FILL,SWT.FILL,true,true,3,1),index);
	}
	
	public void updateColor(int nestinglevel)
	{
		double multiplier = Math.pow(0.92,nestinglevel);
		setBackgroundColor(new Color(null,(int)(baseColor.getRed()*multiplier),
										  (int)(baseColor.getGreen()*multiplier),
										  (int)(baseColor.getBlue()*multiplier)));
	}

}
