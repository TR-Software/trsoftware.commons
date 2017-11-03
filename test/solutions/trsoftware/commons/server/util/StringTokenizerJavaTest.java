package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.client.util.CollectionUtils;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.testutil.PerformanceComparison;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.List;


/**
 * Oct 19, 2009
 *
 * @author Alex
 */
public class StringTokenizerJavaTest extends TestCase {
  private String aliceText;


  protected void setUp() throws Exception {
    super.setUp();
    aliceText = ServerIOUtils.readResourceFileIntoString(
        ServerIOUtils.resourceNameFromFilenameInSamePackage("aliceInWonderlandCorpus.txt", getClass()));
  }

  /**
   * Compares performance of solutions.trsoftware.commons.client.util.StringTokenizer
   * to java.util.StringTokenizer.
   */
  public void testCompareTokenization() throws Exception {
    assertEquals(javaUtilTokenize(aliceText), typeracerCommonsUtilTokenize(aliceText));
  }

  /**
   * Compares performance of solutions.trsoftware.commons.client.util.StringTokenizer
   * to java.util.StringTokenizer.
   */
  public void testComparePerformance() throws Exception {
    // assert that our implementation is at most twice as fast as java.util's
    assertTrue(2 >=
        PerformanceComparison.compare(
            new JavaTokenizeAction(), "java.util.StringTokenizer",
            new TyperacerTokenizeAction(), "solutions.trsoftware.commons.client.util.StringTokenizer",
            200));
    assertTrue(.5 <=
        PerformanceComparison.compare(
            new TyperacerTokenizeAction(), "solutions.trsoftware.commons.client.util.StringTokenizer",
            new JavaTokenizeAction(), "java.util.StringTokenizer",
            200));
  }

  /**
   * Tokenizes the given string using java.util.StringTokenizer
   *
   * @return the tokens from the given string
   */
  private List<String> typeracerCommonsUtilTokenize(String text) {
    return CollectionUtils.asList(new solutions.trsoftware.commons.client.util.StringTokenizer(text));
  }

  /**
   * Tokenizes the given string using java.util.StringTokenizer
   *
   * @return the tokens from the given string
   */
  private List javaUtilTokenize(String text) {
    return Collections.list(new java.util.StringTokenizer(text));
  }

  private class JavaTokenizeAction implements Runnable {
    public void run() {
      javaUtilTokenize(aliceText);
    }
  }

  private class TyperacerTokenizeAction implements Runnable {
    public void run() {
      typeracerCommonsUtilTokenize(aliceText);
    }
  }
}