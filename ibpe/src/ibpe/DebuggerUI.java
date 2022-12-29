package ibpe;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import ibpe.action.DebugAction;
import ibpe.figure.IfChoiceFigure;
import ibpe.figure.MultiCallFigure;
import ibpe.figure.SituationFigure;
import ibpe.figure.TextRowFigure;
import ibpe.figure.TransitionFigure;
import ibpe.io.DebuggerThread;
import ibpe.io.SocosInterface;
import ibpe.io.DebuggerThread.DebugState;
import ibpe.part.*;

import static ibpe.io.DebuggerThread.*;

public class DebuggerUI {
	
	protected final IBPEditor editor;
	protected final DebuggerThread debugger;
	
	public DebuggerUI( IBPEditor e ) {
		
		editor = e;		
		debugger = new DebuggerThread(editor);		

		final Runnable highlighter = new Runnable() {
			
			IFigure lastFigure;
			IFigure lastTransitionFigure;
			List<IFigure> sc;
			
			public void run() { 
				unhighlight();
				if (debugger.getDebugState()!=DebuggerThread.DebugState.STOPPED && debugger.getDebugState()!=DebuggerThread.DebugState.PLAY_WAIT)
					highlight();
			}
			
			public void unhighlight()
			{
				if (lastFigure!=null) {
					changeColor(lastFigure,false);
					changeColor(lastFigure,false,false);
					lastFigure = null;
				}
				if (lastTransitionFigure!=null) {
					changeColor(lastTransitionFigure,false);
					changeColor(lastTransitionFigure,false,false);
					lastTransitionFigure = null;
				}
				if(sc !=null){
					for (Object c : sc) {
						if(c!=null)
						changeColor(((TransitionPart)c).getFigure().getChoiceIndicator(),false);
					
					}
				}
								
			}
			
			
			public void highlight()
			{
				IFigure f = null;
				AbstractIBPEditPart<?> p = editor.getMarkedPart(debugger.getSocosInterface().getLine()-1);
			
				if (p!=null)
				{
					// assume that stack is non-empty
					Object o = debugger.getSocosInterface().getStack().get(0);
					String name = (String)((HashMap)o).get("name");
					boolean firstchoice = name.equals("choice") || name.equals("if") || name.equals("call");
					
					f = p.getFigure();
				
					if (f instanceof IfChoiceFigure)
						changeColor(f,true);
					else if (firstchoice) {
						// highlight
						sc = null;
						if (p instanceof PreconditionPart || p instanceof SituationPart ) 
							sc = p.getSourceConnections();
						
						for (Object c : sc) {
							changeColor(((TransitionPart)c).getFigure().getChoiceIndicator(),true);
						
						}
					}
					else if(name.equals("goto"))
						changeColor(f,true,true);
					if (!(f instanceof TransitionFigure)){
						AbstractIBPEditPart<?> parent = (AbstractIBPEditPart<?> )p.getParent(); 
						
						if (parent instanceof TransitionLabelPart)
						{	
							IFigure parent_p = null;		
							AbstractIBPEditPart<?>	parent2 = (AbstractIBPEditPart<?> )parent.getParent();
							parent_p = parent2.getFigure();	
							changeColor(parent_p,true);
							lastTransitionFigure = parent_p;
						}
						
				
					}
					else if (f instanceof SituationFigure)
					{
						lastTransitionFigure = null;
					}

					if(!name.equals("goto") && !firstchoice )
						changeColor(f,true);

				}
				lastFigure = f;
			}
			
			public void changeColor( IFigure f, boolean s ) {
				changeColor(f,s,false);
			}
			
			public void changeColor(IFigure f, boolean s, boolean transitionEnd) {
				
				boolean activestate = s;

	        	if (f instanceof SituationFigure )
	        		((SituationFigure)f).setActive(activestate);
	        	if (f instanceof TransitionFigure ) {
	        		TransitionFigure tf = (TransitionFigure)f;
	        		tf.setActive(activestate);
	        		tf.setActiveEnd(transitionEnd);
	        	}
	        	if (f instanceof TextRowFigure )
	        		((TextRowFigure)f).setActive(activestate);
	        	if (f instanceof IfChoiceFigure)
	        		((IfChoiceFigure)f).setActive(activestate);
	        	if(f instanceof MultiCallFigure)
	        		((MultiCallFigure)f).setActive(activestate);
	        	
	        	
	        	f.revalidate();
	        	f.repaint();
	        }
			

		};
		
		
		
		debugger.addStateChangeListener(highlighter);
		
		
		// to be done: edit this part when the editor is closed while debugging, debugger should stop 
		IPartService service =
				(IPartService)editor.getEditorSite().getService(IPartService.class);
				                service.addPartListener(new IPartListener() {

				                        public void partActivated(IWorkbenchPart part) {}

				                        public void partBroughtToTop(IWorkbenchPart part) {}

				                        public void partClosed(IWorkbenchPart part) {
				                        	((DebugAction)editor.getActionRegistry().getAction("DEBUG_STOP")).run();
				                        }

				                        public void partDeactivated(IWorkbenchPart part) {}

				                        public void partOpened(IWorkbenchPart part) {}
				                       
				                });
				                
		
		debugger.addStateChangeListener(new Runnable() 
		{ 
			public void run() { 
				((DebugAction)editor.getActionRegistry().getAction("DEBUG_PLAY")).updateEnabledState();
				((DebugAction)editor.getActionRegistry().getAction("DEBUG_STEPINTO")).updateEnabledState();
				((DebugAction)editor.getActionRegistry().getAction("DEBUG_STEPOVER")).updateEnabledState();
				((DebugAction)editor.getActionRegistry().getAction("DEBUG_STOP")).updateEnabledState();
				((DebugAction)editor.getActionRegistry().getAction("DEBUG_PAUSE")).updateEnabledState();
				
				
				if (debugger.getDebugState() == DebugState.STOPPED)
				{
					if(DebuggerView.instance!=null)
						DebuggerView.instance.updateTree(null);
				
				}else 
				if(debugger.getSocosInterface().getStack()!=null && DebuggerView.instance!=null)
					DebuggerView.instance.updateTree(debugger.getSocosInterface().getStack());

		} });
		
		debugger.addStateChangeListener(new Runnable() {
			public void run() {
				if(PlatformUI.getWorkbench().getActiveWorkbenchWindow()!=null)
				{
					Shell sh = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					Cursor c = null;
					if (debugger.getDebugState()==DebugState.PLAY_WAIT ||
					    debugger.getDebugState()==DebugState.STEP_WAIT) 
						c = Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT);
					sh.setCursor(c);
				}
			}
		});
		 
	
		debugger.addStateChangeListener( new Runnable() {
			DebuggerThread.DebugState prevState;
			
			public void run() {	
				if (prevState!=DebugState.STOPPED && debugger.getDebugState()==DebugState.STOPPED)
					debugger.getSocosInterface().displaySummary(true);
				prevState = debugger.getDebugState(); 
			}	
			
		});


		debugger.start();

	}

	public DebuggerThread getDebugger() {
		return debugger;
	}
	
}
