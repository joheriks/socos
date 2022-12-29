package ibpe.io;

import ibpe.model.*;

import org.eclipse.draw2d.geometry.*;
import java.util.*;


public class Serializer
{
	protected static final int spacesPerIndent = 4;
	protected StringBuffer line;
	protected StringBuffer buffer;
	protected int indentLevel;
	
	protected ArrayList<Element> lineToElem;
	protected Stack<Element> elementStack;
	
	public Serializer( StringBuffer buf )
	{
		buffer = buf;
		lineToElem = new ArrayList<Element>();
		elementStack = new Stack<Element>();
		line = new StringBuffer();
	}
	
	public Element lineToElement( int idx )
	{
		assert 0<=idx && idx<getLineCount();
		return lineToElem.get(idx);		
	}
	
	public int getLineCount()
	{
		return lineToElem.size();
	}
	
	public void serialize( Context c ) 
	{
		elementStack.push(c);
		out(c.getText()); out(" : CONTEXT BEGIN"); nl();
		indentMore();
		serialize_context_contents(c.getChildren());
		indentLess();
		out("END"); sp(); out(c.getText()); nl();
		elementStack.pop();
	};
	
	private void serialize_context_contents( List<Element> contents )
	{
		for (Element e : contents)
		{
			if (e instanceof Procedure) serialize((Procedure)e);
			else if (e instanceof TextRow) serialize((TextRow)e);
			else assert false;
		}
	}
	
	public void serialize( Procedure p )
	{
		elementStack.push(p);
		out(p.getText());
		if (!p.getSignature().isEmpty())
		{
			out("["); nl();
			indentMore();
			for (TextRow t : p.getSignature())
			{ 
				elementStack.push(t);
				serialize(t);
				elementStack.pop();
			}
			indentLess();
			out("]");
		}
		outln(" : PROCEDURE");
		if (p.getPrecondition()!=null) serialize(p.getPrecondition()); 
		for (Postcondition e : p.getPostconditions()) serialize(e);
		if (p.getVariant()!=null) 
		{	
			elementStack.push(p.getVariant());
			out("**"); sp(); serialize(p.getVariant());
			elementStack.pop();
		}
		outln("BEGIN");
		indentMore();
		for (TextRow e : p.getDeclarations()) 
		{ 
			elementStack.push(e);
			serialize(e);
			elementStack.pop();
		}
		for (Situation s: p.getToplevelSituations()) serialize(s);
		if (p.getPrecondition()!=null && p.getPrecondition().getOutgoingTransitions().size()>0)
			serialize_outgoing_transitions(p.getPrecondition());
		
		indentLess();
		out("END"); sp(); out(p.getText()); nl();
		elementStack.pop();
	}
	
	private void serialize_outgoing_transitions( GraphNode tn )
	{
		elementStack.push(tn);
	
	// 	if (tn instanceof MultiCall) out("CALL");
	// 	else 
		out(tn.getChoiceType());
		
		sp();
		if (tn instanceof IFChoice) { out("%:"); sp(); serialize(tn.getPosition()); }
		else if (tn instanceof MultiCall) { serialize((MultiCall)tn); nl(); out("%:"); sp(); serialize(tn.getPosition()); }
		nl();
		indentMore();
		for (Transition t : tn.getOutgoingTransitions()) serialize(t);
		indentLess();
		
	//	if (tn instanceof MultiCall) outln("ENDCALL");
	//	else
		outln("END"+tn.getChoiceType());
		
		elementStack.pop();
	}
	
	private void serialize( Transition t )
	{
		elementStack.push(t);
		out("%:"); sp(); 
		if (t.getSourcePoint()!=null) { serialize(t.getSourcePoint()); sp(); }
		serialize(t.getWaypoints()); sp();
		if (t.getTargetPoint()!=null) { serialize(t.getTargetPoint()); sp(); }
		nl();

		out("%:"); sp(); serialize(t.getLabelPoint()); nl();

		// TODO: get rid of the whole TextContainer class?
		TextContainer tc=(TextContainer)t.getChildren().get(0);

		for(Element e : tc.getChildren()) 
		{ 
			elementStack.push(e);			
			if(e instanceof TextRow)
			{
				serialize((TextRow)e);
			}
			else if(e instanceof Proof)
			{
				serialize((Proof)e);
			}
		/*	else if(e instanceof MultiCall)
			{
				serialize((MultiCall)e);
			} */
			elementStack.pop();
		}

		if (t.getTarget() instanceof Postcondition)
		{
			Postcondition p = (Postcondition)t.getTarget();
			out("EXIT"); 
			sp();
			if (p.isAnonymous())  {	out("post__"); }
			else { out(p.getText()); } 
			nl();
		}
		else if (t.getTarget() instanceof Situation)
		{
			out("GOTO"); sp(); out(t.getTarget().getText()); nl();
		}
		else if (t.getTarget() instanceof Fork)
			serialize_outgoing_transitions(t.getTarget());
		else
			assert false; // Can't happen
		elementStack.pop();		
	}

	private void serialize(Proof p)
	{
		elementStack.push(p);

		out("PROOF"); nl();
	
//		for(TextRow row : p.getRows()) 
//		{
//			serialize(row);
//		}

		serialize_prooftext(p.getText());

		out("ENDPROOF"); nl();

		elementStack.pop();		
	}

	private void serialize_prooftext(String str)
	{
		String buff="";

		for(int i=0; i<str.length(); i++)
		{
			if(str.charAt(i)=='\n')
			{
				out(buff+";"); nl();
				buff="";
			}
			else
			{
				buff+=str.charAt(i);
			}
		}

		if(buff.length()>0)
		{
			out(buff+";"); nl();
		}
	}

	public void serialize( Precondition p )
	{
		elementStack.push(p);
		out("PRE BEGIN"); sp(); out("%:"); sp(); serialize(p.getBounds()); nl();
		indentMore();
		for (TextRow c : p.getConstraints()) 
		{ 
			elementStack.push(c);
			out("*"); sp(); serialize(c);
			elementStack.pop();
		}
		indentLess();
		out("END"); nl();
		elementStack.pop();
	}
	
	public void serialize( Postcondition p )
	{
		elementStack.push(p);
		if (!p.isAnonymous()) {	out(p.getText()); out(":"); } 
		out("POST BEGIN"); sp(); out("%:"); sp(); serialize(p.getBounds()); nl();
		indentMore();
		for (TextRow c : p.getConstraints()) 
		{
			elementStack.push(p);
			out("*"); sp(); serialize(c);
			elementStack.pop();
		}
		indentLess();
		out("END");	if (!p.isAnonymous()) {	sp(); out(p.getText()); }
		nl();
		elementStack.pop();
	}

	public void serialize( Situation s )
	{
		elementStack.push(s);
		out(s.getText()); out(" : SITUATION BEGIN"); sp(); out("%:"); sp(); serialize(s.getBounds()); nl();
		indentMore();
		for (TextRow c : s.getDeclaration()) 
		{ 
			elementStack.push(c);
			out("-");sp(); serialize(c);
			elementStack.pop();
		}
		for (TextRow c : s.getConstraints()) 
		{ 
			elementStack.push(c);
			out("*"); sp(); serialize(c);
			elementStack.pop();
		}
	  	if (s.getVariant()!=null) 
	  	{	
	  		elementStack.push(s.getVariant());
	  		out("**"); sp(); serialize(s.getVariant());
	  		elementStack.pop();
	  	}
		for (Situation ns : s.getNested()) serialize(ns);
		if (s.getOutgoingTransitions().size()>0) serialize_outgoing_transitions(s);
		indentLess();
		out("END"); sp(); out(s.getText()); nl();
		elementStack.pop();
	}

	public void serialize( TextRow t )
	{
		elementStack.push(t);
		out(t.getText());
		out(";"); 
		nl();
		elementStack.pop();
	}

	public void serialize( MultiCall t )
	{
		elementStack.push(t);
		out(t.getText());
		out(";");
		elementStack.pop();
	}
	
	public void serialize( Rectangle r ) 
	{
		out("["); out(r.x); sp(); out(r.y); sp(); out(r.width); sp(); out(r.height); out("]"); 
	}

	public void serialize( Point p ) 
	{
		out("["); out(p.x); sp(); out(p.y); out("]"); 
	}

	public void serialize( List<Point> pl ) 
	{
		out("["); sp();
		for (Point p : pl) { serialize(p); sp(); }
		out("]"); 
	}

	/////// FRAGMENTS ///////////////////////////////
	
	@SuppressWarnings("unchecked")
	public void serialize( Fragment<?> f ) 
	{
		if (f instanceof DiagramFragment) 
			serialize((DiagramFragment)f);
		
		else if (f instanceof SequenceFragment) 
			serialize_context_contents((List)f.getModelElements());
	}
	
	public void serialize( DiagramFragment f )
	{
		if (f.pre!=null) serialize(f.pre);
		for (Postcondition p : f.posts) serialize(p);
		for (Situation s : f.situations) serialize(s);
		if (f.pre!=null && f.pre.getOutgoingTransitions().size()>0)
			serialize_outgoing_transitions(f.pre);
	}
	
	/////// HELPERS ///////////////////////////////

	protected void outln( String s ) 
	{
		out(s); nl();
		
	}
	
	// Append string to output
	protected void out( String s )
	{
		assert !(s.contains("\n"));
		line.append(s);
	}
	
	protected void out( int s )
	{
		out(Integer.toString(s));
	}
	
	// Commit current line, newline
	protected void nl()
	{
		for (int i=0; i<spacesPerIndent*indentLevel; i++)
			buffer.append(" ");
		buffer.append(line);
		buffer.append("\n");
		line = new StringBuffer();
		
		lineToElem.add(elementStack.isEmpty() ? null : elementStack.get(elementStack.size()-1));
		
	}
	
	protected void sp()
	{
		if (line.length()>0 && line.charAt(line.length()-1)!=' ')
			line.append(" ");
	}
	
	// Increase indentation by one level
	protected void indentMore() 
	{
		indentLevel++;
	}
	
	// Decrease indentation by one level
	protected void indentLess()
	{
		assert indentLevel>0;
		indentLevel--;
	}
	
}

