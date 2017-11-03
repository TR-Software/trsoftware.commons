package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * Mar 15, 2011
 *
 * @author Alex
 */
public class StringTokenizerGwtTest extends CommonsGwtTestCase {

  private StringTokenizerTest delegate = new StringTokenizerTest();

  public void testEmptyString() throws Exception {
    delegate.testEmptyString();
  }

  public void testStringLength1NoDelimiters() throws Exception {
    delegate.testStringLength1NoDelimiters();
  }

  public void testStringLength1WithDelimiters() throws Exception {
    delegate.testStringLength1WithDelimiters();
  }

  public void testStringLength2AllDelimiters() throws Exception {
    delegate.testStringLength2AllDelimiters();
  }

  public void testStringLength2NoDelimiters() throws Exception {
    delegate.testStringLength2NoDelimiters();
  }

  public void testStringLength2With1Delimiter() throws Exception {
    delegate.testStringLength2With1Delimiter();
  }

  public void testStringLength3With1Delimiter() throws Exception {
    delegate.testStringLength3With1Delimiter();
  }

  public void testStringLength3With2Delimiters() throws Exception {
    delegate.testStringLength3With2Delimiters();
  }

  public void testTokenize() throws Exception {
    delegate.testTokenize();
  }
}