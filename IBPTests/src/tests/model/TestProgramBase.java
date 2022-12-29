package tests.model;

import ibpe.IBPparser.IBP2ModelReader;
import ibpe.model.Branch;
import ibpe.model.Element;
import ibpe.model.If;
import ibpe.model.Module;
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
	protected Module root;
	
	@Before
	public void setUp() {
		root = IBP2ModelReader.fromString(getProgram());
		
	}
	
	public Element element( String name ) {
		ArrayList<Element> stack = new ArrayList<Element>();
		stack.add(root);
		while (!stack.isEmpty()) {
			Element e = stack.get(0);
			stack.remove(0);
			if (e.getName()!=null && e.getName().equals(name))
				return e;
			if (e instanceof Node)
				stack.addAll(((Node)e).getStatementArray());
							
		}
		return null;
	}

	public Module module( String name ) { return (Module)element(name); }
	public Procedure procedure( String name ) { return (Procedure)element(name); }
	public Situation situation( String name ) { return (Situation)element(name); }
	public Branch branch( String name ) { return (If)element(name); }
	public TextRow leaf( String name ) { return (TextRow)element(name); }
	
	protected abstract String getProgram();

}
