package tests.model;

import ibpe.commands.MoveCommand;

import org.junit.Test;


public class TestMoveCommand extends TestProgramBase {
	
	private MoveCommand move;
	
	@Override
	public void setUp() {
		super.setUp();
		move = new MoveCommand();	
	}
	
	
	@Override
	public String getProgram() {
		return 
		"m : MODULE BEGIN %:\n" +
			"lm\n" +
		    "p1 [%:\n" +
		        "asd\n" +
		    "]: PROCEDURE BEGIN %:\n" +
		        "s1 : SITUATION BEGIN %:\n" +
		        	"s3 : SITUATION BEGIN %:\n" +
		        		"ls3\n" +
		        	"END s3 %:\n" +
		        	"ls1\n" +
		        	"%: trs\n" +
		        	"IF %:\n" +
		        		"%: trs\n" +
		        		"X\n" +
		        			"GOTO s2 %:\n" +
		        	"ENDIF %:\n" +
		        "END s1 %:\n" +
		        "lp1\n" +
			"END p1%:\n" +
			"p2 [%:\n" +
			    "asd\n" +
			"]: PROCEDURE BEGIN %:\n" +
			    "s2 : SITUATION BEGIN %:\n" +
			    	"ls2\n" +
			    "END s2%:\n" +
			    "lp2\n" +
			"END p2%:\n" +
		"END m%:\n";
	}
	
	@Test
	public void testIllegalmoves(){
		
		//module not allowed to be moved anywhere
		procedure("fjdslfjls");
		move.setInsertAt(module("m"),procedure("p1"));	assertFalse(move.canExecute());
		move.setInsertAt(module("m"),situation("s1"));	assertFalse(move.canExecute());
		move.setInsertAt(module("m"),leaf("ls1"));	assertFalse(move.canExecute());
//		move.setInsertAt(module("m"),branch("call")); assertFalse(move.canExecute());
		
		//situations can't be moved into Modules
		move.setAppendTo(situation("s1"), module("m")); assertFalse(move.canExecute());
		
		//situations can't be moved into a child situation
		move.setAppendTo(situation("s1"), situation("s3")); assertFalse(move.canExecute());
	}
	
	
	@Test
	public void testLegalMoves1() {
	
		// move p1 last
		move.setAppendTo(procedure("p1"), module("m"));		
		assertTrue(move.canExecute());
		move.execute();		
		assertTrue(module("m").contains(procedure("p1")));
		
		// move p1 first
		move.setInsertAt(procedure("p1"), leaf("lm"));		
		assertTrue(move.canExecute());
		move.execute();		
		assertTrue(module("m").contains(procedure("p1")));
		assertEquals( procedure("p1").getPosition(), leaf("lm").getPosition()-1 );
		
		// move p1 back to original position
		move.setInsertAt(procedure("p1"), procedure("p2"));
		assertTrue(move.canExecute());
		move.execute();		
		assertTrue(module("m").contains(procedure("p1")));
		assertEquals( procedure("p1").getPosition(), leaf("lm").getPosition()+1 );
	}
	
	
	@Test
	public void testLegalMoves2() {
		// move a situation between procedures
		move.setInsertAt(situation("s1"), situation("s2"));		
		assertTrue(move.canExecute());
		move.execute();		
		assertTrue(procedure("p2").getBoxContainer().contains(situation("s1")));
		assertFalse(procedure("p1").getBoxContainer().contains(situation("s1")));
		
		// move s1 back
		move.setAppendTo(situation("s1"), procedure("p1"));
		assertTrue(move.canExecute());
		move.execute();		
		assertTrue(procedure("p1").getBoxContainer().contains(situation("s1")));
		assertFalse(procedure("p2").getBoxContainer().contains(situation("s1")));
	}
	
	public void testLegalMoves3() {
		// appending should add an element to the last position
		move.setAppendTo(leaf("lm"),module("m"));
		assertTrue(move.canExecute());
		move.execute();
		assertEquals( leaf("lm").getPosition(), 2 );
		
	}
	
}
