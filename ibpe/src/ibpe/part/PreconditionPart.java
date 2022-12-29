package ibpe.part;

import ibpe.model.*;
import ibpe.figure.*;

import org.eclipse.draw2d.*;
import org.eclipse.gef.EditPart;

public class PreconditionPart extends BoxElementPart<Precondition> {

	@Override
	protected Class<PreconditionFigure> getFigureClass() {
		return PreconditionFigure.class;
	}

	@Override
	public PreconditionFigure getFigure()
	{
		return (PreconditionFigure)super.getFigure();
	}

	@Override
	public LabelFigure getLabel() 
	{
		return null;
	}

	@Override
	public void addChildVisual( EditPart childPart, int index ) 
	{
		IFigure childFig = ((AbstractIBPEditPart<?>)childPart).getFigure();
		if (childPart.getModel()==getModel().constraintContainer) getFigure().setConstraintCompartment(childFig); 
		else assert false; // shouldn't happen
	}

	@Override
	public boolean isCompartmentalized() { return true;	}

	
}
