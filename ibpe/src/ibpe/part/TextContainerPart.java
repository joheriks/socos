package ibpe.part;

import ibpe.editpolicies.*;
import ibpe.figure.*;
import ibpe.io.Fragment;
import ibpe.io.ParserInterface;
import ibpe.model.*;

import java.beans.PropertyChangeEvent;
import java.util.*;

import org.eclipse.draw2d.*;
import org.eclipse.gef.*;
import org.eclipse.gef.requests.*;
import org.eclipse.swt.*;

public class TextContainerPart extends AbstractIBPEditPart<TextContainer> 
{
	
	@Override
	protected TextContainerFigure createFigure()
	{
		return new TextContainerFigure(2,0,false);
	}
	
	
	@Override
	public TextContainerFigure getFigure() 
	{
		return (TextContainerFigure)super.getFigure();
	}
		
	
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new TextContainerEditPolicy());
	}
	
	
	@Override
	public void addChildVisual( EditPart childPart, int index ) 
	{
		IFigure child = ((GraphicalEditPart) childPart).getFigure();
		getContentPane().add(child,new GridData(SWT.LEFT,SWT.BEGINNING,true,false,1,1),index);
	}
	
	
	@Override
	public List<Element> getModelChildren() 
	{ 
		return getModel().getChildren(); 
	}
	
	
	public void propertyChange(PropertyChangeEvent evt) 
	{
		refresh();
	}

	
	public void performRequest(Request request)
	{
		if (request.getType().equals(RequestConstants.REQ_OPEN))
		{
			// if we double-click on a container, create new declaration
			// this is a bit of a hack
			Node model = getModel();
		
			int children = model.getChildren().size();
			
			CreateRequest creq = new CreateRequest(RequestConstants.REQ_CREATE);
			HashMap<String,Object> data = new HashMap<String,Object>();
			data.put("after",children==0 ? null : model.getChildren().get(model.getChildren().size()-1));
			data.put("directedit",true);
			creq.setExtendedData(data);
			creq.setFactory(new CreationFactory()
			{
				public Object getObjectType()
				{
					return Fragment.class;
				}
	
				public Object getNewObject() 
				{
					return (new ParserInterface()).parseAsFragment("new declaration;\n");
				}	});
	
			performRequest(creq);
		}
		else
		{
			List<EditPart> oldChildren = new ArrayList<EditPart>(getChildren());
			super.performRequest(request);
			
			// if a new item was created, select it and possibly do a directedit
			if (request instanceof CreateRequest)
			{
				for (Object o : getChildren())
				{
					EditPart p = (EditPart)o;
				
					if (!oldChildren.contains(p))
					{
						
						getViewer().select(p);
						
						if (request.getExtendedData().containsKey("directedit"))
						{
							Request ereq = new DirectEditRequest(RequestConstants.REQ_DIRECT_EDIT);
							HashMap<String,Object> m = new HashMap<String,Object>();
							m.put("undolast", true);
							ereq.setExtendedData(m);
							p.performRequest(ereq);
						}
						return;
					}
				}
			}
		}
	}
}