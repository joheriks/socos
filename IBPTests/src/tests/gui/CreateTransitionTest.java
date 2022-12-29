package tests.gui;

import ibpe.EditorRequestConstants;

public class CreateTransitionTest extends AbstractTest {
	
	public void testTransitionCreationProcToProc() {
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc2");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1"), 
				 findEditPart("module.proc2"));
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationProcToSit() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1"), 
				 findEditPart("module.proc1.BoxContainer.sit1"));
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationSitToSit() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit2");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), 
				 findEditPart("module.proc1.BoxContainer.sit2"));
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0"));
	}
	
	public void testTransitionCreationSitToProc() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), 
				 findEditPart("module.proc1"));
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationSitToPre() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), 
				 findEditPart("module.proc1.InvariantContainer.Predefault"));
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationPreToSit() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.InvariantContainer.Predefault"), 
				 findEditPart("module.proc1.BoxContainer.sit1"));
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Predefault.Transition0"));
	}
	
	
	public void testTransitionCreationPreToPost() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.InvariantContainer.Predefault"), 
				findEditPart("module.proc1.InvariantContainer.Postdefault"));
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Predefault.Transition0"));
	}
	
	public void testTransitionCreationSitToPost() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), 
				 findEditPart("module.proc1.InvariantContainer.Postdefault"));
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0"));
	}
	
	public void testTransitionCreationPostToSit() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.InvariantContainer.Postdefault"), 
				 findEditPart("module.proc1.BoxContainer.sit1"));
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationPostToPre() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.InvariantContainer.Postdefault"), 
				 findEditPart("module.proc1.InvariantContainer.Predefault"));
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationSitToSitInOtherProc() {
		
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
					  findEditPart("module.proc2"));
		renameEditPart(findEditPart("module.proc2.BoxContainer.Unique name"), "sit2");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), 
				 findEditPart("module.proc2.BoxContainer.sit2"));
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationPreToPre() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.InvariantContainer.Predefault"), 
				 findEditPart("module.proc1.InvariantContainer.Predefault"));
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationPostToPost() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.InvariantContainer.Postdefault"), 
				 findEditPart("module.proc1.InvariantContainer.Postdefault"));
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationSitToIf() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_IF, 
				  findEditPart("module.proc1"));
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), 
				 findEditPart("module.proc1.BoxContainer.If0"));
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0"));
	}
	
	public void testTransitionCreationSitToChoice() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				  findEditPart("module.proc1"));
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), 
				 findEditPart("module.proc1.BoxContainer.Choice0"));
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0"));
	}
	
	public void testTransitionCreationSitToCall() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_CALL, 
				  findEditPart("module.proc1"));
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), 
				 findEditPart("module.proc1.BoxContainer.Call0"));
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0"));
	}
}
