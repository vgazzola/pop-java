package junit;

import junit.annotations.AnnotationTests; 
import junit.friendComputing.FriendComputingTest;
import junit.localtests.LocalTests;
import junit.system.SystemTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { AnnotationTests.class, LocalTests.class, SystemTests.class, FriendComputingTest.class})
public class AllTests {

}
