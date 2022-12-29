package ibpe.action;

import ibpe.io.DebuggerThread;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class StepOverAction extends DebugAction {

	public StepOverAction(IEditorPart part) {
		super(part);
		setId("DEBUG_STEPOVER");
		setText("StepOver");
		setToolTipText("Step over");
		
		//setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/debug-step-over.png"));
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
		createDebugger().doStepOver();
	}

}
