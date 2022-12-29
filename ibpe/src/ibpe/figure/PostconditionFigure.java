package ibpe.figure;


import org.eclipse.draw2d.*;
import org.eclipse.swt.SWT;

public class PostconditionFigure extends SituationFigure 
{
	public PostconditionFigure()
	{
		
	    setBorder(new CompoundBorder(new SituationBorder(),new SituationBorder()));

		remove(sep1); sep1=null;
		remove(sep3); sep3=null;
	}

	public void setActive( boolean yes )
	{
		((SituationBorder)((CompoundBorder)getBorder()).getOuterBorder()).setActive(yes);
	}
	
	@Override
	public void setConstraintCompartment( IFigure f )
	{
		super.setConstraintCompartment(f);

		// Make the constraint-compartment vertical filling
		GridLayout layout = (GridLayout)this.getLayoutManager();
		layout.setConstraint(constraintCompartment,new GridData(SWT.FILL,SWT.FILL,true,true,3,1));
	}
	
	public void setNameCompartmentVisible( boolean yes )
	{
		if (label==null)
			return;
		if (yes)
		{
			if (!getChildren().contains(label))
				add(label,new GridData(SWT.BEGINNING,SWT.BEGINNING,false,false,1,1),0);
			sep2.setVisible(true);
		}
		else
		{
			if (getChildren().contains(label))
				remove(label);
			sep2.setVisible(false);
		}
	}
	
}