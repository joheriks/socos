package ibpe.editpolicies;

import ibpe.commands.*;
import ibpe.model.*;
import ibpe.part.*;

import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.*;
import org.eclipse.gef.requests.*;


public class EditTextPolicy extends DirectEditPolicy 
{
	private String oldValue;
	
	@Override
	public AbstractDirectEditPart<?> getHost()
	{
		return (AbstractDirectEditPart<?>)super.getHost();
	}
	
	@Override
	protected Command getDirectEditCommand(DirectEditRequest request)
	{
		String celleditorvalue = (String)request.getCellEditor().getValue();
		if (celleditorvalue==null)
			return null;
		
		// if celleditorvalue=="" on a textrow, request a delete from the host instead
		if (celleditorvalue.length()==0 && getHost().getModel() instanceof TextRow)
			return getHost().getCommand(new Request(RequestConstants.REQ_DELETE));
		
	//	if (celleditorvalue.length()==0 && getHost().getModel() instanceof MultiCall)
	//		return getHost().getCommand(new Request(RequestConstants.REQ_DELETE));
		
		UpdateTextCommand cmd1 = new UpdateTextCommand((Element) getHost().getModel(),celleditorvalue);
		BoxElement be = (BoxElement)getHost().getModel().getParentOfType(BoxElement.class);
		if (be==null)
			return cmd1;
		else
			return cmd1.chain(new BoxGrowingChildCommand(getHost().getViewer().getEditPartRegistry(),be));
	}

	@Override
	protected void showCurrentEditValue(DirectEditRequest request)
	{
		if (getHost().getLabel()!=null)
			getHost().getLabel().setVisible(false);
		//getHost().refresh();
	}

	@Override
	protected void storeOldEditValue(DirectEditRequest request)
	{		
		oldValue = (String) request.getCellEditor().getValue();
	}

	@Override
	protected void revertOldEditValue(DirectEditRequest request)
	{
		super.revertOldEditValue(request);
		request.getCellEditor().setValue(oldValue);
		
		/*
		if (getHost().getLabel()!=null)
		{
			getHost().getLabel().setVisible(true);
			getHost().getLabel().revalidate();
		}*/
		
		getHost().setSelected(EditPart.SELECTED_PRIMARY);

	}

}
