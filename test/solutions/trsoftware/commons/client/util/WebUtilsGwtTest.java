package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.bridge.util.UrlEncoder;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import static solutions.trsoftware.commons.client.util.MapUtils.stringLinkedHashMap;
import static solutions.trsoftware.commons.client.util.WebUtils.*;

/**
 *
 * Date: Oct 20, 2008
 * Time: 8:17:16 PM
 * @author Alex
 */
public class WebUtilsGwtTest extends CommonsGwtTestCase {

//  @Override
//  protected void gwtSetUp() throws Exception {
//    super.gwtSetUp();
//    String hostPageBaseURL = GWT.getHostPageBaseURL();
//    System.out.println("hostPageBaseURL = " + hostPageBaseURL);
//  }

  public void testUrlQueryString() throws Exception {
    assertEquals("foo=1&bar=baz", urlQueryString(
        stringLinkedHashMap("foo", "1", "bar", "baz")));
  }

  public void testUrlQueryStringEncodeParameters() throws Exception {
    String result = urlQueryStringEncode(stringLinkedHashMap(
        "size", "3/4", "phone#=", "617 123 4567"));
    // the GWT version will encode space as %20, while the Java version will encode it as "+"
    // both forms are valid (see http://en.wikipedia.org/wiki/Percent-encoding)
    String expectedResult;
    if ("+".equals(UrlEncoder.get().encode(" ")))
      expectedResult = "size=3%2F4&phone%23%3D=617+123+4567";
    else
      expectedResult = "size=3%2F4&phone%23%3D=617%20123%204567";
    assertEquals(expectedResult, result);
    // test the overloaded version of the method (which takes varargs instead of a hash map)
    assertEquals(result, urlQueryStringEncode("size", "3/4", "phone#=", "617 123 4567"));
  }

  public void testUrlWithQueryString() throws Exception {
    assertEquals("http://example.com?foo=1&bar=baz", urlWithQueryString("http://example.com", "foo", "1", "bar", "baz"));
    // Exception should be thrown for uneven number of arguments
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        urlWithQueryString("http://example.com", "foo", "1", "bar");
      }
    });
  }

  public void test_replaceUrlParameter() throws Exception {
    printWindowLocation();
    String newURL = Window.Location.getHref() + "&foo=1&bar=2";
    System.out.println("       newURL = " + newURL);
    Window.Location.assign(newURL);
    printWindowLocation();
//    fail("TODO"); // TODO
  }

  // TODO: temp
  private void printWindowLocation() {
    String hostPageBaseURL = GWT.getHostPageBaseURL();
    System.out.println("hostPageBaseURL = " + hostPageBaseURL);
    String href = Window.Location.getHref();
    System.out.println("href = " + href);
    String buildString = Window.Location.createUrlBuilder().buildString();
    System.out.println("buildString = " + buildString);
    System.out.println("Removing param gwt.codesvr " + Window.Location.createUrlBuilder().removeParameter("gwt.codesvr").buildString());
    System.out.println("Replacing param gwt.codesvr " + Window.Location.createUrlBuilder().setParameter("gwt.codesvr", "foobar").buildString());
  }
}