package ibpe.action;

import ibpe.io.DebuggerThread;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class PauseAction extends DebugAction {
	
public PauseAction(IEditorPart part) {
		
		super(part);
		setId("DEBUG_PAUSE");
		setText("Pause");
		setToolTipText("Pause execution");
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/pauses.png"));
		
		setEnabled(false);
	}
	
	
	@Override
	protected boolean calculateEnabled() {
		if(debuggerUI == null)
			return false;
		else
			return getDebugger().getDebugState()!=DebuggerThread.DebugState.PAUSED;
	}
	
	
	@Override
	public void updateEnabledState() {
		
		setEnabled(calculateEnabled());
	}

	
	@Override 
	public void run()
	{
		getDebugger().doPause();	
	
	}

}
