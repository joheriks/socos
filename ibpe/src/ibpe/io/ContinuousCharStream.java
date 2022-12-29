package ibpe.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.antlr.runtime.*;

public class ContinuousCharStream implements CharStream
{
	protected InputStreamReader in;
	protected StringBuffer readSoFar;
	protected int index;
	protected int line,pos;
	
	public ContinuousCharStream( InputStream s ) throws IOException 
	{
		readSoFar = new StringBuffer(4096);
		index = 0;
		line = pos = 0;
		assert s!=null;
		in = new InputStreamReader(s,"utf-8");
	}
	
	public int LA( int i )
	{
		assert i==1;
		if (i==0) return 0;
		if (i<0)
		{
			i++;
			if ( (index+i-1) < 0 ) 
				return CharStream.EOF;
		}	
		 
		int c = 0;
			 
		while ( c!=-1 && index+i-1 >= readSoFar.length() ) 
		{
				
			try {
				if (in.ready())
				{
					c = in.read();
					if (c!=-1)
						readSoFar.append((char)c);
				}
				else
					Thread.sleep(5);
			} 
			catch (IOException e) {
				c = -1;
			}
			catch (InterruptedException e) {
				c = -1;
		}
		 }
		 if (c==-1)
			 return CharStream.EOF;
		 return readSoFar.charAt(index+i-1);
	}
	
	
	public int LT( int i )
	{
		return LA(i);
	}

	public int getCharPositionInLine() 
	{
		return pos;
	}

	public int getLine() 
	{
		return line;
	}

	public void setCharPositionInLine( int p ) 
	{
		pos = p;
	}

	public void setLine( int l ) 
	{
		line = l;
		
	}

	public String substring( int start, int end ) 
	{
		if (start<readSoFar.length() && end<readSoFar.length())
			return readSoFar.substring(start,end+1);
		else
			return "";
	}

	public void consume() 
	{
		char c = readSoFar.charAt(index);
		if (c=='\n')
		{
			line += 1;
			pos = 0;
		}
		else
		{
			pos += 1;
		}
		index++;

	}

	public String getSourceName() 
	{
		return null;
	}

	public int index() 
	{
		return index;
	}

	public int mark() 
	{
		throw new RuntimeException("Not implemented");
	}

	public void release(int arg0) 
	{
		throw new RuntimeException("Not implemented");
		
	}

	public void rewind() 
	{
		throw new RuntimeException("Not implemented");
		
	}

	public void rewind(int arg0) 
	{
		throw new RuntimeException("Not implemented");
		
	}

	public void seek( int to ) 
	{
		if ( to<=index ) 
		{
        	  index = to;
        	  return;
		}
        while ( index<to ) 
        {
        	  consume();
        }
	}

	public int size() 
	{
		return Integer.MAX_VALUE;
	}

}
