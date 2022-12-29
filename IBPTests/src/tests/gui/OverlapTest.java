package tests.gui;

import ibpe.EditorRequestConstants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;

public class OverlapTest extends AbstractTest {
	
	public void testMoveOverlap() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 		 findEditPart("module.Placeholder"));
		
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 		 findEditPart("module.proc1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 		findEditPart("module.proc1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit2");
		
		//**********************************
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
					 findEditPart("module.proc1.BoxContainer.sit1"),
					 new Point(0, 40));
	
		assertTrue(noIntersectionAssertion(findEditPart("module.proc1.BoxContainer.sit1"),
										   findEditPart("module.proc1.BoxContainer.sit2")));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
				  	 findEditPart("module.proc1.BoxContainer.sit2"),
				  	 new Point(0, -40));
		
		assertTrue(noIntersectionAssertion(findEditPart("module.proc1.BoxContainer.sit1"),
				   findEditPart("module.proc1.BoxContainer.sit2")));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit2"),
			  	 new Point(220+40, 60));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
				 findEditPart("module.proc1.BoxContainer.sit1"),
				 new Point(0, 160));
		
		assertTrue(noIntersectionAssertion(findEditPart("module.proc1.BoxContainer.sit1"),
				   findEditPart("module.proc1.BoxContainer.sit2")));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
				 findEditPart("module.proc1.BoxContainer.sit1"),
				 new Point(120, 0));
		
		assertTrue(noIntersectionAssertion(findEditPart("module.proc1.BoxContainer.sit1"),
				   findEditPart("module.proc1.BoxContainer.sit2")));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit2"),
			  	 new Point(-100, 0));
		
		assertTrue(noIntersectionAssertion(findEditPart("module.proc1.BoxContainer.sit1"),
				   findEditPart("module.proc1.BoxContainer.sit2")));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit2"),
			  	 new Point(-100, 0));
		
		assertTrue(noIntersectionAssertion(findEditPart("module.proc1.BoxContainer.sit1"),
				   findEditPart("module.proc1.BoxContainer.sit2")));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
				 findEditPart("module.proc1.BoxContainer.sit1"),
				 new Point(0, -160));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit2"),
			  	 new Point(0, -60));
		
		assertTrue(noIntersectionAssertion(findEditPart("module.proc1.BoxContainer.sit1"),
				   findEditPart("module.proc1.BoxContainer.sit2")));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit2"),
			  	 new Point(-40, -40));
		
		assertTrue(noIntersectionAssertion(findEditPart("module.proc1.BoxContainer.sit1"),
				   findEditPart("module.proc1.BoxContainer.sit2")));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit2"),
			  	 new Point(0, -60));
		
		//**********************************
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 		 findEditPart("module.proc1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit3");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
		 		 findEditPart("module.proc1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit4");
		
		List<EditPart> allSituations = new ArrayList<EditPart>();
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit1"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit2"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit3"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit4"));
		
		//***********************************
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit4"),
			  	 new Point(0, -80));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit4"),
			  	 new Point(240, -100));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit4"),
			  	 new Point(-40, -40));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit4"),
			  	 new Point(-40, 0));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit4"),
			  	 new Point(0, -40));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit1"),
			  	 new Point(40, 40));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"),
			  	 findEditPart("module.proc1.BoxContainer.sit1"),
			  	 new Point(40, 40));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
	}
	
	public void testResizeOverlap() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				 		 findEditPart("module.proc1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				 		findEditPart("module.proc1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit2");
		
		List<EditPart> allSituations = new ArrayList<EditPart>();
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit1"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit2"));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		resizeEditPart(findEditPart("module.proc1.BoxContainer.sit1"), 
					   new Dimension(50, 50));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		resizeEditPart(findEditPart("module.proc1.BoxContainer.sit2"), 
				   new Dimension(50, 50), new Point(-50, -50));
	}
	
	public void testResizeOverlap2() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				 		 findEditPart("module.proc1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				 		findEditPart("module.proc1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit2");
		
		List<EditPart> allSituations = new ArrayList<EditPart>();
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit1"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit2"));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"), 
					 findEditPart("module.proc1.BoxContainer.sit2"), 
					 new Point(240, 0));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		resizeEditPart(findEditPart("module.proc1.BoxContainer.sit2"), 
					   new Dimension(100, 100), new Point(-100, -100));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		resizeEditPart(findEditPart("module.proc1.BoxContainer.sit1"), 
				   new Dimension(100, 100));
	}
	
	public void testResizeOverlap3() {
		
		createElement(EditorRequestConstants.NEW_PROCEDURE, 
		 		 findEditPart("module.Placeholder"));
		renameEditPart(findEditPart("module.default"), "proc1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				 		 findEditPart("module.proc1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit1");
		
		createElement(EditorRequestConstants.NEW_SITUATION, 
				 		findEditPart("module.proc1.BoxContainer"));
		renameEditPart(findEditPart("module.proc1.BoxContainer.Unique name"), "sit2");
		
		List<EditPart> allSituations = new ArrayList<EditPart>();
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit1"));
		allSituations.add(findEditPart("module.proc1.BoxContainer.sit2"));
		
		moveEditPart(findEditPart("module.proc1.BoxContainer"), 
					 findEditPart("module.proc1.BoxContainer.sit2"), 
					 new Point(240, 0));
		
		assertTrue(noIntersectionAssertion(allSituations));
		
		resizeEditPart(findEditPart("module.proc1.BoxContainer.sit1"), 
					   new Dimension(300, 300));
		
		assertTrue(noIntersectionAssertion(allSituations));
	}
}
