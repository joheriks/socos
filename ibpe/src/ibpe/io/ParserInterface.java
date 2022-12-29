package ibpe.io;

import ibpe.model.Context;
import java.io.IOException;
import java.util.*;

import org.antlr.runtime.*;


public class ParserInterface implements IErrorReporter 
{ 
	ArrayList<String> errors = new ArrayList<String>();

	
	public void reportError(String msg)
	{
		errors.add(msg);
	}
	
	public void reportError( int line, int col, String msg )
	{
		reportError(line+":"+col+": "+msg);
	}
	
	public ArrayList<String> getErrors() 
	{
		return errors;
	}
		
	private IBPParser newParser(CharStream input)
	{
		IBPLexer lexer = new IBPLexer(input);
		lexer.setErrorReporter(this);
		IBPParser parser = new IBPParser(new CommonTokenStream(lexer));
		parser.setErrorReporter(this);
		return parser;
	}
	
	private IBPParser newParser(String s)
	{
		return newParser(new ANTLRStringStream(s));
	}
		
	private boolean parsedOk( IBPParser parser )
	{
		return parser.getNumberOfSyntaxErrors()==0;
		//return parser.getNumberOfSyntaxErrors()==0 &&  
	     //      parser.getTokenStream().index()==parser.getTokenStream().size();
		
	}
	
	public Context fromFile(String inputFileName) throws IOException 
	{
		return parseAsContext(new ANTLRFileStream(inputFileName));
	}
 
	public Context parseAsContext(CharStream input) 
	{
    	try 
    	{
    		IBPParser parser = newParser(input);
    		IBPParser.context_return r = parser.context();
			if (!parsedOk(parser)) return null; 
			else return r.elem;
    	}
    	catch (RecognitionException e) { 
    		return null;
    	}
	}
	
	public String parseAsName(String input) 
	{
		
		if (input.trim().equals("")) return null;
    	try 
    	{
    		IBPParser parser = newParser(input);
    		IBPParser.name_return r = parser.name();
			if (!parsedOk(parser)) return null; 
			else return r.start.getText();
    	}
    	catch (RecognitionException e) { return null; }
	}
	
	public String parseAsTextRow(String input) 
	{
		if (input.trim().equals("")) return "";
    	try 
    	{
    		IBPParser parser = newParser(input);
			IBPParser.textrow_return r = parser.textrow();
			if (!parsedOk(parser)) return null; 
			else return r.start.getText();
    	}
    	catch (RecognitionException e) { return null; }
	}
	
	public Fragment<?> parseAsFragment( String input )
	{
		if (input.trim().equals("")) return null;

		// Try parsing as different kinds of fragment. Order must be from
		// most general to least general type.
		try 
    	{
			IBPParser parser = newParser(input);
			IBPParser.diagram_fragment_return r1 = parser.diagram_fragment();
			if (parsedOk(parser) && r1.frag!=null) return r1.frag; 
    	}
    	catch (RecognitionException e) {}
		try 
    	{
			IBPParser parser = newParser(input);
			IBPParser.context_contents_fragment_return r2 = parser.context_contents_fragment();
			if (parsedOk(parser) && r2.frag!=null) return r2.frag; 
    	}
		catch (RecognitionException e) {}
		return null;

	}
	
}

