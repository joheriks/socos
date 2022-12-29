package ibpe.action;

import ibpe.io.DebuggerThread;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class StopAction extends DebugAction {
	
	public StopAction(IEditorPart part) {
		
		super(part);
		setId("DEBUG_STOP");
		setText("Stop");
		setToolTipText("Stop execution");
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/stop.png"));
		
		setEnabled(false);
	}
	
	
	@Override
	protected boolean calculateEnabled() {
		return debuggerUI!=null;
	}

	
	@Override
	public void updateEnabledState() {
		
		setEnabled(calculateEnabled());
	}
	
	
	@Override 
	public void run()
	{
		DebuggerThread debugger = getDebugger();
		debuggerUI = null;
		if (debugger!=null)
			debugger.doStop();	
	}

}
