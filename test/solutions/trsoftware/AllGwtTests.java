package solutions.trsoftware;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;
import junitx.util.DirectorySuiteBuilder;
import junitx.util.SimpleTestFilter;
import solutions.trsoftware.commons.server.util.CanStopClock;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;

import java.io.File;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * Date: Jan 25, 2008 Time: 7:40:38 PM
 *
 * @author Alex
 */
public class AllGwtTests implements CanStopClock {

  public static Test suite() throws Exception {
    TestSuite directorySuite = (TestSuite)buildSuite(".*/client/.*Test\\.class", GWTTestCase.class, true);
    // sort the test cases into module suites (GWTTestSuite creates a separate suite for each module, so that compilation can be batched)
    GWTTestSuite gwtTestSuite = new GWTTestSuite(AllGwtTests.class.getName());
    for (Enumeration allTests = directorySuite.tests(); allTests.hasMoreElements(); ) {
      gwtTestSuite.addTest((Test)allTests.nextElement());
    }
    // give each module suite a prettier name (strip the ".JUnit.gwt.xml" extension to get the actual name of the module)
    for (Enumeration gwtModuleSuites = gwtTestSuite.tests(); gwtModuleSuites.hasMoreElements(); ) {
      TestSuite testSuite = (TestSuite)gwtModuleSuites.nextElement();
      String name = testSuite.getName();
      int suffixPos = name.indexOf(".JUnit.gwt.xml");
      if (suffixPos >= 0)
        testSuite.setName(name.substring(0, suffixPos) + " Module");
    }
    return gwtTestSuite;
  }

  /**
   * Creates a test suite containing all testable classes matching the given filename pattern
   * in the same compiler output path as this class.
   *
   * Examples:
   * <pre>
   *   buildSuite(".*", GWTTestCase.class, true);  // includes only GWT tests
   *   buildSuite(".*", GWTTestCase.class, false);  // includes only non-GWT tests
   * </pre>
   *
   * @param filenameRegex pattern for matching a filename. Should use
   * the '/' character as a file separator - it will be automatically
   * replaced with the OS-native file separator.
   * @param superclass Tests will only be included in or excluded from the suite if
   * they inherit from this superclass, depending on the value of the excludeSuperclass
   * flag.
   * @param includeSuperclass False if the superclass is to be negated.
   * @return the generated test suite
   *
   */
  public static Test buildSuite(String filenameRegex, final Class superclass, final boolean includeSuperclass) throws Exception {
    // TODO: extract this method to a common utility class (similar to com.typeracer.TestSuiteBuilder), and replace all the test suite builders in com.typeracer with the TR Commons versions
    // TODO: perhaps use our new FileSet class instead of DirectorySuiteBuilder (to remove dependency on junitx)

    File classesDir = ReflectionUtils.getCompilerOutputDir(AllGwtTests.class);

    String separatorRegex = File.separatorChar == '\\' ? "\\\\" : File.separator;
    final Pattern regex = Pattern.compile(filenameRegex.replace("/", separatorRegex));

    DirectorySuiteBuilder builder = new DirectorySuiteBuilder(new SimpleTestFilter() {
      public boolean include(String classpath) {
        return regex.matcher(classpath).matches();
      }
      public boolean include(Class cls) {
        return super.include(cls) &&
            ((includeSuperclass && superclass.isAssignableFrom(cls))
                || (!includeSuperclass && !superclass.isAssignableFrom(cls)));
      }
    });
    return builder.suite(classesDir);
  }

  // TODO: do the same as com.typeracer.commons.server.SuperTestCase.checkForNonNullFields() for GWT unit tests (the reflection can be done in the test suite, and something like a com.typeracer.commons.server.SetUpTearDownDelegate can be added to each test case)
}
