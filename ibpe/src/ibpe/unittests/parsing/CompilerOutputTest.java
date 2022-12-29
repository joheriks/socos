package ibpe.unittests.parsing;

import ibpe.model.Element;
import ibpe.model.Context;
import ibpe.model.Precondition;
import ibpe.model.Procedure;
import ibpe.model.Situation;
import ibpe.model.TextRow;
import ibpe.unittests.GUITests.AbstractTest;

import org.eclipse.gef.EditPart;

public class CompilerOutputTest extends AbstractTest {
	
	/*
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		createProject("MyProject");
		createAndOpenFile("test.ibp", "");
		
		createElement(EditorRequestConstants.NEW_LEAF, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_LEAF, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
	
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createConnection("module.proc1.BoxContainer.sit1", 
						 "module.proc1.BoxContainer.sit2");
		
		createElement(EditorRequestConstants.NEW_LEAF, "module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder");
		createElement(EditorRequestConstants.NEW_LEAF, "module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder");
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test1() {
		EditPart ep = CompilerOutputReader.fromString("module");
		assertTrue(ep.getModel() instanceof Context);
		assertTrue(((Element)ep.getModel()).getText().equals("module"));
	}
	
	public void test2() {
		EditPart ep = CompilerOutputReader.fromString("module:Placeholder");
		//assertTrue(ep.getModel() instanceof Placeholder);
		assertTrue(ep.getParent().getModel() instanceof Context);
	}
	
	public void test3() {
		EditPart ep = CompilerOutputReader.fromString("module:HeaderContainer");
		//assertTrue(ep.getModel() instanceof HeaderContainer);
		assertTrue(ep.getParent().getModel() instanceof Context);
	}
	
	public void test4() {
		EditPart ep = CompilerOutputReader.fromString("module#0");
		assertTrue(ep.getModel() instanceof TextRow);
		assertTrue(ep.getParent().getModel() instanceof Context);
	}
	
	public void test5() {
		EditPart ep = CompilerOutputReader.fromString("module#1");
		assertTrue(ep.getModel() instanceof TextRow);
		assertTrue(ep.getParent().getModel() instanceof Context);
	}
	
	public void test6() {
		EditPart ep = CompilerOutputReader.fromString("module.proc1.sit1");
		assertTrue(ep.getModel() instanceof Situation);
		assertTrue(ep.getParent().getParent().getModel() instanceof Procedure);
		assertTrue(ep.getParent().getParent().getParent().getModel() instanceof Context);
	}
	
	public void test7() {
		EditPart ep = CompilerOutputReader.fromString("module.sit1");
		assertTrue(ep.getModel() instanceof Situation);
		assertTrue(ep.getParent().getParent().getModel() instanceof Procedure);
		assertTrue(ep.getParent().getParent().getParent().getModel() instanceof Context);
	}
	
	public void test8() {
		EditPart ep = CompilerOutputReader.fromString("module.pre1");
		assertTrue(ep.getModel() instanceof PreSituation);
	}
	
	public void test9() {
		EditPart ep = CompilerOutputReader.fromString("module.sit1~0#0");
		assertTrue(ep.getModel() instanceof TextRow);
	}
	
	public void test10() {
		EditPart ep = CompilerOutputReader.fromString("module.sit1~0#1");
		assertTrue(ep.getModel() instanceof TextRow);
	}

	*/
}
