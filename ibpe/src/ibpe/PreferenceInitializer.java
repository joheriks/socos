package ibpe;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.*;

public class PreferenceInitializer extends AbstractPreferenceInitializer 
{

	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault("SOCOS_CHECKER","plugin");
		store.setDefault("SOCOS_PATH",System.getProperty("user.home")+"/pc/socos");
		store.setDefault("SOCOS_CHECKER_URL","");

		store.setDefault("SOCOS_STRATEGY","(endgame)");
		store.setDefault("SOCOS_SHOW_SUMMARY",true);
	}

}
