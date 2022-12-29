package ibpe.action;

import ibpe.io.DebuggerThread;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public class PlayAction extends DebugAction {

	public PlayAction(IEditorPart part) {
		super(part);
		
		setId("DEBUG_PLAY");
		setText("Play");
		setToolTipText("Play");
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/debug.png"));
		
		setEnabled(true);
	}
	
	@Override
	protected boolean calculateEnabled() {
	
		return debuggerUI == null || getDebugger().getDebugState()==DebuggerThread.DebugState.STOPPED
					 		      || getDebugger().getDebugState()==DebuggerThread.DebugState.PAUSED;
		
	}

	@Override
	public void updateEnabledState() {
		if (debuggerUI!=null && getDebugger().getDebugState()==DebuggerThread.DebugState.STOPPED)
			debuggerUI = null;
		
		setEnabled(calculateEnabled());
	}


	
	@Override 
	public void run()
	{
		createDebugger().doRun();
	}
		

}
 