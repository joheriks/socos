package ibpe;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.*;


public class IBPPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage 
{
	public IBPPreferences()
	{
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		//setDescription("Environmental settings and visual preferences");
	}
	
	public void createFieldEditors()
	{
		addField(new RadioGroupFieldEditor("SOCOS_CHECKER", "Checker:", 1,
				                           new String[][] { {"Plugin","plugin"},{"Local","local"},{"Remote","remote"} }, 
				                           getFieldEditorParent()));
		addField(new StringFieldEditor("SOCOS_PATH","Local checker path:",getFieldEditorParent()));
		addField(new StringFieldEditor("SOCOS_CHECKER_URL","Checker URL:",getFieldEditorParent()));

		addField(new StringFieldEditor("SOCOS_STRATEGY","Default strategy:",getFieldEditorParent()));
		addField(new BooleanFieldEditor("SOCOS_SHOW_SUMMARY","Display summary",getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench) {}
	
}