package ibpe.part;

import ibpe.figure.SeparatorFigure;
import ibpe.figure.TextContainerFigure;
import ibpe.figure.VisibleDisplayMode;
import ibpe.io.JSONParser;
import ibpe.model.Element;
import ibpe.tool.IBPDragTracker;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.eclipse.gef.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


/*
 * An abstract class containing common behavior for all IBPE EditParts.
 */
public abstract class AbstractIBPEditPart<E extends Element> 
       extends AbstractGraphicalEditPart
	   implements PropertyChangeListener
{ 
	protected List<IMarker> markers = new LinkedList<IMarker>();
	
	// A transparent figure that paints the selection borders on the feedback layer while dragging.
	RectangleFigure selectedIndicator = new RectangleFigure() 
	{ 
		{
			this.setBorder(new LineBorder(ColorConstants.white,1,Graphics.LINE_DOT));
			this.setAlpha(128);
		}
	};
	
	@SuppressWarnings("unchecked")
	@Override
	public E getModel() 
	{
		return (E)super.getModel();
	}
	
	// Type safe mapping from model to part, relies on the viewer of this part
	// for the editpart registry.
	@SuppressWarnings("unchecked")
	public <EE extends Element> AbstractIBPEditPart<EE> modelToPart( EE e )
	{
		return (AbstractIBPEditPart<EE>)getViewer().getEditPartRegistry().get(e);
	}
	
	@Override
	public void activate()
	{
		super.activate(); 
		getModel().addPropertyChangeListener(this);
	}
	
	@Override
	public void deactivate()
	{ 
		super.deactivate(); 
		getModel().removePropertyChangeListener(this); 
	} 
	
		
	@Override	
	public void showTargetFeedback(Request request)
	{
		if(request.getType().equals("DRAG_SELECT"))
		{
			if (!getLayer(LayerConstants.FEEDBACK_LAYER).getChildren().contains(selectedIndicator))
				getLayer(LayerConstants.FEEDBACK_LAYER).add(selectedIndicator);
				
			selectedIndicator.setBounds(getFigure().getBounds().getCopy().expand(1,1));
		}			
		super.showTargetFeedback(request);
	}
	
	@Override	
	public void eraseTargetFeedback(Request request)
	{
		if(request.getType().equals(REQ_SELECTION))
		{
			if (getLayer(LayerConstants.FEEDBACK_LAYER).getChildren().contains(selectedIndicator))
				getLayer(LayerConstants.FEEDBACK_LAYER).remove(selectedIndicator);
		}			
		super.eraseTargetFeedback(request);
	}
	
	@Override
	public void performRequest(Request request)
	{
		Command command = getCommand(request);
		if (command != null && command.canExecute())
		{
			getViewer().getEditDomain().getCommandStack().execute(command);
		}
	}
	

	@Override
	public DragTracker getDragTracker(Request request)
	{
		return new IBPDragTracker(this);
	}
	

	/**
	 * Returns whether children are compartments. Compartments cannot be selected and are bypassed
	 * in keyboard navigation.
	 */
	public boolean isCompartmentalized()
	{
		return false;
	}
	

	public boolean isCompartment()
	{
		return (getParent() instanceof AbstractIBPEditPart<?>) 
		       && ((AbstractIBPEditPart<?>)getParent()).isCompartmentalized(); 
	}
	
	
	public void clearMarkers()
	{
		markers.clear();
	}
	
	public void showMarker( IMarker mr )
	{
		markers.add(mr);
	}
	
	
	protected int getMarkerCombinedSeverity()
	{
		int severity = IMarker.SEVERITY_INFO;
		for (IMarker m : markers)
			try {
				Integer s = (Integer) m.getAttribute(IMarker.SEVERITY);
				if (s > severity)
						severity = s;
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return severity;
	}
	

	protected Figure getDebugCombinedToolTip(HashMap msg)
	{
		Figure tooltip = new Figure();
		tooltip.setBorder(new MarginBorder(4));
		ToolbarLayout tb = new ToolbarLayout();
		tb.setSpacing(3);
		tooltip.setLayoutManager(tb);
		boolean first = true;
		String lastmsg = "";
		Figure f = new Figure();
		GridLayout gl = new GridLayout(2,false);
		gl.horizontalSpacing = 10;
		f.add(new Label((String)msg.get("message")));
		f.setLayoutManager(gl);
		tooltip.add(f);
		return tooltip;
	}
	
	protected Figure getMarkerCombinedToolTip()
	{
		Figure tooltip = new Figure();
		tooltip.setBorder(new MarginBorder(4));
		ToolbarLayout tb = new ToolbarLayout();
		tb.setSpacing(3);
		tooltip.setLayoutManager(tb);
		boolean first = true;
		String lastmsg = "";
		List<String> lastass = new ArrayList<String>();
		for (IMarker m : markers)
			try {
				int sev = (Integer)m.getAttribute(IMarker.SEVERITY);
				String msg = (String)m.getAttribute("short_message");
				Image img = PlatformUI.getWorkbench().getSharedImages().getImage(sev==IMarker.SEVERITY_ERROR ? 
						                                                         ISharedImages.IMG_DEC_FIELD_ERROR :
						                                                         ISharedImages.IMG_DEC_FIELD_WARNING);
				if (!first)
					tooltip.add(new SeparatorFigure());
				if (!msg.equals(lastmsg))
				{
					Label lbl = new Label(msg,img);
					lbl.setLabelAlignment(PositionConstants.LEFT);
					tooltip.add(lbl);
				}
				Object o = null;
				try {
					o = JSONParser.parseString((String)m.getAttribute("orig_message"));
				}
				catch (RecognitionException e)
				{
					// shouldn't happen, since we have already parsed this message successfully
					e.printStackTrace();
				}
				if (o instanceof HashMap) 
				{
					HashMap h = (HashMap)o;
					if (h.containsKey("assumptions") && h.containsKey("goals") && 
						h.get("assumptions") instanceof List && h.get("goals") instanceof List)
					{
						Figure f = new Figure();
						GridLayout gl = new GridLayout(2,false);
						gl.horizontalSpacing = 10;
						f.setLayoutManager(gl);
						List<String> ass = (List)h.get("assumptions");
						List<String> goals = (List)h.get("goals");
						if (!(ass.equals(lastass) ))
							for (String s : ass)
							{
								f.add(new Label("—"));
								f.add(new Label(s));
							}
						f.add(new Label("⊢"));
						for (int i=0; i<goals.size(); i++)
						{
							f.add(new Label(goals.get(i)));
							if (i!=goals.size()-1)
								f.add(new Label("∨"));
						}
						lastass = ass;
						tooltip.add(f);
					}
				}
				lastmsg = msg;
				first = false;
				
			} catch (CoreException e) {
				e.printStackTrace();
			}
		return tooltip;
	}


}

