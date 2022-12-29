package ibpe.commands;

import org.eclipse.gef.commands.Command;

import ibpe.model.*;


/**
 * Command to change the text field
 */
public class UpdateTextCommand extends Command {
	
	private Element elem;
	private String text, oldText;

	
	public UpdateTextCommand( Element target, String newtext )
	{
		elem = target;
		text = newtext;
	}
	
	public void execute() {
		assert text!=null;
		oldText = elem.getText();
		elem.setText(text);
	}

	public void undo() {
		elem.setText(oldText);
	}

	public boolean canExecute()
	{
		return true;
		// TODO: update to use consistent namespace mechanism
		/*
		if(leaf instanceof Header && !(leaf instanceof ProcedureHeader))
		{
			Element gParent = leaf.getParent().getParent();
			Element scope = null;
			
			if(gParent instanceof Situation)
			{
				scope = (Element) gParent.findParentOfClass(Procedure.class);
			}
			else if(gParent instanceof Procedure)
			{
				scope = (Element) gParent.findParentOfClass(Context.class);
			}
			
			if(gParent instanceof PostSituation && ((Procedure) gParent.getParent().getParent()).countCantPost()<=1)
				return false;
			
			if(scope != null && !scope.isUniqueName(leaf, name))
				return false;
		}
		
		Element gParent = leaf.getParent();
				
		if(gParent instanceof TextContainer && name != null && !name.equals(oldName))
			return true;
			else if (gParent instanceof HeaderContainer){
				if (name != null && !name.equals(oldName))
					return true;
				else
				{
					name = oldName;
					return false;
				}
			}
						
		if (name != null && !name.equals(oldName))
			return true;
		else
		{
			name = oldName;
			return false;
		}*/
		
	}


}