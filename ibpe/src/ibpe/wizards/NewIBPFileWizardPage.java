package ibpe.wizards;

import java.io.*;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class NewIBPFileWizardPage extends WizardNewFileCreationPage 
{
	public NewIBPFileWizardPage(IStructuredSelection selection)
	{
		super("NewIBPFile", selection);
        setTitle("New invariant based program");
        setDescription("Creates a new invariant based program");
        setFileExtension("ibp");
	}

	@Override
	protected InputStream getInitialContents() 
	{
		// keep only ASCII letters, and numbers

		StringBuffer ctx = new StringBuffer();
		for (int i=0; i< getFileName().length(); i++)
		{
			char ch = getFileName().charAt(i);
			if ( ('0'<=ch && ch<='9') ||
				 ('A'<=ch && ch<'Z') ||
				 ('a'<=ch && ch<'z') )
				ctx.append(ch);
			if (ch=='.')
				break;
		}
		
		String name = ctx.toString();
		if (name.length()==0 || ('0'<=name.charAt(0) && name.charAt(0)<='9'))
			name = "newcontext";
		
		return new StringBufferInputStream(name+": context\nbegin\nend "+name);
		
	}

}