package tests.gui;

import ibpe.EditorRequestConstants;

public class CreateElementTest extends AbstractTest {
	
	public void testElementCreationInModule() {
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
		 		 findEditPart("module"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
		
	}
	
	public void testElementCreationInModulePlaceholder() {
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
		 		 findEditPart("module.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				 findEditPart("module.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_IF, 
				 findEditPart("module.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				 findEditPart("module.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				 findEditPart("module.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				 findEditPart("module.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				 findEditPart("module.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				 findEditPart("module.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testElementCreationInProcedure() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
		 		 	  findEditPart("module.proc1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Call0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
					  findEditPart("module.proc1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Choice0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
					  findEditPart("module.proc1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.If0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
					  findEditPart("module.proc1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
					  findEditPart("module.proc1"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Unique name"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
					  findEditPart("module.proc1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
					  findEditPart("module.proc1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testElementCreationInProcedureHeaderContainer() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
		 		 	  findEditPart("module.proc1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
	}
	
	public void testElementCreationInProcedureInvariantContainer() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
		 		 	  findEditPart("module.proc1.InvariantContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.InvariantContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.InvariantContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.InvariantContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.InvariantContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.InvariantContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.InvariantContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.InvariantContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
	}
	
	public void testElementCreationInProcedurePreBox() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
				findEditPart("module.proc1.InvariantContainer.Predefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Call0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.InvariantContainer.Predefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Choice0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.InvariantContainer.Predefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.If0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.InvariantContainer.Predefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.InvariantContainer.Predefault"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.InvariantContainer.Predefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Unique name"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.InvariantContainer.Predefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Predefault.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.InvariantContainer.Predefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testElementCreationInProcedurePostBox() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
				findEditPart("module.proc1.InvariantContainer.Postdefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Call0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.InvariantContainer.Postdefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Choice0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.InvariantContainer.Postdefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.If0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.InvariantContainer.Postdefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.InvariantContainer.Postdefault"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.InvariantContainer.Postdefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Unique name"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.InvariantContainer.Postdefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.InvariantContainer.Postdefault"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testElementCreationInProcedurePreBoxPlaceholder() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
				findEditPart("module.proc1.InvariantContainer.Predefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Call0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.InvariantContainer.Predefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Choice0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.InvariantContainer.Predefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.If0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.InvariantContainer.Predefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.InvariantContainer.Predefault.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.InvariantContainer.Predefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Unique name"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.InvariantContainer.Predefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Predefault.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.InvariantContainer.Predefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testElementCreationInProcedurePostBoxPlaceholder() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
				findEditPart("module.proc1.InvariantContainer.Postdefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Call0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.InvariantContainer.Postdefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Choice0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.InvariantContainer.Postdefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.If0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.InvariantContainer.Postdefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.InvariantContainer.Postdefault.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.InvariantContainer.Postdefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Unique name"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.InvariantContainer.Postdefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.InvariantContainer.Postdefault.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testElementCreationInProcedurePlaceholder() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
				findEditPart("module.proc1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Call0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Choice0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.If0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.TextContainer.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Unique name"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testElementCreationInProcedureBoxContainer() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
				findEditPart("module.proc1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Call0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Choice0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.If0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.BoxContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.Unique name"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.TextContainer.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}

	public void testElementCreationInSituation() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
		 		 	  findEditPart("module.proc1.BoxContainer.sit1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.Call0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
					  findEditPart("module.proc1.BoxContainer.sit1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.Choice0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
					  findEditPart("module.proc1.BoxContainer.sit1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.If0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
					  findEditPart("module.proc1.BoxContainer.sit1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
					  findEditPart("module.proc1.BoxContainer.sit1"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
					  findEditPart("module.proc1.BoxContainer.sit1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
					  findEditPart("module.proc1.BoxContainer.sit1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.TextContainer.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
					  findEditPart("module.proc1.BoxContainer.sit1"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testElementCreationInSituationHeaderContainer() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
	renameEditPart(findEditPart("module.default"), "proc1");
	
	createElement(EditorRequestConstants.NEW_SITUATION, 
				  findEditPart("module.proc1"));
	renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
		 		 	  findEditPart("module.proc1.BoxContainer.sit1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.BoxContainer.sit1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.BoxContainer.sit1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.BoxContainer.sit1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.BoxContainer.sit1.HeaderContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
	}
	
	public void testElementCreationInSituationPlaceholder() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
	renameEditPart(findEditPart("module.default"), "proc1");
	
	createElement(EditorRequestConstants.NEW_SITUATION, 
				  findEditPart("module.proc1"));
	renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
				findEditPart("module.proc1.BoxContainer.sit1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.Call0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.BoxContainer.sit1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.Choice0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.BoxContainer.sit1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.If0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.TextContainer.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.Unique name"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.BoxContainer.sit1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.TextContainer.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.BoxContainer.sit1.TextContainer.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testElementCreationInSituationBoxContainer() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  findEditPart("module.Placeholder"));
	renameEditPart(findEditPart("module.default"), "proc1");
	
	createElement(EditorRequestConstants.NEW_SITUATION, 
				  findEditPart("module.proc1"));
	renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
				findEditPart("module.proc1.BoxContainer.sit1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.Call0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.BoxContainer.sit1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.Choice0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.BoxContainer.sit1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.If0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.Postdefault"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.BoxContainer"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.Unique name"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.BoxContainer.sit1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.TextContainer.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.BoxContainer.sit1.BoxContainer"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testElementCreationInTransitionLabel() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
	 		 	  	  findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
	
		createElement(EditorRequestConstants.NEW_SITUATION, 
				  	  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
			  	  findEditPart("module.proc1"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit2");
		
		createConnection(findEditPart("module.proc1.BoxContainer.sit1"), findEditPart("module.proc1.BoxContainer.sit2"));
		
		int numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CALL, 
				findEditPart("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_CHOICE, 
				findEditPart("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_IF, 
				findEditPart("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_POSTSITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PRESITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				findEditPart("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_LEAF, 
				findEditPart("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		assertTrue(numOfEP != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.TextRow0"));
		
		numOfEP = NumberOfEditParts();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
				findEditPart("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder"));
		assertTrue(numOfEP == NumberOfEditParts());
	}

}
