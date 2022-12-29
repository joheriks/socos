package ibpe.unittests.ModelTests;
import junit.framework.Test;
import junit.framework.TestSuite;

public class IBPEditorTestSuite{
	
	public static Test suite(){
        TestSuite suite = new TestSuite("IBPEditor Tests");
        
        suite.addTestSuite(TestCommands.class);
        
		return suite;
	}
	
	
}
