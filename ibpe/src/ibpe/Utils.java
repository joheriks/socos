package ibpe;

import java.io.*;
import java.util.*;

public class Utils 
{
	// Utility methods to make Java suck less. Add more as needed.

	/** Join toString() for items in collection using delimiter.
	 * Source: http://snippets.dzone.com  
	 */
	public static String join( Iterable< ? extends Object > pColl, String separator )
    {
        Iterator< ? extends Object > oIter;
        if ( pColl == null || ( !( oIter = pColl.iterator() ).hasNext() ) )
            return "";
        StringBuilder oBuilder = new StringBuilder( String.valueOf( oIter.next() ) );
        while ( oIter.hasNext() )
            oBuilder.append( separator ).append( oIter.next() );
        return oBuilder.toString();
    }
	
	/**
	 * Return a sublist of objects of assignable to type t.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> filterObjectsOfType( Collection<?> list, Class<T> t ) 
	{
		List<T> l = new ArrayList<T>();
		for (Object e : list)
			if (t.isAssignableFrom(e.getClass()))
				l.add((T)e);
		return l;
	}

	
	/** 
	 * Reads file from filePath into a string.
	 */
	public static String slurp( String filePath ) throws IOException
	{
		StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            fileData.append(buf, 0, numRead);
        }
        reader.close();
        return fileData.toString();
    }
	
}
