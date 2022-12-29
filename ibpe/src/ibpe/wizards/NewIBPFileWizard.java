package ibpe.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

public class NewIBPFileWizard extends Wizard implements INewWizard {
	
	private IStructuredSelection selection;
    private NewIBPFileWizardPage newFileWizardPage;
    private IWorkbench workbench;
 
    public NewIBPFileWizard() {
        setWindowTitle("New IBP File");
    }
    
    @Override
    public void addPages()
    {
        newFileWizardPage = new NewIBPFileWizardPage(selection);
        addPage(newFileWizardPage);
    }
   
    @Override
    public boolean performFinish()
    {
        IFile file = newFileWizardPage.createNewFile();
        if (file != null)
        {
        	try {
				IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), file);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
            return true;
        }
        else
            return false;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
        this.workbench = workbench;
        this.selection = selection;
    }
}
