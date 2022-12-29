package ibpe.figure;

import org.eclipse.draw2d.*;
import org.eclipse.swt.SWT;

public class PreconditionFigure extends SituationFigure {
	
	public PreconditionFigure()
	{
		setBorder(new SituationBorder(5));		
		remove(sep1); sep1=null;
		remove(sep2); sep2=null;
		remove(sep3); sep3=null;
		remove(label); label=null;
	}

	@Override
	public void setConstraintCompartment( IFigure f )
	{
		super.setConstraintCompartment(f);
		
		// Make the constraint-compartment vertical filling
		GridLayout layout = (GridLayout)this.getLayoutManager();
		layout.setConstraint(constraintCompartment,new GridData(SWT.FILL,SWT.FILL,true,true,3,1));
	}

}