package solutions.trsoftware.commons.server.auth;

import solutions.trsoftware.commons.client.util.WebUtils;
import solutions.trsoftware.commons.server.util.Clock;

import javax.servlet.http.HttpServletRequest;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Uses timpestamps to mitigate the possibility of replay attacks.
 * 
 * Feb 6, 2013
 *
 * @author Alex
 */
public class RequestAuthWithTimestamps extends RequestAuth {

  public static final String PARAM_NAME_DATE = "date";
  /*
   * Used to prevent replay attack by ensuring each request's date is not older
   * than this number milliseconds from the last request's date.
   */
  public static final long VALID_TIME_WINDOW_MILLIS = 30000;
  
  private long lastIncomingRequestDate;

  private final Clock.TimeFunction clock;

  public RequestAuthWithTimestamps(String macAlogorithm, String publicKey, String secretKey) {
    this(macAlogorithm, publicKey, secretKey, Clock.SYSTEM_TIME_FCN);
  }

  /** Constructor exposed for unit testing (allows using an instrumented clock) */
  RequestAuthWithTimestamps(String macAlogorithm, String publicKey, String secretKey, Clock.TimeFunction clock) {
    super(macAlogorithm, publicKey, secretKey);
    this.clock = clock;
  }

  /**
   * Prevent replay attack by ensuring there is a passed in "date" parameter,
   * and that date is not older than VALID_TIME_WINDOW_MILLIS before the last request's date.
   * This method expects that all clients (e.g. nodes on Google App Engine)
   * have their clocks sycnronized.
   *
   * Shortcomings of this method:
   * 1) all clients must have their clocks synchronized with each other (this is probably true for GAE)
   * 2) if no requests are received in a while, a stale request that was sent within the allowed time window of the last request can replayed
   * 3) there's still a window of time during which a replay is possible
   * The best replay protection is to use sessions (http://en.wikipedia.org/wiki/Replay_attack)
   *
   * Subclasses may override this method to address the shortcomings.
   */
  @Override
  protected void preventReplayAttack(HttpServletRequest request) throws SecurityException {
    String dateParam = getRequiredParam(request, PARAM_NAME_DATE);
    long requestDate = Long.parseLong(dateParam);
    if (requestDate < lastIncomingRequestDate - VALID_TIME_WINDOW_MILLIS)
      throw new SecurityException(String.format("Stale date received (possible replay attack); request date: %tc; last request date: %tc", requestDate, lastIncomingRequestDate));
    if (requestDate > lastIncomingRequestDate)
      lastIncomingRequestDate = requestDate;
  }

  @Override
  public void addSigningParams(String method, String url, SortedMap<String, String> paramMap) {
    paramMap.put(PARAM_NAME_DATE, String.valueOf(clock.currentTimeMillis()));
    super.addSigningParams(method, url, paramMap);
  }

  public static void main(String[] args) {
    // Simple test
    SortedMap<String, String> params = new TreeMap<String, String>();
    params.put("a", "1");
    params.put("b", "2");
    new RequestAuthWithTimestamps("hmacSha1", "pub1234", "scretABCD").addSigningParams("GET", "http://example.com", params);
    System.out.println("Request URL: http://example.com?" + WebUtils.urlQueryString(params));
  }
}
