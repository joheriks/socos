package ibpe.unittests.GUITests;

public class ReparentTest extends AbstractTest {
	/*
	public void testSitInProcToOtherProcReparent()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1"));
		reparentEditPart("module.proc1.BoxContainer.sit1",
						 "module.proc2.BoxContainer");
		assertTrue(editPartExistsAssertion("module.proc2.BoxContainer.sit1"));
		assertFalse(editPartExistsAssertion("module.proc1.BoxContainer.sit1"));
	}
	
	public void testSitInSitToProcReparent()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer.sit1.BoxContainer");
		
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.sit2"));
		
		reparentEditPart("module.proc1.BoxContainer.sit1.BoxContainer.sit2",
						 "module.proc1.BoxContainer");
		
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1"));
		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit2"));
		assertFalse(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.sit2"));
	}
	
	
	public void testSitInSitToOtherProcReparent()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION,
					  "module.proc1.BoxContainer.sit1.BoxContainer");

		assertTrue(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.sit2"));
		
		reparentEditPart("module.proc1.BoxContainer.sit1",
						 "module.proc2.BoxContainer");
		
		assertTrue(editPartExistsAssertion("module.proc2.BoxContainer.sit1.BoxContainer.sit2"));
		assertFalse(editPartExistsAssertion("module.proc1.BoxContainer.sit1.BoxContainer.sit2"));
	}
	*/
}
