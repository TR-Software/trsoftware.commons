package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

import static solutions.trsoftware.commons.client.util.StringUtils.*;

/**
 * Date: Jul 7, 2008 Time: 2:34:57 PM
 *
 * @author Alex
 */
public class StringUtilsGwtTest extends CommonsGwtTestCase {

  private StringUtilsJavaTest delegate = new StringUtilsJavaTest();

  public void testTemplate() throws Exception {
    delegate.testTemplate();
  }

  public void testStripTrailing() throws Exception {
    delegate.testStripTrailing();
  }

  public void testRepeat() throws Exception {
    delegate.testRepeat();
  }

  public void testConstantNameToTitleCase() throws Exception {
    delegate.testConstantNameToTitleCase();
  }

  public void testRandString() throws Exception {
    delegate.testRandString();
  }

  public void testRandStringExhaustive() throws Exception {
    delegate.testRandStringExhaustive();
  }

  public void testUnderscoresToCamelHumps() throws Exception {
    delegate.testUnderscoresToCamelHumps();
  }

  public void testNotBlank() throws Exception {
    delegate.testNotBlank();
  }

  public void testIsBlank() throws Exception {
    delegate.testIsBlank();
  }

  public void testCount() throws Exception {
    delegate.testCount();
  }

  public void testAbbreviate() throws Exception {
    delegate.testAbbreviate();
  }

  public void testTruncate() throws Exception {
    delegate.testTruncate();
  }

  public void testJoin() throws Exception {
    delegate.testJoin();
  }

  public void testLastIntegerInString() throws Exception {
    delegate.testLastIntegerInString();
  }

  public void testQuantity() throws Exception {
    delegate.testQuantity();
  }

  public void testPluralize() throws Exception {
    delegate.testPluralize();
  }

  public void testCommonPrefix() throws Exception {
    delegate.testCommonPrefix();
  }

  public void testCommonSuffix() throws Exception {
    delegate.testCommonSuffix();
  }

  public void testAppendSurrounded() throws Exception {
    delegate.testAppendSurrounded();
  }

  public void testAppendArgs() throws Exception {
    delegate.testAppendArgs();
  }

  public void testMethodCallToString() throws Exception {
    delegate.testMethodCallToString();
  }

  public void testReverse() throws Exception {
    delegate.testReverse();
  }

  public void testSubstringBefore() throws Exception {
    delegate.testSubstringBefore();
  }

  public void testSubstringAfter() throws Exception {
    delegate.testSubstringAfter();
  }

  public void testSubstringBetween() throws Exception {
    delegate.testSubstringBetween();
  }

  public void testSplitAndTrim() throws Exception {
    delegate.testSplitAndTrim();
  }

  public void testAsList() throws Exception {
    delegate.testAsList();
  }
}