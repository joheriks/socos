package tests.gui;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class GUITestSuite implements Test {
	
	public static Test suite() {
		TestSuite testSuite = new TestSuite(CreateElementTest.class,
											CreateTransitionTest.class,
											OverlapTest.class,
											UndoRedoTest.class,
											ReparentTest.class,
											TextRowReparentTest.class);
		
		return testSuite;
	}

	@Override
	public int countTestCases() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void run(TestResult result) {
		// TODO Auto-generated method stub
		
	}

}
