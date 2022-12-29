package ibpe.commands;

import ibpe.model.Proof;
import org.eclipse.gef.commands.Command;

public class UpdateProofCommand extends Command 
{
	private Proof proof;
	private String text, oldtext;

	public UpdateProofCommand(Proof proof, String text)
	{
		this.proof=proof;
		this.text=text;
	}

	public void execute()
	{
		oldtext=proof.getText();
		proof.setText(text);
	}

	public void undo()
	{
		proof.setText(oldtext);
	}

	public boolean canExecute()
	{
		return true;
	}

}
