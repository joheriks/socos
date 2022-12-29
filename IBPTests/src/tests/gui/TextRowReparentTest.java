package tests.gui;

import ibpe.EditorRequestConstants;

public class TextRowReparentTest extends AbstractTest {
	
	public void testTextRowReparent() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc2");
		
		createElement(EditorRequestConstants.NEW_LEAF, findEditPart("module.proc1"));
		
		moveTextRow(findEditPart("module.proc1.TextContainer.TextRow0"),
						findEditPart("module.proc2.TextContainer.Placeholder"));
		assertTrue(editPartExistsAssertion("module.proc2.TextContainer.TextRow0"));
		assertFalse(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
	}
	
	
	public void testTextRowReparent2() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_LEAF, findEditPart("module.proc1"));
		
		moveTextRow(findEditPart("module.proc1.TextContainer.TextRow0"),
						findEditPart("module.proc1.InvariantContainer.Predefault.Placeholder"));
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Predefault.TextRow0"));
		assertFalse(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
	}
	
	public void testTextRowReparent3() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_LEAF, findEditPart("module.proc1.InvariantContainer.Postdefault.Placeholder"));
		
		moveTextRow(findEditPart("module.proc1.InvariantContainer.Postdefault.Placeholder"),
						findEditPart("module.proc1.InvariantContainer.Predefault.Placeholder"));
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault.TextRow0"));
		assertFalse(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
	}
	
	public void testTextRowReparent4() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_LEAF, findEditPart("module.proc1"));
		
		moveTextRow(findEditPart("module.proc1.TextContainer.TextRow0"),
						findEditPart("module.proc1.BoxContainer.sit1.TextContainer.Placeholder"));
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.TextContainer.TextRow0"));
		assertFalse(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
	}
	
	public void testTextRowReparent5() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit2");
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), 
				 findEditPart("module.proc1.BoxContainer.sit2"));
		
		createElement(EditorRequestConstants.NEW_LEAF, findEditPart("module.proc1"));
		
		moveTextRow(findEditPart("module.proc1.TextContainer.TextRow0"),
					findEditPart("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		assertFalse(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
	}
	
}
