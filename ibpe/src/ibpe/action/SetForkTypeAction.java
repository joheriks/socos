package ibpe.action;

import org.eclipse.ui.IEditorPart;

import ibpe.commands.*;
import ibpe.model.*;

public class SetForkTypeAction extends IBPESelectionAction 
{
	private String choiceType;
	
	public SetForkTypeAction( IEditorPart part, String id, String type )
	{
		super(part);

		setId(id);
		choiceType = type;
		setText("Enable liveness check");

		setToolTipText("Set branch to "+type);
		setLazyEnablementCalculation(true);
	}
	
	
	@Override
	protected boolean calculateEnabled() 
	{
		
		if (!(getSelectedOne()!=null && getSelectedOne().getModel() instanceof GraphNode))
			return false;
		GraphNode gn = (GraphNode)getSelectedOne().getModel();
		if (gn.getChoiceType().equals("IF"))
			setText("Disabe liveness check");
		else if (gn.getChoiceType().equals("CHOICE"))
			setText("Enable liveness check");
		return true;
	}
	
	@Override
	public void run()
	{
		GraphNode gn = (GraphNode)getSelectedOne().getModel();
		if (gn.getChoiceType().equals("IF"))
			execute(new SetChoiceTypeCommand(gn,"CHOICE"));
		else if (gn.getChoiceType().equals("CHOICE"))
			execute(new SetChoiceTypeCommand(gn,"IF"));
		calculateEnabled();
	}


}
