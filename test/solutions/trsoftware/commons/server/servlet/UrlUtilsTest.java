package solutions.trsoftware.commons.server.servlet;

import junit.framework.TestCase;

import java.net.URL;

import static solutions.trsoftware.commons.server.servlet.UrlUtils.rewrite;
import static solutions.trsoftware.commons.server.servlet.UrlUtils.urlQueryString;
import static solutions.trsoftware.commons.shared.util.MapUtils.stringLinkedHashMap;

/**
 * @author Alex
 * @since 11/14/2017
 */
public class UrlUtilsTest extends TestCase {

  public void testUrlQueryString() throws Exception {
    assertEquals("foo=1&bar=baz", urlQueryString(
            stringLinkedHashMap("foo", "1", "bar", "baz")));
    assertEquals("size=3%2F4&phone%23%3D=617+123+4567", urlQueryString(
            stringLinkedHashMap("size", "3/4", "phone#=", "617 123 4567")));
    // now test the overloaded method that takes an array
    assertEquals("foo=1&bar=baz", urlQueryString("foo", "1", "bar", "baz"));
    assertEquals("size=3%2F4&phone%23%3D=617+123+4567", urlQueryString(
            "size", "3/4", "phone#=", "617 123 4567"));
  }

  public void testRewrite() throws Exception {
    String[] protocols = new String[]{"http", "https"};
    for (String protocol : protocols) {
      URL url = new URL(protocol + "://www.example.com/foo/bar?foo=bar&bar=baz");
      assertEquals(protocol + "://www.example.com/new/path?p1=foo&p2=bar",
          rewrite(url, "/new/path", "p1", "foo", "p2", "bar"));
    }
  }

}