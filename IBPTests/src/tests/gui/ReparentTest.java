package tests.gui;

import ibpe.EditorRequestConstants;

public class ReparentTest extends AbstractTest {
	
	public void testSitInProcToOtherProcReparent() {
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc2");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1"));
		reparentEditPart(findEditPart("module.proc1.BoxContainer.sit1"),
						 findEditPart("module.proc2.BoxContainer"));
		assertTrue(editPartExistsAssertion("module.proc2.BoxContainer.sit1"));
		assertFalse(editPartExistsAssertion("module.proc1.BoxContainer.sit1"));
	}
	
	public void testSitInSitToProcReparent() {
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				  findEditPart("module.proc1.BoxContainer.sit1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.sit1.BoxContainer.Unique name"), "sit2");
		
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.sit2"));
		
		reparentEditPart(findEditPart("module.proc1.BoxContainer.sit1.BoxContainer.sit2"),
				findEditPart("module.proc1.BoxContainer"));
		
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1"));
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit2"));
		assertFalse(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.sit2"));
	}
	
	
	public void testSitInSitToOtherProcReparent() {
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc2");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				  findEditPart("module.proc1.BoxContainer.sit1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.sit1.BoxContainer.Unique name"), "sit2");

		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.sit2"));
		
		reparentEditPart(findEditPart("module.proc1.BoxContainer.sit1"),
						 findEditPart("module.proc2.BoxContainer"));
		
		assertTrue(editPartExistsAssertion("module.proc2.BoxContainer.sit1.BoxContainer.sit2"));
		assertFalse(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.sit2"));
	}
	
}
