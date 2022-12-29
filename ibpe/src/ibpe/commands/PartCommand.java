package ibpe.commands;

import java.util.Map;

import org.eclipse.gef.commands.Command;

public abstract class PartCommand extends Command {

	protected Map partRegistry;

	public PartCommand( Map editpartRegistry) 
	{
		super();
		partRegistry = editpartRegistry;
	}


}