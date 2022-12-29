package ibpe.figure;

import org.eclipse.draw2d.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class SituationFigure extends Figure 
{
	protected final int roundness = 12;
	protected Color baseColor =  ColorConstants.white; 
	
	protected SeparatorFigure sep1;
	protected GridData sep1Gd;
	protected SeparatorFigure sep2;
	protected LabelFigure label;
	protected SeparatorFigure sep3;
	protected GridData labelGd;
	protected IFigure variantCompartment;
	protected IFigure constraintCompartment;
	protected IFigure declarationCompartment;
	
	/*
	 * Grid layout of a situation.
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
	 * 
	 */
	public SituationFigure()
	{
		GridLayout layout = new GridLayout(3,false);
		layout.marginWidth = layout.marginHeight = 3;
		layout.horizontalSpacing = layout.verticalSpacing = 1;			
		setLayoutManager(layout);
		
		add(label=new LabelFigure(),new GridData(SWT.BEGINNING,SWT.BEGINNING,false,false,1,1));
		add(sep1=new SeparatorFigure(),new GridData(SWT.END,SWT.FILL,true,false,1,1));
		add(variantCompartment=new Figure());
		
		add(sep3=new SeparatorFigure(),new GridData(SWT.FILL,SWT.BEGINNING,true,false,3,1));
		add(declarationCompartment=new Figure());
		
		add(sep2=new SeparatorFigure(),new GridData(SWT.FILL,SWT.BEGINNING,true,false,3,1));
		add(constraintCompartment=new Figure());
	

		setOpaque(true);
		setBackgroundColor(baseColor);
	}

	
	public void setVariantCompartment( IFigure f )
	{
		int index = getChildren().indexOf(variantCompartment);
		remove(variantCompartment);
		add(variantCompartment=f,new GridData(SWT.END,SWT.FILL,false,false,1,1),index);
	}
	
	public void setConstraintCompartment( IFigure f )
	{
		int index = getChildren().indexOf(constraintCompartment);
		remove(constraintCompartment);
		add(constraintCompartment=f,new GridData(SWT.FILL,SWT.BEGINNING,true,false,3,1),index);
		// set a non-zero vertical margin so the compartment does not disappear when empty 
		((GridLayout)constraintCompartment.getLayoutManager()).marginHeight = 2;
	}
	
	public void setDeclarationCompartment( IFigure f )
	{
		int index = getChildren().indexOf(declarationCompartment);
		remove(declarationCompartment);
		add(declarationCompartment=f,new GridData(SWT.FILL,SWT.BEGINNING,true,false,3,1),index);
		// set a non-zero vertical margin so the compartment does not disappear when empty 
		((GridLayout)declarationCompartment.getLayoutManager()).marginHeight = 2;
	}
	
	public void setActive( boolean yes )
	{
		((SituationBorder)getBorder()).setActive(yes);
	}

	
	public LabelFigure getLabel() { return label; }

}
