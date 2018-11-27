// This example was found in a JUnit 4 Tutorial by David Janzen
// Ref: http://users.csc.calpoly.edu/~djanzen/tdl/tddintro/helloworld/
package proj9AbramsDeutschDurstJones.bantam.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import junit.framework.JUnit4TestAdapter;

// This section declares all of the test classes in your program.
@RunWith(Suite.class)
@Suite.SuiteClasses({
   TestHelloWorld.class  // Add test classes here.
})

public class TestSuite
{
	//This can be empty if you are using an IDE that includes support for JUnit
	//such as Eclipse.  However, if you are using Java on the command line or
	//with a simpler IDE like JGrasp or jCreator, the following main() and suite()
	//might be helpful.

    // Execution begins at main().  In this test class, we will execute
    // a text test runner that will tell you if any of your tests fail.
    public static void main (String[] args) 
    {
       junit.textui.TestRunner.run (suite());
    }


		// The suite() method is helpful when using JUnit 3 Test Runners or Ant.
    public static junit.framework.Test suite() 
    {
       return new JUnit4TestAdapter(TestSuite.class);
    }
}
