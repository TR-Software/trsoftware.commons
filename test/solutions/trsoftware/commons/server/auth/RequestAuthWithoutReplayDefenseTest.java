package solutions.trsoftware.commons.server.auth;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertThrows;
import solutions.trsoftware.commons.client.util.MapUtils;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletRequest;
import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.SortedMap;

/**
 * Feb 7, 2013
 *
 * @author Alex
 */

public class RequestAuthWithoutReplayDefenseTest extends TestCase {
  private RequestAuthWithoutReplayDefense auth = new RequestAuthWithoutReplayDefense(
      "hmacSha1", "publicKeyString", "secretKeyString");

  /** Bidirectional test: signs request then makes sure the same request validates */
  public void testRequestSigningAndValidation() throws Exception {
    {
      // this should succeed
      HttpServletRequest request = newSignedRequest();
      assertTrue(auth.authenticateIncomingRequest(request));
    }
    // should throw if either parameter is missing
    {
      final DummyHttpServletRequest request = newSignedRequest();
      request.removeParameter(RequestAuth.PARAM_NAME_ACCESS_KEY);
      AssertUtils.assertThrows(SecurityException.class, new Runnable() {
        public void run() {
          auth.authenticateIncomingRequest(request);
        }
      });
    }
    {
      final DummyHttpServletRequest request = newSignedRequest();
      request.removeParameter(RequestAuth.PARAM_NAME_SIGNATURE);
      AssertUtils.assertThrows(SecurityException.class, new Runnable() {
        public void run() {
          auth.authenticateIncomingRequest(request);
        }
      });
    }
    // should throw if either parameter is incorrect
    {
      final DummyHttpServletRequest request = newSignedRequest();
      request.putParameter(RequestAuth.PARAM_NAME_ACCESS_KEY, "bad value");
      AssertUtils.assertThrows(SecurityException.class, new Runnable() {
        public void run() {
          auth.authenticateIncomingRequest(request);
        }
      });
    }
    {
      final DummyHttpServletRequest request = newSignedRequest();
      request.putParameter(RequestAuth.PARAM_NAME_SIGNATURE, "bad value");
      AssertUtils.assertThrows(SecurityException.class, new Runnable() {
        public void run() {
          auth.authenticateIncomingRequest(request);
        }
      });
    }
  }

  private DummyHttpServletRequest newSignedRequest() {
    SortedMap<String,String> params = MapUtils.sortedMap("foo", "1", "bar", "baz");
    String url = "http://example.com/authenticated";
    final String method = "GET";
        auth.addSigningParams(method, url, params);
    // make sure the signing parameters have been added by the RequestAuth
    assertEquals(auth.getPublicKey(), params.get(RequestAuth.PARAM_NAME_ACCESS_KEY));
    assertTrue(params.containsKey(RequestAuth.PARAM_NAME_SIGNATURE));
    // make sure that a request with these parameters will validate
    return new DummyHttpServletRequest(url, null, params) {
      @Override
      public String getMethod() {
        return method;
      }
    };
  }
}