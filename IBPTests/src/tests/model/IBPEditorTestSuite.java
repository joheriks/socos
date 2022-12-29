package tests.model;
import junit.framework.Test;
import junit.framework.TestSuite;

public class IBPEditorTestSuite{
	
	public static Test suite(){
        TestSuite suite = new TestSuite("IBPEditor Tests");
        
        suite.addTestSuite(TestModel.class);
        suite.addTestSuite(TestMoveCommand.class);
        suite.addTestSuite(TestCommands.class);
        
		return suite;
	}
	
	
}
