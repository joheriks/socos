package ibpe.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import java.util.concurrent.*;
import ibpe.io.DebuggerThread;

public class StepIntoAction extends DebugAction {
	
	public StepIntoAction(IEditorPart part) {
		
		super(part);
		setId("DEBUG_STEPINTO");
		setText("StepInto");
		setToolTipText("Step Into");
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/debug-step-into.png"));
		
		setEnabled(true);
	
	}
	
	@Override
	protected boolean calculateEnabled() {
		
		return debuggerUI==null || getDebugger().getDebugState()==DebuggerThread.DebugState.PAUSED;
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
		createDebugger().doStepInto();
	}

}
