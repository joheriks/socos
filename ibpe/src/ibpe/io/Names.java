package ibpe.io;

import java.util.*;
import java.util.regex.*;

public class Names 
{
	/**
	 * A class for inventing new names.
	 */
	
	public static List<String> rename( List<String> check, Set<String> namespace )
	{
		HashSet<String> reserved = new HashSet<String>();
		List<String> namemap = new ArrayList<String>(); 
		
		for (String s : namespace)
			reserved.add(s);
		
		for (String s : check)
		{
			if (reserved.contains(s)) 
			{
				String stem = getNameStem(s);
				int index = getNameIndex(s);
				for (String r : reserved)
					if (getNameStem(r).equals(stem))
						index = Math.max(index,getNameIndex(r));
				index += 1;
				String newname = stem + index;
				assert !reserved.contains(newname);
				reserved.add(newname);
				namemap.add(newname);
			}
			else
			{
				reserved.add(s);
				namemap.add(null);
			}
		}
		
		return namemap;
	}

	
	// TODO: optimize this class by building a cache from strings to parsed
	// stem-index pairs.
	
	static final Pattern NAME = Pattern.compile("^([A-Za-z0-9]+?)([1-9][0-9]*)?$");
	

	private static int getNameIndex( String name )
	{
		Matcher m = NAME.matcher(name);
		return (!m.matches() || m.group(2)==null) ? 0 : Integer.parseInt(m.group(2));
	}
	
	
	private static String getNameStem( String name )
	{
		Matcher m = NAME.matcher(name);
		return m.matches() ? m.group(1) : "post"; // for clash with anon postconditions
	}
	
}
