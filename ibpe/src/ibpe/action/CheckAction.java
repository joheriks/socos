package ibpe.action;

import ibpe.*;
import ibpe.io.*;
import ibpe.io.SocosInterface.SocosTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.*;
import org.eclipse.ui.console.*;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.ui.actions.*;
import org.eclipse.jface.dialogs.*;

import java.io.IOException;
import java.lang.reflect.*;

public class CheckAction extends IBPEditorAction
{
	
	public CheckAction(IEditorPart part)
	{
		super(part);

		setId("CHECK_FILE");
		setText("Check Correctness");
		setToolTipText("Check Correctness");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE","icons/verify.png"));
		
		setEnabled(true);
	}


	@Override 
	protected boolean calculateEnabled()
	{ 
		return true;
	} 
	

	@Override 
	public void run()
	{ 			
		saveCurrent();
	
		// Activate Problems view 
		try {
			editor.getSite().getPage().showView(IPageLayout.ID_PROBLEM_VIEW);
		}
		catch (PartInitException e)
		{
			System.out.println("Unable to show Problems view");
			e.printStackTrace();
		}
		
		editor.clearDisplayedMarkers();

		new CheckerJob(editor).schedule();
	}
}
