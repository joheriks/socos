package ibpe.tool;

import java.util.ArrayList;

import ibpe.commands.*;
import ibpe.model.Element;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.tools.CreationTool;

public class IBPCreationTool extends CreationTool {
	
	@Override
	protected void handleFinished()
	{
		EditPart select = null;

		if (getCommand()!=null)
		{
			ArrayList<Command> cmds = new ArrayList<Command>();
			cmds.add(getCommand());
			
			while (!cmds.isEmpty())
			{
				Command cmd = cmds.remove(0);
				if (cmd instanceof CompoundCommand)
				{
					cmds.addAll(((CompoundCommand)cmd).getCommands());
				}
				else if (cmd instanceof InsertCommand)
				{
					InsertCommand icmd = (InsertCommand)cmd;
					if (icmd.elements.size()==1)
						select = (EditPart)getCurrentViewer().getEditPartRegistry().get(icmd.elements.get(0));
				}
			}
		}

		super.handleFinished();

		getDomain().loadDefaultTool();
		
		if (select!=null)
			select.getViewer().select(select);
		
	}
	

}
