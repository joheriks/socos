package ibpe.io;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import ibpe.Activator;
import ibpe.IBPEditor;
import ibpe.action.DebugAction;
import ibpe.figure.IfChoiceFigure;
import ibpe.figure.SituationFigure;
import ibpe.figure.TextRowFigure;
import ibpe.figure.TransitionFigure;
import ibpe.io.SocosInterface.SocosStatus;
import ibpe.part.AbstractIBPEditPart;
import ibpe.part.TransitionLabelPart;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

import javax.lang.model.element.UnknownElementException;

import ibpe.DebuggerView;
 

public class DebuggerThread extends Thread
{
	protected IBPEditor editor;
	protected SocosInterface socos;
	
	protected ArrayList<Runnable> onStateChange = new ArrayList<Runnable>();
	
	public enum DebugState { PAUSED, STEP_WAIT, PLAY_WAIT, STOPPED };
	protected DebugState state;
	
	protected enum DebugCommand { STEPINTO, STOP, PLAY, STEPOVER, PAUSE};
	protected DebugCommand command;
	
	protected int stack_size = 1;
	protected int line=0;
	
	public DebugState getDebugState() { return state; }
	
	public SocosInterface getSocosInterface() { return socos; }
	
	
	
	public DebuggerThread( IBPEditor e )
	{
		editor = e;
		socos = SocosInterface.createSocosInterface(SocosInterface.SocosTask.DEBUG,editor);

		setName("DebuggerThread");
	}
	
	
	public void addStateChangeListener( Runnable r ) {
		onStateChange.add(r);
	}
	
	
	public void removeStateChangeListener( Runnable r ) {
		if (onStateChange.contains(r))
			onStateChange.remove(r);
	}
	
	
	protected void runStateChangeListeners() {
	
		for (Runnable r : onStateChange) {
			Display.getDefault().syncExec(r);
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public void run()
	{
				
		// clear all markers associated with this resource
		IEditorInput ip = (IEditorInput)editor.getEditorInput();
		if (ip instanceof FileEditorInput)
		{
			try {
				editor.getFile().deleteMarkers("fi.imped.socos.IBPE.ibpmarker",
											  false,IResource.DEPTH_INFINITE);
				
				
			}
			catch (CoreException e)	{
				
				e.printStackTrace();
			}
			
		} 
		
		setStatus();
			
		while (state!=DebugState.STOPPED)
		{
			
			try {
								
				synchronized(this)
				{
					
					if (command==null)
						this.wait();
					 
					if (command==DebugCommand.PAUSE)
					{
						state = DebugState.PAUSED;
						runStateChangeListeners();
						command = null;
					}
					
					if (command==DebugCommand.STEPINTO)
					{
						state = DebugState.STEP_WAIT;
						runStateChangeListeners();
						step(false);
						
						if(state!=DebugState.STOPPED)
						{	
							state = DebugState.PAUSED;
							runStateChangeListeners();
							command = null;
						}
					}
					else if(command==DebugCommand.STEPOVER)
					{
						
						state = DebugState.STEP_WAIT;
			
						runStateChangeListeners();
						step(true);
						
						if(state!=DebugState.STOPPED)
						{
							state = DebugState.PAUSED;
							runStateChangeListeners();
							command = null;
						}
					}
					else if(command==DebugCommand.PLAY)
					{ 
						
						state = DebugState.PLAY_WAIT;
						runStateChangeListeners();
						play();
						runStateChangeListeners();
					}
				
				}

			} catch (InterruptedException e) {
				
				state = DebugState.STOPPED;
				runStateChangeListeners();
				break;
			}
		}
			
		runStateChangeListeners();
		socos.killChecker();
		
		interrupt();
		editor.enableEditor();

	}

	

	private void doCommand( DebugCommand cmd )
	{
		command = cmd;
		
		synchronized (this)
		{

			this.notify();
		}
	}
	
	
	public void doStepInto() 
	{
		doCommand(DebugCommand.STEPINTO);
	}
	
	
	public void doStop()
	{

		socos.killChecker();
		state = DebugState.STOPPED;
	
		if(command==null)
			runStateChangeListeners();
	}
	
	
	public void doPause()
	{
		doCommand(DebugCommand.PAUSE);
	}
	
	
	public void doRun()
	{
		doCommand(DebugCommand.PLAY);
	}
	
	
	public void doStepOver()
	{
		doCommand(DebugCommand.STEPOVER);
	}
	

	public void step( boolean over)
	{
		
		socos.handleNextTraceMessage();

		if(socos.getStatus()!=SocosInterface.SocosStatus.ERROR && socos.getStatus()!=SocosInterface.SocosStatus.COMPLETE)
		{
			
			if(over)
				while(socos.getStack()!=null && socos.getStack().size() > stack_size) {
					socos.handleNextMessage();
			}
				
			line=socos.getLine();
			
			if(socos.getStack()!=null)
				stack_size = socos.getStack().size();
		}
		
		setStatus();
		
	}
	
	
	
	public void play()
	{
		
		socos.handleNextMessage();
	
		if(socos.getStack()!=null)
			stack_size = socos.getStack().size();
		
		setStatus();
		
	}
	
	public void setStatus()
	{
		switch(socos.getStatus())
		{
			case COMPLETE:
				state = DebugState.STOPPED;
				break;
			case INCOMPLETE:
				state = DebugState.PAUSED;
				break;
			case ERROR:
				state = DebugState.STOPPED;
		}
		
	}

	
}
