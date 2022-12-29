package ibpe.action;

import ibpe.*;
import ibpe.model.*;
import ibpe.part.*;
import ibpe.commands.*;
import ibpe.io.*;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.requests.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import java.util.HashMap;


public class InsertAction extends IBPESelectionAction
{
	/**
	 * An action for inserting an element in front or after the currently selected element. 
	 */

	private CreationFactory factory;
	protected boolean insertAfter = false;
	protected Point mouseLocation = new Point();
	
	public InsertAction( IWorkbenchPart part, String id, String desc, String icon, 
						 CreationFactory f, boolean after )
	{
		super(part);

		setId(id);
		setText(desc);
		setToolTipText(desc);
		setEnabled(false);
		setLazyEnablementCalculation(false);

		ImageDescriptor img = AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE", icon);
		if (img != null)
			setImageDescriptor(img);
		
		factory = f;
		insertAfter = after;
	}

	@Override
	protected boolean calculateEnabled()
	{
		AbstractIBPEditPart<?> target = getTarget();
		if (target==null) return false;
		
		Request req = getRequest();
		if (req==null) return false;
		
		Command cmd = target.getCommand(req);
		return cmd != null && cmd.canExecute();
	}
	
	public void run()
	{
		getTarget().performRequest(getRequest());
	}
	

	
	private AbstractIBPEditPart<?> getTarget()
	{
		AbstractIBPEditPart<?> part = getSelectedOne();
		if (part==null) return null;
		Element selected = part.getModel();
		
		if (selected instanceof TextContainer || selected instanceof BoxContainer)
			return part.modelToPart((Node)selected);
		else if (selected.getParent() instanceof TextContainer || selected.getParent() instanceof BoxContainer)
			return part.modelToPart(selected.getParent());
		else
			return null;
	}
	
	protected CreateRequest getRequest()
	{
		AbstractIBPEditPart<?> target = getTarget();
		if (target==null) return null;
		
		Element selected = getSelectedOne().getModel();

		CreateRequest req = new CreateRequest(RequestConstants.REQ_CREATE);
		HashMap<String,Object> data = new HashMap<String,Object>();
		data.put("directedit",true);
		req.setExtendedData(data);
		req.setLocation(mouseLocation);
		req.setFactory(factory);
		
		if (selected.getParent() instanceof TextContainer || selected.getParent() instanceof BoxContainer)
		{
			int idx = selected.getIndex();
			if (!insertAfter) idx--;
			data.put("after",idx<0 ? null : selected.getParent().getChildren().get(idx));

		}
		else if (selected instanceof Node)
		{
			if (insertAfter && ((Node)selected).getChildren().size()>0)
				data.put("after",((Node)selected).getChildren().get(((Node)selected).getChildren().size()-1));
		}
		
		return req;
 	}
	

	public void setMouseClickPosition(Point point){
		mouseLocation.x = point.x;
		mouseLocation.y = point.y;
	}

}
