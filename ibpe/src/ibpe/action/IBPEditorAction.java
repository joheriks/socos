package ibpe.action;

import ibpe.Activator;
import ibpe.IBPEditor;
import ibpe.io.LocalSocosInterface;
import ibpe.io.RemoteSocosInterface;
import ibpe.io.SocosInterface;
import ibpe.io.SocosInterface.SocosTask;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.FileEditorInput;

public class IBPEditorAction extends EditorPartAction {
	
	IBPEditor editor = (IBPEditor)getEditorPart();
	MessageConsole console;

	public IBPEditorAction(IEditorPart editor) {
		super(editor);
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}
	
	protected void saveCurrent() 
	{		
		// save file associated to editor
		try {
			editor.save(editor.getPartName());
			editor.getCommandStack().markSaveLocation();
			IEditorInput ip = (IEditorInput)editor.getEditorInput();
			
			// refresh file system after saving
			if (ip instanceof FileEditorInput)
			{
				try {
					((FileEditorInput)ip).getFile().refreshLocal(1,null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
}
