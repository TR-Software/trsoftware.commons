package solutions.trsoftware.commons.server.auth;

import solutions.trsoftware.commons.client.util.MapUtils;
import solutions.trsoftware.commons.server.TestCaseCanStopClock;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletRequest;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpSession;
import solutions.trsoftware.commons.server.util.Clock;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.SortedMap;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertThrows;

/**
 * Feb 7, 2013
 *
 * @author Alex
 */
public class RequestAuthWithSessionTokensTest extends TestCaseCanStopClock {
  private int maxOutstandingTokens = 3;
  private long tokenTtlMillis = 5000;
  private RequestAuthWithSessionTokens auth = new RequestAuthWithSessionTokens("hmacSha1", "publicKeyString", "secretKeyString", maxOutstandingTokens, tokenTtlMillis);

  private DummyHttpSession session;
  private String url = "http://example.com/authenticated";
  private String method = "GET";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    session = new DummyHttpSession();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    session = null;
  }

  public void testAddSigningParams() throws Exception {
    // this method should make sure that a token parameter is included
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        auth.addSigningParams(method, url, MapUtils.<String, String>sortedMap("foo", "1", "bar", "baz"));
      }
    });
  }

  private void assertThrowsWithToken(Class<? extends Throwable> expectedThrowableClass, final String token) {
    AssertUtils.assertThrows(expectedThrowableClass, new Runnable() {
      public void run() {
        auth.authenticateIncomingRequest(newSignedRequest(token));
      }
    });
  }

  public void testFailsWithoutToken() throws Exception {
    assertThrowsWithToken(SecurityException.class, null);
  }

  public void testFailsWithBadToken() throws Exception {
    // these tokens don't exist
    assertThrowsWithToken(SecurityException.class, "");
    assertThrowsWithToken(SecurityException.class, "foo");
  }

  public void testSucceedsWithGoodToken() throws Exception {
    String token = generateNewToken();
    assertTrue(auth.authenticateIncomingRequest(newSignedRequest(token)));
  }

  private String generateNewToken() {
    // this stores the token in the session
    return auth.generateNewToken(newMockRequest());
  }

  private DummyHttpServletRequest newMockRequest() {
    return newMockRequest(MapUtils.<String, String>sortedMap());
  }

  private DummyHttpServletRequest newMockRequest(SortedMap<String, String> params) {
    return new DummyHttpServletRequest(method, url, null, params).setSession(session);
  }

  public void testSucceedsWithMultipleTokens() throws Exception {
    // now check having 2 outstanding tokens and using them out of order
    String token1 = generateNewToken();
    String token2 = generateNewToken();
    assertTrue(auth.authenticateIncomingRequest(newSignedRequest(token2)));
    assertTrue(auth.authenticateIncomingRequest(newSignedRequest(token1)));
  }

  public void testFailsWithUsedToken() throws Exception {
    // make sure the tokens are for one-time use
    String token = generateNewToken();
    assertTrue(auth.authenticateIncomingRequest(newSignedRequest(token)));
    assertThrowsWithToken(SecurityException.class, token);
  }

  public void testFailsWithExpiredToken() throws Exception {
    Clock.stop();
    String token = generateNewToken();
    Clock.advance(tokenTtlMillis + 1);
    assertThrowsWithToken(SecurityException.class, token);
  }

  public void testFailsWithForcedOutToken() throws Exception {
    // if we create more tokens than the cache capacity, the earlier ones will be forced out
    String[] tokens = new String[maxOutstandingTokens];
    for (int i = 0; i < tokens.length; i++) {
      tokens[i] = generateNewToken();
    }
    // the token cache should now be at capacity
    // first, make sure the oldest token still works
    assertTrue(auth.authenticateIncomingRequest(newSignedRequest(tokens[0])));
    // now put 2 more tokens to overflow the capacity
    generateNewToken();
    generateNewToken();
    // now the oldest token should fail
    assertThrowsWithToken(SecurityException.class, tokens[1]);
  }

  private HttpServletRequest newSignedRequest(String token) {
    SortedMap<String, String> params = MapUtils.sortedMap("foo", "1", "bar", "baz");
    auth.addSigningParams(method, url, params, token);
    // make sure the signing parameters have been added by the RequestAuth
    assertEquals(auth.getPublicKey(), params.get(RequestAuth.PARAM_NAME_ACCESS_KEY));
    assertTrue(params.containsKey(RequestAuth.PARAM_NAME_SIGNATURE));
    // make sure that a request with these parameters will validate
    return newMockRequest(params);
  }

  public void testGenerateNewToken() throws Exception {
    testSucceedsWithGoodToken();
  }

  public void testGenerateSpareTokens() throws Exception {
    // each call to generateSpareTokens should return N tokens, where N is
    // half the size of the outstanding capacity of that cache
    RequestAuthWithSessionTokens mockAuth = new RequestAuthWithSessionTokens("hmacSha1", "publicKeyString", "secretKeyString", 5, tokenTtlMillis);
    DummyHttpServletRequest mockRequest = newMockRequest();
    String[] spareTokens = mockAuth.generateSpareTokens(mockRequest);
    // since the cache capacity is 5, and we're starting with an empty cache, this call should give 2 tokens
    assertEquals(2, spareTokens.length);
    // now the cache capacity is 2
    Map<String,Boolean> cache = mockAuth.getOrCreateSessionTokenCache(mockRequest);
    assertEquals(2, cache.size());
    // calling this method again, will generate 1 more token (half the remaining capacity of 3)
    String[] spareTokens2 = mockAuth.generateSpareTokens(mockRequest);
    assertEquals(1, spareTokens2.length);
    assertEquals(3, cache.size());
    // calling this method again, will generate 1 more token (half the remaining capacity of 2)
    String[] spareTokens3 = mockAuth.generateSpareTokens(mockRequest);
    assertEquals(1, spareTokens3.length);
    assertEquals(4, cache.size());
    // calling this method again, will generate 0 tokens (half the remaining capacity of 1)
    String[] spareTokens4 = mockAuth.generateSpareTokens(mockRequest);
    assertEquals(0, spareTokens4.length);
    assertEquals(4, cache.size());
    // now use up all the outstanding tokens
    for (String spareToken : spareTokens) {
      assertTrue(mockAuth.authenticateIncomingRequest(newSignedRequest(spareToken)));
    }
    for (String spareToken : spareTokens2) {
      assertTrue(mockAuth.authenticateIncomingRequest(newSignedRequest(spareToken)));
    }

    for (String spareToken : spareTokens3) {
      assertTrue(mockAuth.authenticateIncomingRequest(newSignedRequest(spareToken)));
    }
    assertEquals(0, cache.size());
    // the next call should generate 2 new tokens once again
    assertEquals(2, mockAuth.generateSpareTokens(mockRequest).length);
    assertEquals(2, cache.size());
  }

}