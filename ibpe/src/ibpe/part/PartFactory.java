package ibpe.part;

import ibpe.model.*;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

public class PartFactory implements EditPartFactory 
{

	public EditPart createEditPart(EditPart context, Object model) 
	{
		AbstractGraphicalEditPart part = null;

		if (model instanceof Context) part = new ContextPart();
		else if (model instanceof Procedure) part = new ProcedurePart();
		else if (model instanceof Situation) part = new SituationPart();
		else if (model instanceof IFChoice) part = new IfChoicePart();
		else if (model instanceof MultiCall) part = new MultiCallPart();
		else if (model instanceof TextRow) part = new TextRowPart();
		else if (model instanceof BoxContainer) part = new BoxContainerPart();
		else if (model instanceof TextContainer) part = new TextContainerPart();
		else if (model instanceof Transition) part = new TransitionPart();
		else if (model instanceof Precondition) part = new PreconditionPart();
		else if (model instanceof Postcondition) part = new PostconditionPart();
		else if (model instanceof Proof) part = new ProofPart();

		part.setModel(model);
		return part;
	}
}