package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;

/**
 *
 * @author Alex
 */
public class WebUtilsJavaTest extends TestCase {

  WebUtilsGwtTest delegate = new WebUtilsGwtTest();

  public void testUrlQueryString() throws Exception {
    delegate.testUrlQueryString();
  }

  public void testUrlQueryStringEncodeParameters() throws Exception {
    delegate.testUrlQueryStringEncodeParameters();
  }

  public void testUrlWithQueryString() throws Exception {
    delegate.testUrlWithQueryString();
  }
}