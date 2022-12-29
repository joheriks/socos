package ibpe.action;


import ibpe.DebuggerUI;
import ibpe.IBPEditor;
import ibpe.io.DebuggerThread;
import ibpe.io.SocosInterface.SocosTask;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import java.util.concurrent.*;

public class DebugAction extends IBPEditorAction {
	
	protected static DebuggerUI debuggerUI;

	@Override
	protected boolean calculateEnabled() {
		return true;
	}
	
	
	public void updateEnabledState() {
		setEnabled(calculateEnabled());
	}
	
	
	public DebugAction(IEditorPart part)
	{	
		super(part);
		
	}

	
	public DebuggerThread createDebugger() 
	{	
		if (debuggerUI==null) {
			saveCurrent();
			editor.clearDisplayedMarkers();
			
			// Activate Debugger view 
			try {
				editor.getSite().getPage().showView("fi.imped.socos.IBPE.DebuggerView");
			}
			catch (PartInitException e)
			{
				System.out.println("Unable to show Debugger view");
				e.printStackTrace();
			}
			
			debuggerUI = new DebuggerUI(editor);
		}
		return getDebugger();
	}

	
	public static DebuggerThread getDebugger() 
	{
		if (debuggerUI==null)
			return null;
		return debuggerUI.getDebugger();
	}

}
