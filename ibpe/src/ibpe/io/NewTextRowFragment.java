package ibpe.io;

import ibpe.model.*;
import ibpe.commands.*;

import java.util.*;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;

public class NewTextRowFragment extends SequenceFragment 
{
	public NewTextRowFragment()
	{
		super(Collections.singletonList((Element)new TextRow("new text")));
	}
	
	@Override
	public Command adapt( Node parent, Element after )
	{
		CompoundCommand cmd = new CompoundCommand();
		cmd.add(super.adapt(parent,after));
		String txt = null;

		if (parent.getParent() instanceof Procedure || parent instanceof Context)
			txt = "new declaration";
		else if (parent.getParent() instanceof BoxElement)
		{
			Situation s = (Situation)parent.getParent();
		
			if (parent==s.declarationContainer)
				txt = "new declaration";
			else if (parent==s.constraintContainer)
				txt = "new constraint";
			else
				txt = "new variant";
		}
		else if (parent.getParent() instanceof Transition)
			txt = "new statement";
		if (txt!=null)
			cmd.add(new UpdateTextCommand(contents.get(0),txt));
		return cmd;
	}
}
