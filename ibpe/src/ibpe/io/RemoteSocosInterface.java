package ibpe.io;

import java.io.*;
import java.net.*;
import java.util.*;

import ibpe.*;
import ibpe.io.SocosInterface.SocosTask;

import org.antlr.runtime.CharStream;
import org.eclipse.ui.console.MessageConsoleStream;

@SuppressWarnings("rawtypes")
public class RemoteSocosInterface extends SocosInterface
{
	int session;

	public RemoteSocosInterface( SocosTask task, IBPEditor ibpEditor )
	{
		super(task,ibpEditor);
	}
	
	public String getDescription()
	{
		return "Using: "+Activator.getDefault().getPreferenceStore().getString("SOCOS_CHECKER_URL");
	}

	
	protected HttpURLConnection getHttpConnection( ) throws Exception
	{
		URL url = new URL(Activator.getDefault().getPreferenceStore().getString("SOCOS_CHECKER_URL"));
		URLConnection conn = url.openConnection();
		if (!(conn instanceof HttpURLConnection))
			throw new Exception("Wrong protocol type: "+url.getProtocol());
		HttpURLConnection http = (HttpURLConnection)conn; 
		http.setRequestMethod("POST");
		
		http.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=iso-8859-1");
		http.setRequestProperty("Content-Length","0");
		http.setRequestProperty("Accept-Charset","iso-8859-1");
		http.setUseCaches(false);
	    http.setDoInput(true);
	    http.setDoOutput(true);
	    
	    http.connect();
		return http;
		
	}
	
	
	protected void writeRequest( HttpURLConnection http, HashMap contents  ) throws IOException
	{
		DataOutputStream out = new DataOutputStream (http.getOutputStream ());
		
		
		out.writeBytes("request="+URLEncoder.encode(JSONSerializer.JSONtoString(contents),"iso-8859-1"));
		
		out.flush();
		out.close();
	}
	

	@SuppressWarnings("unchecked")
	@Override
	protected CharStream initChecker() 
	{
		try {
			// -> cmd: get_session_id
			HttpURLConnection http = getHttpConnection();
			HashMap req = new HashMap();
			req.put("cmd","get_session_id");
			writeRequest(http,req);

			// <- get session id
			ContinuousCharStream stream = new ContinuousCharStream(http.getInputStream());
			Object o = parseJSON(stream);
			if (o instanceof HashMap && 
				    ((HashMap)o).containsKey("session") &&
				    ((HashMap)o).get("session") instanceof Integer)
					session = ((Integer)((HashMap)o).get("session"));
				else
					throw new IOException("Unable to acquire session");

			// -> cmd: check, data: file contents
			http = getHttpConnection();
			String filePath = editor.getPartName();
			req = new HashMap();
			req.put("session",session);
			
			if (task==SocosTask.DEBUG)
				req.put("cmd","debug");
			else
				req.put("cmd","check");
			
			String strata = Activator.getDefault().getPreferenceStore().getString("SOCOS_STRATEGY");
			if (strata instanceof String && ((String)strata).trim().length()>0)
				req.put("strategy",strata);
			req.put("data",Utils.slurp(filePath));
			
			
			writeRequest(http,req);
		
			return new ContinuousCharStream(http.getInputStream());

		}
		catch (Exception e)
		{
			summary = "Unable to acquire session ("+e.getLocalizedMessage()+")";
			return null;
		}
	}


	@SuppressWarnings("unchecked")
	protected InputStream setupStream() 
	{
		try {
			String filePath = editor.getPartName();
			HttpURLConnection http = getHttpConnection();
			HashMap req = new HashMap();
			req.put("session",session);

			
			if (task==SocosTask.DEBUG)
				req.put("cmd","debug");
			else
				req.put("cmd","check");
			
			String strata = Activator.getDefault().getPreferenceStore().getString("SOCOS_STRATEGY");
			if (strata instanceof String && ((String)strata).trim().length()>0)
				req.put("strategy",strata);
			
			req.put("data",Utils.slurp(filePath));
			
			writeRequest(http,req);
		
			return http.getInputStream();	
		}
		catch (Exception e) {
			return null;
		}
	}
	
	@Override
	protected void handleProgress( HashMap msg )
	{
		super.handleProgress(msg);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void killChecker() 
	{
		if (session==0)
			return;
		
		try {
			HttpURLConnection http = getHttpConnection();
			HashMap req = new HashMap();
			req.put("session",session);
			req.put("cmd","kill");
			
			//System.out.println("Sending KILL command...");
			writeRequest(http,req);
			
			InputStream in = http.getInputStream();
			InputStreamReader sr = new InputStreamReader(in);
			int ch = sr.read();
			while (ch!=-1)
			{
				//System.out.print(Character.toChars(ch));
				ch = sr.read();
			}
			
		}
		catch (Exception e) {
			return;
		}

	}
		      
		      

}
