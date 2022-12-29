package ibpe.io;

import java.util.*;

public class JSONSerializer 
{
	protected StringBuffer buffer;
	
	public JSONSerializer( StringBuffer buf )
	{
		buffer = buf;
	}
	
	public void serialize( Object json )
	{
		if (json==Boolean.TRUE) buffer.append("true");
		else if (json==Boolean.FALSE) buffer.append("false");
		else if (json==null) buffer.append("null");
		else if (json instanceof String) buffer.append("\""+escapeString((String)json)+"\"");
		else if (json instanceof Number) buffer.append(json.toString());
		else if (json instanceof Map)
		{
			buffer.append("{ ");
			Boolean f = false;
			for (Object  o : ((Map)json).entrySet())
			{
				if (f) buffer.append(", ");
				else f = true;
				buffer.append("\""+(String)((Map.Entry)o).getKey()+"\"");
				buffer.append(":");
				serialize(((Map.Entry)o).getValue());
			}
				
			buffer.append(" }");
		}
		else if (json instanceof List)
		{
			buffer.append("[ ");
			Boolean f = false;
			for (Object o : ((List)json))
			{
				if (f) buffer.append(", ");
				else f = true;
				serialize(o);
			}
				
			buffer.append(" ]");
		}
		else
			throw new RuntimeException("Unable to serialize "+json.toString());
	}
	
	
	private String escapeString( String s )
	{
		return s.replace("\\","\\\\")
		        .replace("\b","\\b")
		        .replace("\t","\\t")
		        .replace("\n","\\n")
		        .replace("\f","\\f")
		        .replace("\r","\\r")
		        .replace("\"","\\\"")
		        .replace("/","\\/");
		        
	}
	
	public static String JSONtoString( Object json )
	{
		StringBuffer sb = new StringBuffer();
		JSONSerializer se = new JSONSerializer(sb);
		se.serialize(json);
		return sb.toString();
	}
}
