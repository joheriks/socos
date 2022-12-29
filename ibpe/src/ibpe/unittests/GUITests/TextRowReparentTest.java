package ibpe.unittests.GUITests;

public class TextRowReparentTest extends AbstractTest {
	
	/*
	public void testTextRowReparent()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		
		createElement(EditorRequestConstants.NEW_LEAF, findEditPart("module.proc1"));
		
		moveTextRow(findEditPart("module.proc1.TextContainer.TextRow0"),
						findEditPart("module.proc2.TextContainer.Placeholder"));
		assertTrue(editPartExistsAssertion("module.proc2.TextContainer.TextRow0"));
		assertFalse(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
	}
	
	
	public void testTextRowReparent2()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		
		createElement(EditorRequestConstants.NEW_LEAF, findEditPart("module.proc1"));
		
		moveTextRow(findEditPart("module.proc1.TextContainer.TextRow0"),
						findEditPart("module.proc1.InvariantContainer.pre1.Placeholder"));
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.pre1.TextRow0"));
		assertFalse(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
	}
	
	public void testTextRowReparent3()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		
		createElement(EditorRequestConstants.NEW_LEAF, findEditPart("module.proc1.InvariantContainer.post1.Placeholder"));
		
		moveTextRow(findEditPart("module.proc1.InvariantContainer.post1.Placeholder"),
						findEditPart("module.proc1.InvariantContainer.pre1.Placeholder"));
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.post1.TextRow0"));
		assertFalse(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
	}
	
	public void testTextRowReparent4()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 findEditPart("module.proc1"));
		
		createElement(EditorRequestConstants.NEW_LEAF, findEditPart("module.proc1"));
		
		moveTextRow(findEditPart("module.proc1.TextContainer.TextRow0"),
						findEditPart("module.proc1.BoxContainer.sit1.TextContainer.Placeholder"));
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.TextContainer.TextRow0"));
		assertFalse(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
	}
	
	public void testTextRowReparent5()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 findEditPart("module.proc1"));
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 findEditPart("module.proc1"));
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), 
				 findEditPart("module.proc1.BoxContainer.sit2"));
		
		createElement(EditorRequestConstants.NEW_LEAF, findEditPart("module.proc1"));
		
		moveTextRow(findEditPart("module.proc1.TextContainer.TextRow0"),
					findEditPart("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		assertFalse(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
	}
	*/
	
}
