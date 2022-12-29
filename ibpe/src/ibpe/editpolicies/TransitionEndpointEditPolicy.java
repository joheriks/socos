package ibpe.editpolicies;

import ibpe.part.*;
import ibpe.figure.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.handles.ConnectionEndpointHandle;
import org.eclipse.gef.requests.ReconnectRequest;

public class TransitionEndpointEditPolicy extends ConnectionEndpointEditPolicy 
{
	@Override
	protected void addSelectionHandles()
	{
		super.addSelectionHandles();
		getConnectionFigure().setLineWidth(2);
	}
	

	@Override
	public TransitionPart getHost() 
	{
		return (TransitionPart)super.getHost();
	}
	
	
	protected TransitionFigure getConnectionFigure() 
	{
		return getHost().getFigure();
	}
	
	
	@Override
	protected void showConnectionMoveFeedback(ReconnectRequest request) 
	{
		super.showConnectionMoveFeedback(request);
		
		getConnectionFigure().showChoiceIndicator(false);
	}	
	
	
	@Override
	protected void eraseConnectionMoveFeedback(ReconnectRequest request) 
	{
		super.eraseConnectionMoveFeedback(request);
		
		// possibly restore the choice indicator which was hidden in showConnectionMoveFeedback
		getHost().refresh();
	}	

	
	@Override
	protected void removeSelectionHandles()
	{
		super.removeSelectionHandles();
		getConnectionFigure().setLineWidth(0);
	}

	
	@SuppressWarnings("unchecked")
	protected List createSelectionHandles()
	{
		List list = new ArrayList();
		list.add(new ConnectionEndpointHandle((ConnectionEditPart)getHost(), ConnectionLocator.SOURCE)
			{
				// This method is used to choose white or black fill color.
				// Therefore we override it here, as it fits our purposes.
				@Override
				protected boolean isPrimary()
				{
					return getOwner() instanceof TransitionPart && 
						   ((TransitionPart)getOwner()).getSourceConnectionAnchor() instanceof MoveableAnchor &&
						   ((MoveableAnchor)((TransitionPart)getOwner()).getSourceConnectionAnchor()).isManualAnchor();
				}
			});
		
		list.add(new ConnectionEndpointHandle((ConnectionEditPart)getHost(), ConnectionLocator.TARGET)
			{
				@Override
				protected boolean isPrimary()
				{
					return getOwner() instanceof TransitionPart &&
						   ((TransitionPart)getOwner()).getTargetConnectionAnchor() instanceof MoveableAnchor &&
						   ((MoveableAnchor)((TransitionPart)getOwner()).getTargetConnectionAnchor()).isManualAnchor();
				}
			});
		
	 	return list;
	}

}
