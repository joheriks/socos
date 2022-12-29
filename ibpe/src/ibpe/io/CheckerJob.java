package ibpe.io;

import ibpe.Activator;
import ibpe.IBPEditor;
import ibpe.io.SocosInterface.SocosStatus;
import ibpe.part.AbstractIBPEditPart;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

public class CheckerJob extends Job 
{
	protected IBPEditor editor;
	protected SocosInterface socos;
	protected IProgressMonitor monitor;
	
	public CheckerJob( IBPEditor e ) 
	{
		super("Checking");
		setUser(true);
		editor = e;
		socos = SocosInterface.createSocosInterface(SocosInterface.SocosTask.CHECK,editor);
		
	}
	
	@Override
	protected synchronized void canceling() 
	{
		socos.killChecker();
		editor.enableEditor();
    }


	@Override
	public IStatus run( IProgressMonitor m )
	{
		monitor = m;
		
		CharStream stream = null;

		// clear all markers associated with this resource
		IEditorInput ip = (IEditorInput)editor.getEditorInput();
	

		if (ip instanceof FileEditorInput || ip instanceof FileStoreEditorInput)
		{
			try {
			
			//	((FileEditorInput)ip).getFile().deleteMarkers("fi.imped.socos.IBPE.ibpmarker",
			//								  false,IResource.DEPTH_INFINITE);
				editor.getFile().deleteMarkers("fi.imped.socos.IBPE.ibpmarker",
						  false,IResource.DEPTH_INFINITE);
				
			}
			catch (CoreException e)	{
				e.printStackTrace();
			}
		}
		

		// initialize connection to checker
		stream = socos.initChecker();
		
		boolean haveSetMaxProgress = false;
		int progress = 0;
		// loop until end-of-messages, canceled, or error
		while (stream!=null && !monitor.isCanceled() && socos.getStatus()==SocosInterface.SocosStatus.CHECKING_UNFINISHED)
		{
			socos.handleNextMessage();
			
			if (!haveSetMaxProgress && socos.getMaxProgress()!=-1)
			{
				monitor.beginTask(socos.getDescription(),socos.getMaxProgress());
				haveSetMaxProgress = true;
			}
			
			if (haveSetMaxProgress)
			{
				monitor.worked(socos.getProgress()-progress);
				progress = socos.getProgress();
			}
		}

		monitor.done();
		
		// Display the summary and show markers in the graphical editor;
		// this must be executed in the UI thread
		Display.getDefault().asyncExec( new Runnable() { public void run() { socos.displaySummary(false); } } );
//		editor.enableEditor();
		return Status.OK_STATUS;
	}

	
}
