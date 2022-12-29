package ibpe.io;

import java.io.*;
import java.net.URL;
import java.util.*;

import ibpe.Activator;
import ibpe.IBPEditor;
import ibpe.io.SocosInterface.SocosTask;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.console.*;
import org.osgi.framework.Bundle;



public class LocalSocosInterface extends SocosInterface 
{ 
	Process shell;
	int pid;
	
	public LocalSocosInterface( SocosTask task, IBPEditor ibpEditor )
	{
		super(task,ibpEditor);
	}

	
	public String getDescription()
	{
		return "Using: "+getSocosPath();
	}
	
	
	protected String getSocosPath() 
	{
		if (Activator.getDefault().getPreferenceStore().getString("SOCOS_CHECKER").equals("plugin")) {
			Bundle b = Platform.getBundle("fi.imped.socos.PC");
			if (b==null) 
				return null;
			Path path = new Path("pc/socos");
			URL fileURL;
			try {
				fileURL = FileLocator.toFileURL(FileLocator.find(b, path, null));
			} catch (IOException e) {
				return null;
			}
			return fileURL.getPath();
		}
		else {
			return Activator.getDefault().getPreferenceStore().getString("SOCOS_PATH");
		}
	}

	
	@Override
	protected CharStream initChecker()  
	{
		try {
			String socosPath = getSocosPath();
			if (socosPath==null)
				throw new IOException("Checker plugin (ID: fi.imped.socos.pc) not installed");

			String filePath = editor.getPartName();
			
			List<String> args = new ArrayList<String>();
			args.add(socosPath);
			args.add(filePath);
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.environment().put("SOCOS_COLOR", "");
			pb.environment().put("SOCOS_GENERATE_PVS","");
			pb.environment().put("SOCOS_LOG","");
			pb.environment().put("SOCOS_NOWRAP","1");
			pb.environment().put("SOCOS_JSON","1");
			pb.environment().put("SOCOS_PIPE","");

			// create .pvs, .prf files in fresh temporary directory
			pb.environment().put("SOCOS_OUTPUT_DIR","");
			
			if (task==SocosTask.DEBUG)
				pb.environment().put("SOCOS_TRACE","1");
			else
				pb.environment().put("SOCOS_TRACE","");
			
			String strata = Activator.getDefault().getPreferenceStore().getString("SOCOS_STRATEGY");
			if (strata instanceof String && ((String)strata).trim().length()>0)
				pb.environment().put("SOCOS_STRATEGY",strata);
			pb.environment().put("SOCOS_PROCESS_INFO","1");
			pb.redirectErrorStream(true);
			
			pid = -1;
			shell = pb.start();
			
			return new ContinuousCharStream(shell.getInputStream());
		}
		catch (IOException e)
		{
			summary = e.getLocalizedMessage();
			return null;
		}
	}

	
	@Override
	protected void handleSummary( HashMap msg )
	{
		super.handleSummary(msg);
		if (msg.containsKey("pid"))
		{
			pid = (Integer)msg.get("pid");
		}
		
	}
	
	@Override
	protected void killChecker() 
	{
		if (pid!=-1)
		{
			try {
				Process killer = Runtime.getRuntime().exec("kill -s INT "+pid);
				killer.waitFor();
			}
			catch (IOException e){
				e.printStackTrace();
			}
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		else
			shell.destroy();
	}
}
