package solutions.trsoftware.commons.server.auth;

import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.client.util.MapUtils;
import solutions.trsoftware.commons.server.TestCaseCanStopClock;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletRequest;
import solutions.trsoftware.commons.server.util.Clock;

import javax.servlet.http.HttpServletRequest;
import java.util.SortedMap;

/**
 * Mar 6, 2010
 *
 * @author Alex
 */
public class RequestAuthWithTimestampsTest extends TestCaseCanStopClock {


  /** Bidirectional test: signs request then makes sure the same request validates */
  public void testRequestSigningAndValidation() throws Exception {
    Clock.stop();
    final RequestAuth auth = new RequestAuthWithTimestamps("hmacSha1", "publicKeyString", "secretKeyString", Clock.INSTRUMENTED_TIME_FCN);
    final HttpServletRequest request = newSignedRequest(auth);

    assertTrue(auth.authenticateIncomingRequest(request));
    // the same request should only be valid for up to 30 seconds before the original date
    Clock.advance(RequestAuthWithTimestamps.VALID_TIME_WINDOW_MILLIS - 1);
    assertTrue(auth.authenticateIncomingRequest(newSignedRequest(auth)));  // send a different request with the new clock time to update the last request time in auth
    // the old request should still be valid for another millisecond
    assertTrue(auth.authenticateIncomingRequest(request));
    Clock.advance(2);
    assertTrue(auth.authenticateIncomingRequest(newSignedRequest(auth)));  // send a different request with the new clock time to update the last request time in auth
    // now should be expired
    AssertUtils.assertThrows(SecurityException.class, new Runnable() {
      public void run() {
        auth.authenticateIncomingRequest(request);
      }
    });
  }

  private HttpServletRequest newSignedRequest(RequestAuth auth) {
    SortedMap<String,String> params = MapUtils.sortedMap("foo", "1", "bar", "baz");
    String url = "http://example.com/authenticated";
    final String method = "GET";
        auth.addSigningParams(method, url, params);
    // make sure the signing parameters have been added by the RequestAuth
    assertEquals(auth.getPublicKey(), params.get(RequestAuth.PARAM_NAME_ACCESS_KEY));
    assertTrue(params.containsKey(RequestAuth.PARAM_NAME_SIGNATURE));
    assertTrue(params.containsKey(RequestAuthWithTimestamps.PARAM_NAME_DATE));
    // make sure that a request with these parameters will validate
    return new DummyHttpServletRequest(url, null, params) {
      @Override
      public String getMethod() {
        return method;
      }
    };
  }
}