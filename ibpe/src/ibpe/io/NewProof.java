package ibpe.io;

import ibpe.model.*;

import java.util.Collections;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;

public class NewProof extends SequenceFragment
{
	public NewProof()
	{
		super(Collections.singletonList((Element)new Proof()));
	}

	@Override
	public Command adapt(Node parent, Element after)
	{
		CompoundCommand cmd=new CompoundCommand();

		if(parent.getParent() instanceof Transition)
		{
			cmd.add(super.adapt(parent,after));			
		}

		return cmd;
	}

}
