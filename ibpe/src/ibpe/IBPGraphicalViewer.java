package ibpe;

import ibpe.figure.BoxContainerFigure;
import ibpe.figure.IfChoiceFigure;
import ibpe.figure.PreconditionFigure;
import ibpe.figure.ProcedureFigure;
import ibpe.figure.TextContainerFigure;
import ibpe.figure.TransitionFigure;

import org.eclipse.draw2d.EventDispatcher;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolTipHelper;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Handle;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.ui.parts.DomainEventDispatcher;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;

public class IBPGraphicalViewer extends ScrollingGraphicalViewer 
{
	private EventDispatcher eventDispatcher;

	class IBPEventDispatcher extends DomainEventDispatcher
	{
		public IBPEventDispatcher(EditDomain d, EditPartViewer v) {
			super(d, v);
			// TODO Auto-generated constructor stub
		}

		private IBPTooltipHelper tooltipHelper;
		
		protected ToolTipHelper  getToolTipHelper()
		{
			if (tooltipHelper==null)
				tooltipHelper = new IBPTooltipHelper(control);
			return tooltipHelper;
		}
	};
	
	public void setEditDomain(EditDomain domain) {
		super.setEditDomain(domain);
		// Set the new event dispatcher, even if the new domain is null. This
		// will dispose
		// the old event dispatcher.
		getLightweightSystem().setEventDispatcher(
				eventDispatcher = new IBPEventDispatcher(domain, this));
	}

	protected static final class ForkFigureSearch implements TreeSearch
	{
		public boolean accept(IFigure figure)
		{
			return figure instanceof IfChoiceFigure;
		}

		public boolean prune(IFigure figure)
		{
			return false;
		}
	}

	public boolean forkFigureAt(Point p) 
	{
		IFigure rootFigure=getLightweightSystem().getRootFigure();
		IFigure figure=rootFigure.findFigureAt(p.x, p.y, new ForkFigureSearch());
		return figure instanceof IfChoiceFigure;
	}

	@Override
	public FigureCanvas getFigureCanvas() {
		return super.getFigureCanvas();
	}
	
}
