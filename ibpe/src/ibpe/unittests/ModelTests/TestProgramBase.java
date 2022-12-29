package ibpe.unittests.ModelTests;

import ibpe.io.ParserInterface;
import ibpe.model.Fork;
import ibpe.model.Element;
import ibpe.model.Context;
import ibpe.model.Node;
import ibpe.model.Procedure;
import ibpe.model.Situation;
import ibpe.model.TextRow;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Before;

/* A base class for testing model properties. The model is parsed from the program string returned by
 * getProgram().
 */

public abstract class TestProgramBase extends TestCase {

	protected String program;
	protected Context root;
	
	@Before
	public void setUp() {
		//root = (new IBPReader()).fromString(getProgram());
		
	}
	
	public Element element( String name ) {
		ArrayList<Element> stack = new ArrayList<Element>();
		stack.add(root);
		while (!stack.isEmpty()) {
			Element e = stack.get(0);
			stack.remove(0);
			if (e.getText()!=null && e.getText().equals(name))
				return e;
			if (e instanceof Node)
				stack.addAll(((Node)e).getChildren());
							
		}
		return null;
	}

	public Context module( String name ) { return (Context)element(name); }
	public Procedure procedure( String name ) { return (Procedure)element(name); }
	public Situation situation( String name ) { return (Situation)element(name); }
	public TextRow leaf( String name ) { return (TextRow)element(name); }
	
	protected abstract String getProgram();

}
