package tests.gui;

import ibpe.EditorRequestConstants;

import org.eclipse.gef.commands.CommandStack;

public class UndoRedoTest extends AbstractTest {
	
	public void testUndoRedoProcCreation() {
		CommandStack commandStack = moduleEditPart.getViewer().getEditDomain().getCommandStack();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		assertTrue(editPartExistsAssertion("module.default"));
		
		if(commandStack.canUndo()) commandStack.undo();
		assertTrue(editPartNotExistsAssertion("module.default"));
		
		if(commandStack.canRedo()) commandStack.redo();
		assertTrue(editPartExistsAssertion("module.default"));
		
		if(commandStack.canUndo()) commandStack.undo();
		assertTrue(editPartNotExistsAssertion("module.default"));
		
		if(commandStack.canUndo()) commandStack.undo();
		assertTrue(editPartNotExistsAssertion("module.default"));
		
		if(commandStack.canRedo()) commandStack.redo();
		assertTrue(editPartExistsAssertion("module.default"));
	}
	
	public void testUndoRedoProcSitCreation() {
		CommandStack commandStack = moduleEditPart.getViewer().getEditDomain().getCommandStack();
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		assertTrue(editPartExistsAssertion("module.default"));
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 findEditPart("module.default"));
		assertTrue(editPartExistsAssertion("module.default"));
		assertTrue(editPartExistsAssertion("module.default.BoxContainer.Unique name"));
		
		if(commandStack.canUndo()) commandStack.undo();
		assertTrue(editPartExistsAssertion("module.default"));
		assertTrue(editPartNotExistsAssertion("module.default.BoxContainer.Unique name"));
		
		if(commandStack.canRedo()) commandStack.redo();
		assertTrue(editPartExistsAssertion("module.default"));
		assertTrue(editPartExistsAssertion("module.default.BoxContainer.Unique name"));
		
		if(commandStack.canUndo()) commandStack.undo();
		assertTrue(editPartExistsAssertion("module.default"));
		assertTrue(editPartNotExistsAssertion("module.default.BoxContainer.Unique name"));
		
		if(commandStack.canUndo()) commandStack.undo();
		assertTrue(editPartNotExistsAssertion("module.default"));
		assertTrue(editPartNotExistsAssertion("module.default.BoxContainer.Unique name"));
		
		if(commandStack.canRedo()) commandStack.redo();
		assertTrue(editPartExistsAssertion("module.default"));
		assertTrue(editPartNotExistsAssertion("module.default.BoxContainer.Unique name"));
		
		if(commandStack.canRedo()) commandStack.redo();
		assertTrue(editPartExistsAssertion("module.default"));
		assertTrue(editPartExistsAssertion("module.default.BoxContainer.Unique name"));
	}
	
}
