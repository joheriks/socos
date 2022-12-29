package ibpe.unittests.GUITests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;

public class OverlapTest extends AbstractTest {
	/*
	public void testMoveOverlap()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		moveEditPart("module.proc1.BoxContainer",
					 "module.proc1.BoxContainer.sit1",
					 new Point(0, 40));
	
		assertTrue(noIntersectionAssertion("module.proc1.BoxContainer.sit1",
										   "module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer",
				  	 "module.proc1.BoxContainer.sit2",
				  	 new Point(0, -40));
		
		assertTrue(noIntersectionAssertion("module.proc1.BoxContainer.sit1",
				   						   "module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit2",
			  	 	 new Point(220+40, 60));
		
		moveEditPart("module.proc1.BoxContainer",
				 	 "module.proc1.BoxContainer.sit1",
				 	 new Point(0, 160));
		
		assertTrue(noIntersectionAssertion("module.proc1.BoxContainer.sit1",
				   "module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer", 
					 "module.proc1.BoxContainer.sit1",
					 new Point(120, 0));
		
		assertTrue(noIntersectionAssertion("module.proc1.BoxContainer.sit1",
				   						   "module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit2",
			  	 	 new Point(-100, 0));
		
		assertTrue(noIntersectionAssertion("module.proc1.BoxContainer.sit1",
				   						   "module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit2",
			  	 	 new Point(-100, 0));
		
		assertTrue(noIntersectionAssertion("module.proc1.BoxContainer.sit1",
				   						   "module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer",
				 	 "module.proc1.BoxContainer.sit1",
				 	 new Point(0, -160));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit2",
			  	 	 new Point(0, -60));
		
		assertTrue(noIntersectionAssertion("module.proc1.BoxContainer.sit1",
				   						   "module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit2",
			  	 	 new Point(-40, -40));
		
		assertTrue(noIntersectionAssertion("module.proc1.BoxContainer.sit1",
				   						   "module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit2",
			  	 	 new Point(0, -60));
		
		//**********************************
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 		 "module.proc1.BoxContainer");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 "module.proc1.BoxContainer");
		
		List<EditPart> allSituations = new ArrayList<EditPart>();
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit1"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit2"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit3"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit4"));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit4",
			  	 	 new Point(0, -80));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit4",
			  	 	 new Point(240, -100));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit4",
			  	 	 new Point(-40, -40));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit4",
			  	 	 new Point(-40, 0));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit4",
			  	 	 new Point(0, -40));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit1",
			  	 	 new Point(40, 40));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart("module.proc1.BoxContainer",
			  	 	 "module.proc1.BoxContainer.sit1",
			  	 	 new Point(40, 40));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
	}
	
	public void testResizeOverlap()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		List<EditPart> allSituations = new ArrayList<EditPart>();
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit1"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit2"));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		resizeEditPart("module.proc1.BoxContainer.sit1", 
					   new Dimension(50, 50));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		resizeEditPart("module.proc1.BoxContainer.sit2", 
				   	   new Dimension(50, 50), new Point(-50, -50));
	}
	
	public void testResizeOverlap2()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		List<EditPart> allSituations = new ArrayList<EditPart>();
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit1"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer", 
					 "module.proc1.BoxContainer.sit2", 
					 new Point(240, 0));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		resizeEditPart("module.proc1.BoxContainer.sit2", 
					   new Dimension(100, 100), new Point(-100, -100));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		resizeEditPart("module.proc1.BoxContainer.sit1", 
				   	   new Dimension(100, 100));
	}
	
	public void testResizeOverlap3()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		List<EditPart> allSituations = new ArrayList<EditPart>();
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit1"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer", 
					 "module.proc1.BoxContainer.sit2", 
					 new Point(240, 0));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		resizeEditPart("module.proc1.BoxContainer.sit1", 
					   new Dimension(300, 300));
		
		assertTrue(noIntersectionAssertion(allSituations));
	}
	
	public void testRedoPropagateBug()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		moveEditPart("module.proc1.BoxContainer", 
				 	 "module.proc1.BoxContainer.sit1", 
				 	 new Point(40, 40));
		State s1 = new State("module.proc1.BoxContainer.sit1");
		
		undo();
		undo();
		redo();
		redo();
		
		assertTrue(s1.equals(new State("module.proc1.BoxContainer.sit1")));
	}
	
	public void testResizeOverlap4()
	{
		createElement(EditorRequestConstants.NEW_PROCEDURE, "module.Placeholder");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		createElement(EditorRequestConstants.NEW_SITUATION, "module.proc1.BoxContainer");
		
		List<EditPart> allSituations = new ArrayList<EditPart>();
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit1"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit2"));
		
		moveEditPart("module.proc1.BoxContainer", 
					 "module.proc1.BoxContainer.sit2", 
					 new Point(240, 50));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		State s1 = new State("module.proc1.BoxContainer.sit2");
		
		resizeEditPart("module.proc1.BoxContainer.sit1", 
					   new Dimension(0, 300));
		
		assertTrue(s1.equals(new State("module.proc1.BoxContainer.sit2")));
		
		resizeEditPart("module.proc1.BoxContainer.sit1", 
				   	   new Dimension(300, -300));
		
		assertTrue(s1.equals(new State("module.proc1.BoxContainer.sit2")));
		
		assertTrue(noIntersectionAssertion(allSituations));
	}
	*/
}
