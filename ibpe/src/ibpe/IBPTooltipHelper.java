package ibpe;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolTipHelper;
import org.eclipse.swt.widgets.Control;

import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.draw2d.geometry.Dimension;

public class IBPTooltipHelper extends ToolTipHelper
{
	/*
	 * This class is a terrible hack: it copies the entire Eclipse implementation,
	 * of ToolTipHelper, disabling only the tooltip popup timer. Eclipse draw2d tooltips
	 * should have a configurable timeout.
	 */
	
	//private Timer timer;
	private IFigure currentTipSource;


	public IBPTooltipHelper(Control c) {
		super(c);
	}
	
	
	private Point computeWindowLocation(IFigure tip, int eventX, int eventY) {
		org.eclipse.swt.graphics.Rectangle clientArea = control.getDisplay()
				.getClientArea();
		Point preferredLocation = new Point(eventX, eventY + 26);

		Dimension tipSize = getLightweightSystem().getRootFigure()
				.getPreferredSize().getExpanded(getShellTrimSize());

		// Adjust location if tip is going to fall outside display
		if (preferredLocation.y + tipSize.height > clientArea.height)
			preferredLocation.y = eventY - tipSize.height;

		if (preferredLocation.x + tipSize.width > clientArea.width)
			preferredLocation.x -= (preferredLocation.x + tipSize.width)
					- clientArea.width;

		return preferredLocation;
	}


	public void displayToolTipNear(IFigure hoverSource, IFigure tip,
			int eventX, int eventY) {
		if (tip != null && hoverSource != currentTipSource) {
			getLightweightSystem().setContents(tip);
			Point displayPoint = computeWindowLocation(tip, eventX, eventY);
			Dimension shellSize = getLightweightSystem().getRootFigure()
					.getPreferredSize().getExpanded(getShellTrimSize());
			setShellBounds(displayPoint.x, displayPoint.y, shellSize.width,
					shellSize.height);
			show();
			currentTipSource = hoverSource;
			/*
			timer = new Timer(true);
			timer.schedule(new TimerTask() {
				public void run() {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							hide();
							timer.cancel();
						}
					});
				}
			}, 100);
			*/
		}
	}


	public void dispose() {
		if (isShowing()) {
			//timer.cancel();
			hide();
		}
		getShell().dispose();
	}


	protected void hookShellListeners() {
		// Close the tooltip window if the mouse enters the tooltip
		getShell().addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
				hide();
				currentTipSource = null;
				//if (timer != null) {
				//	timer.cancel();
				//}
			}
		});
	}

public void updateToolTip(IFigure figureUnderMouse, IFigure tip,
			int eventX, int eventY) {
		if (figureUnderMouse == null) {
			if (isShowing()) {
				hide();
				//timer.cancel();
			}
		}
		if (isShowing() && figureUnderMouse != currentTipSource) {
			hide();
			//timer.cancel();
			displayToolTipNear(figureUnderMouse, tip, eventX, eventY);
		} else if (!isShowing() && figureUnderMouse != currentTipSource)
			currentTipSource = null;
	}
	
}
