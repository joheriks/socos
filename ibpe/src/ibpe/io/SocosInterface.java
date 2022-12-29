package ibpe.io;

import ibpe.*;
import ibpe.figure.SituationFigure;
import ibpe.figure.TextRowFigure;
import ibpe.figure.TransitionFigure;
import ibpe.part.AbstractIBPEditPart;
import ibpe.part.TransitionLabelPart;
import ibpe.action.IBPEditorAction;

import java.io.*;
import java.util.*;

import org.antlr.runtime.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.*;
import org.eclipse.ui.console.*;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.core.filesystem.*;

import ibpe.DebuggerView;

@SuppressWarnings("rawtypes")
public abstract class SocosInterface 
{
	protected IBPEditor editor;

	protected int line;
	protected ArrayList stack;
	
	public enum SocosTask { CHECK, DEBUG }; 
	public SocosTask task;
	
	protected CharStream stream;
	protected boolean errors = false;
	
	public enum SocosStatus { ERROR, 
							  CHECKING_UNFINISHED, INCOMPLETE, COMPLETE,
							  DEBUG };
	
	protected SocosStatus status;

	protected Double time = null;
	protected boolean warnings = false;
	protected String summary = "";
	
	protected int progress=-1,maxprogress=-1;
	
	protected boolean is_summary = false;

	// The following methods must be implemented by subclasses.
	protected abstract CharStream initChecker();
	
	protected abstract void killChecker();
	
	public abstract String getDescription();

	
	public static SocosInterface createSocosInterface( SocosTask t, IBPEditor e )
	{
		if (Activator.getDefault().getPreferenceStore().getString("SOCOS_CHECKER").equals("remote"))
			return new RemoteSocosInterface(t,e);	
		else
			return new LocalSocosInterface(t,e);
	}


	public SocosInterface( SocosTask t, IBPEditor ibpEditor )
	{
		editor = ibpEditor;
		task = t;
		if (task==SocosTask.CHECK) 
			status = SocosStatus.CHECKING_UNFINISHED;
		else if (task==SocosTask.DEBUG) 
			status = SocosStatus.DEBUG;
		editor.disableEditor();
		stream = initChecker();
		if (stream==null)
			status = SocosStatus.ERROR;
		
	}
	
	public SocosStatus getStatus() { return status; }
	
	public void setSt(SocosStatus st) { status=st; }
	
	public String getSummary() { return summary; }	
	
	public Double getTime() { return time; }
	
	public int getProgress() { return progress; }
	
	public int getMaxProgress() { return maxprogress; }

	public int getLine() { return line; }
	
	public ArrayList getStack() { return stack; }
	
	public boolean isSummary() { return is_summary; }
	
	public boolean hasWarnings() { return warnings; }
	
	public boolean hasFinished() { return status==SocosStatus.ERROR || status==SocosStatus.COMPLETE;  }
	
	
	void handleNextTraceMessage( ) {
		HashMap msg;
		while ( !hasFinished() && ((msg=handleNextMessage())!=null) && !getType(msg).equals("TRACE") )
			;
	}
	
	public HashMap handleNextMessage( )
	{
		HashMap msg = null;
		try {
			Object o = parseJSON(stream);
			if ( !(o instanceof HashMap) || getType((HashMap)o)==null )
			{
				summary = "Unknown message: "+JSONSerializer.JSONtoString(o);
				status = SocosStatus.ERROR;
				return null;
			}
			msg = (HashMap)o;
		
		} catch (IOException e) {
			status = SocosStatus.ERROR;
			summary = e.getMessage();
			return null;
		} catch (RecognitionException e) {
			// Bail out at first parse error.
			status = SocosStatus.ERROR;
			System.err.println(e);
			return null;
		}

		if (getType(msg).equals("SUMMARY"))	handleSummary(msg);
		else if (getType(msg).equals("PROGRESS"))	handleProgress(msg);
		else if (getType(msg).equals("WARNING"))	handleWarning(msg);
		else if (getType(msg).equals("ERROR"))	handleError(msg);
		else if (getType(msg).equals("CONDITION"))	handleCondition(msg);
		else if (getType(msg).equals("TRACE"))	handleRunning(msg);
		else
		{
			summary = "Unhandled message type: "+getType(msg);
			status = SocosStatus.ERROR;
		}
		return msg;
	}
	

	protected void handleSummary(  HashMap msg )
	{
		if (msg.containsKey("status"))
		{
			String s = (String)msg.get("status");
			is_summary = true;
			if ("complete".equals(s))
				status = SocosStatus.COMPLETE;
			else if ("incomplete".equals(s))
				status = SocosStatus.INCOMPLETE;
		}
	}
	

	protected void handleProgress(  HashMap msg )
	{
		if (msg.containsKey("pnow") && msg.containsKey("pmax"))
		{
			progress = (Integer)msg.get("pnow");
			maxprogress = (Integer)msg.get("pmax");
		}		
		if (msg.containsKey("time"))
		{
			time = (Double)msg.get("time");
		}
		
	}

	
	protected void handleWarning( HashMap msg )
	{
		warnings = true;
		createMarker(msg);
	}

	
	protected void handleError( HashMap msg )
	{
		status = SocosStatus.ERROR;
		createMarker(msg);
	}
	

	protected void handleCondition( HashMap msg )
	{
		createMarker(msg);
	}
	

	protected void handleRunning(HashMap msg)
	{
		line = getLine(msg);
		stack = getStack(msg);
			
	}
	
	

	protected void createMarker( HashMap msg )
	{
	
		IMarker mr = null;
		IFile f = null;
		
		try
		{
			f = editor.getFile();
			mr = f.createMarker("fi.imped.socos.IBPE.ibpmarker");

			mr.setAttribute("orig_message",JSONSerializer.JSONtoString(msg));

			mr.setAttribute("short_message",(String)msg.get("message"));
		
			// For unproved conditions, add also the sequent to the message
			String s = (String)msg.get("message");  
			if (msg.containsKey("assumptions") && msg.containsKey("goals"))
			{
				List<String> ass = (List<String>)msg.get("assumptions"); 
				List<String> goals = (List<String>)msg.get("goals"); 
				s += "\n";
				for (String a : ass)
					s += "—" + a + "\n";
				s += "⊢";
				for (int i=0; i<goals.size(); i++)
					s += (i==0 ? " " : "∨") + goals.get(i) + (i==goals.size()-1 ? "" : "\n");
				
			}
			mr.setAttribute(IMarker.MESSAGE,s);
			
			mr.setAttribute(IMarker.LINE_NUMBER,getLine(msg));
			mr.setAttribute(IMarker.CHAR_START,getColumn(msg));
			mr.setAttribute(IMarker.CHAR_END,getColumn(msg));
			mr.setAttribute(IMarker.SEVERITY,
			getType(msg).equals("WARNING") ? IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR);			
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	
	public void displaySummary(boolean debug)
	{
		String status = "";
		int style = 0;
		switch (getStatus())
		{
		case CHECKING_UNFINISHED:
			status += "FAILED OR CANCELED";
			style = SWT.ICON_ERROR;
			break;
		case COMPLETE: 
			status += hasWarnings() ? "COMPLETE WITH WARNINGS" : "COMPLETE";
			style = hasWarnings() ? SWT.ICON_WARNING : SWT.ICON_WORKING;
			break;
		case INCOMPLETE: 
		case DEBUG:
			status += "INCOMPLETE";
			style = SWT.ICON_ERROR;
			break;
		case ERROR:
			status += "ERROR";
			style = SWT.ICON_ERROR;
			break;
		}
				
		if (Activator.getDefault().getPreferenceStore().getBoolean("SOCOS_SHOW_SUMMARY"))
		{
			MessageBox box = new MessageBox(IBPEditor.shell, SWT.OK|style);
			if(debug)
				box.setText("Execution summary");
			else
				box.setText("Verification summary");
			String msg = "File: "+editor.getPartName() + "\nStatus: "+status;
			if (getTime()!=null)
				msg = msg + "\n" + "Time: " + getTime(); 
			if (getSummary()!=null)
				msg = msg + "\n" + getSummary();
			box.setMessage(msg);
			box.open();
			editor.enableEditor();
		}
		
		editor.marker();
	}
	
	
	protected String getType( HashMap msg )
	{
		return (msg.containsKey("type")) ? (String)msg.get("type") : null;
	}

	
	protected int getLine( HashMap msg )
	{
		return (msg.containsKey("line")) ? (Integer)msg.get("line") : -1;
	}
	
	
	protected ArrayList getStack( HashMap msg )
	{
		return (msg.containsKey("stack")) ? (ArrayList)msg.get("stack") : null;	
	}
	
	
	protected int getColumn( HashMap msg )
	{
		return (msg.containsKey("column")) ? (Integer)msg.get("column") : -1;
	}


	protected Object parseJSON( CharStream in ) throws IOException, RecognitionException 
	{
	    JSONLexer lexer = new JSONLexer(in);
	    UnbufferedTokenStream tokens = new UnbufferedTokenStream(lexer);
        JSONParser parser = new JSONParser(tokens);
        JSONParser.value_return r = parser.value();
        return r.result;
	 }
	
}
