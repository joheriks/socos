package ibpe.unittests.GUITests;

import ibpe.model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.eclipse.gef.EditPart;

public class CreateDeleteElementTest extends AbstractTest {
	/*
	String[] createWhatArray = {EditorRequestConstants.NEW_CALL,
								EditorRequestConstants.NEW_CHOICE,
								EditorRequestConstants.NEW_IF,
								EditorRequestConstants.NEW_POSTSITUATION,
								EditorRequestConstants.NEW_SITUATION,
								EditorRequestConstants.NEW_LEAF,
								EditorRequestConstants.NEW_PROCEDURE};
	
	public void testCreationAndDeletion0() {
		createAndDelete("module");
	}
	public void testCreationAndDeletion1() {
		createAndDelete("module.Placeholder");
	}
	public void testCreationAndDeletion2()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createAndDelete("module.proc1");
	}
	public void testCreationAndDeletion3()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createAndDelete("module.proc1.HeaderContainer");
	}
	public void testCreationAndDeletion4()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createAndDelete("module.proc1.InvariantContainer");
	}
	public void testCreationAndDeletion5()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createAndDelete("module.proc1.InvariantContainer.pre1");
	}
	public void testCreationAndDeletion6()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createAndDelete("module.proc1.InvariantContainer.post1");
	}
	public void testCreationAndDeletion7()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createAndDelete("module.proc1.InvariantContainer.pre1.Placeholder");
	}
	public void testCreationAndDeletion8()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createAndDelete("module.proc1.InvariantContainer.post1.Placeholder");
	}
	public void testCreationAndDeletion9()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createAndDelete("module.proc1.TextContainer.Placeholder");
	}
	public void testCreationAndDeletion10()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createAndDelete("module.proc1.BoxContainer");
	}
	public void testCreationAndDeletion11()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createAndDelete("module.proc1.BoxContainer.sit1");
	}
	public void testCreationAndDeletion12()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createAndDelete("module.proc1.BoxContainer.sit1.HeaderContainer");
	}
	public void testCreationAndDeletion13()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createAndDelete("module.proc1.BoxContainer.sit1.TextContainer.Placeholder");
	}
	public void testCreationAndDeletion14()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createAndDelete("module.proc1.BoxContainer.sit1.BoxContainer");
	}
	public void testCreationAndDeletion15()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1");
		
		createConnection("module.proc1.BoxContainer.sit1", 
						 "module.proc1.BoxContainer.sit2");
		
		createAndDelete("module.proc1.BoxContainer.sit1.Transition0.TransitionLabel.Placeholder");
	}
	
	public void createAndDelete(String createInStr)
	{
		EditPart createIn = null;
		int numOfEP;
		
		for(String createWhat : createWhatArray)
		{
			createIn = findEditPart(createInStr);
			numOfEP = NumberOfEditParts();
			
			List<String> allElementsOld = gatherElementsOfType(createWhat);
			
			createElement(createWhat, createIn);
			
			List<String> allElementsNew = gatherElementsOfType(createWhat);
			allElementsNew.removeAll(allElementsOld);
			
			boolean createValid = determineCreateValidity(createWhat, createIn);
			
			if(createValid)
			{
				assertTrue(numOfEP != NumberOfEditParts());
				
				String createdElementLocation = allElementsNew.get(0);

				assertTrue(editPartExistsAssertion(createdElementLocation));
				
				testCreationUndoRedo(createWhat, createdElementLocation);
				
				testDelete(createWhat, createdElementLocation);
				
				testDeletionUndoRedo(createWhat, createdElementLocation);
			}
			else
				assertTrue(numOfEP == NumberOfEditParts());
		}
	}
	
	public List<String> gatherElementsOfType(String createWhat)
	{
		Class<? extends Element> type = null;
		if		(createWhat.equals(EditorRequestConstants.NEW_CALL)) 			type = Call.class;
		else if	(createWhat.equals(EditorRequestConstants.NEW_CHOICE)) 			type = Choice.class;
		else if	(createWhat.equals(EditorRequestConstants.NEW_IF)) 				type = If.class;
		else if	(createWhat.equals(EditorRequestConstants.NEW_LEAF)) 			type = TextRow.class;
		else if	(createWhat.equals(EditorRequestConstants.NEW_POSTSITUATION)) 	type = PostSituation.class;
		else if	(createWhat.equals(EditorRequestConstants.NEW_PRESITUATION)) 	type = PreSituation.class;
		else if	(createWhat.equals(EditorRequestConstants.NEW_PROCEDURE)) 		type = Procedure.class;
		else if	(createWhat.equals(EditorRequestConstants.NEW_SITUATION)) 		type = Situation.class;
		else return null;
		
		List<String> nameList = new ArrayList<String>();
		Queue<Element> children = new LinkedList<Element>();
		children.addAll(((Node)moduleEditPart.getModel()).getChildren());
		
		while (!children.isEmpty())
		{
			Element e = children.poll();
			if(e == null) 
				continue;
			if (e instanceof Node)
			{
				children.addAll(((Node)e).getChildren());
				if(e instanceof BoxElement)
					children.addAll(((BoxElement)e).getSourceTransitions());
			}
			if (type.equals(e.getClass()))
				nameList.add(e.getDebugName());
		}
		
		return nameList;
	}

	private void testDeletionUndoRedo(String createWhat, String createdElementLocation)
	{
		undo();
		assertTrue(editPartExistsAssertion(createdElementLocation));
		redo();
		assertFalse(editPartExistsAssertion(createdElementLocation));
		
	}
	private void testDelete(String createWhat, String createdElementLocation)
	{
		int numOfEP = NumberOfEditParts();
		deleteElement(createdElementLocation);
		assertTrue(numOfEP != NumberOfEditParts());
		assertFalse(editPartExistsAssertion(createdElementLocation));
	}
	public void testCreationUndoRedo(String createWhat, String createdElementLocation)
	{
		undo();
		assertFalse(editPartExistsAssertion(createdElementLocation));
		redo();
		assertTrue(editPartExistsAssertion(createdElementLocation));
	}
	
	public boolean determineCreateValidity(String createWhat, EditPart createIn)
	{
		Element model = (Element) createIn.getModel();
		boolean createValid = true;
		
		if(createWhat.equals(EditorRequestConstants.NEW_PRESITUATION))
			createValid = false;
		else if(createWhat.equals(EditorRequestConstants.NEW_CALL) ||
			    createWhat.equals(EditorRequestConstants.NEW_CHOICE) ||
			    createWhat.equals(EditorRequestConstants.NEW_IF) ||
			    createWhat.equals(EditorRequestConstants.NEW_POSTSITUATION) ||
			    createWhat.equals(EditorRequestConstants.NEW_SITUATION))
		{
			if (model instanceof Context) 
			{
				createValid = false;
			}
		}
		else if(createWhat.equals(EditorRequestConstants.NEW_PROCEDURE))
		{

			   if (model.getParent() instanceof TransitionLabel)
			{
				createValid = false;
			}
		}
		
		return createValid;
	}
	*/

}