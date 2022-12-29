package ibpe.unittests.GUITests;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class GUITestSuite implements Test {
	
	public static Test suite()
	{
		TestSuite testSuite = new TestSuite(CreateDeleteElementTest.class,
											CreateTransitionTest.class,
											OverlapTest.class,
											ReparentTest.class,
											TextRowReparentTest.class);
		
		return testSuite;
	}

	public int countTestCases() {
		return 0;
	}

	public void run(TestResult result) {}

}
