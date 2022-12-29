package ibpe.unittests.GUITests;


public class CreateTransitionTest extends AbstractTest {
	
	/*
	public void testTransitionCreationProcToProc()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1", 
						 "module.proc2");
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationProcToSit()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1", 
				 		 "module.proc1.BoxContainer.sit1");
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationSitToSit()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.BoxContainer.sit1", 
				 		 "module.proc1.BoxContainer.sit2");
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0"));
	}
	
	public void testTransitionCreationSitToProc()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.BoxContainer.sit1", 
				 		 "module.proc1");
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationSitToPre()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.BoxContainer.sit1", 
				 		 "module.proc1.InvariantContainer.pre1");
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationPreToSit()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.InvariantContainer.pre1", 
				 		 "module.proc1.BoxContainer.sit1");
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.pre1.Transition0"));
	}
	
	
	public void testTransitionCreationPreToPost()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.InvariantContainer.pre1", 
						 "module.proc1.InvariantContainer.post1");
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.InvariantContainer.pre1.Transition0"));
	}
	
	public void testTransitionCreationSitToPost()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.BoxContainer.sit1", 
				 		 "module.proc1.InvariantContainer.post1");
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0"));
	}
	
	public void testTransitionCreationPostToSit()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.InvariantContainer.post1", 
				 		 "module.proc1.BoxContainer.sit1");
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationPostToPre()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.InvariantContainer.post1", 
				 		 "module.proc1.InvariantContainer.pre1");
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationSitToSitInOtherProc()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc2");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.BoxContainer.sit1", 
						 "module.proc2.BoxContainer.sit1");
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationPreToPre()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.InvariantContainer.pre1", 
				 		 "module.proc1.InvariantContainer.pre1");
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationPostToPost()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.InvariantContainer.post1", 
				 		 "module.proc1.InvariantContainer.post1");
		
		assertTrue(numOfEditParts == NumberOfEditParts());
	}
	
	public void testTransitionCreationSitToIf()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createElement(EditorRequestConstants.NEW_IF, "module.proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.BoxContainer.sit1", 
				 		 "module.proc1.BoxContainer.If0");
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0"));
	}
	
	public void testTransitionCreationSitToChoice()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createElement(EditorRequestConstants.NEW_CHOICE, "module.proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.BoxContainer.sit1", 
						 "module.proc1.BoxContainer.Choice0");
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0"));
	}
	
	public void testTransitionCreationSitToCall()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createElement(EditorRequestConstants.NEW_CALL, "module.proc1");
		
		int numOfEditParts = NumberOfEditParts();
		
		createConnection("module.proc1.BoxContainer.sit1", 
						 "module.proc1.BoxContainer.Call0");
		
		assertTrue(numOfEditParts != NumberOfEditParts());
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.Transition0"));
	}
	*/
}
