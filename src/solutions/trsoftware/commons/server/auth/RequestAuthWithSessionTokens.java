/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.auth;

import solutions.trsoftware.commons.server.cache.FixedTimeCache;
import solutions.trsoftware.commons.server.util.ServerStringUtils;
import solutions.trsoftware.commons.server.util.UrlSafeBase64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * Prevents replay attack by storing in the request's session up to a certain
 * number of single-use tokens.  Each request must contain one of the tokens,
 * at which point it is invalidated.  The tokens expire after a period of
 * inactivity. 
 *
 * Feb 6, 2013
 *
 * @author Alex
 */
public class RequestAuthWithSessionTokens extends RequestAuth {

  private final int maxOutstandingTokens;
  private final long tokenTtlMillis;

  private final SecureRandom rnd = new SecureRandom();

  private static final String SESSION_ATTR = "authTokens";
  public static final String PARAM_NAME_TOKEN = "token";


  public RequestAuthWithSessionTokens(String macAlogorithm, String publicKey, String secretKey) {
    this(macAlogorithm, publicKey, secretKey, 10, TimeUnit.MINUTES.toMillis(30));
  }

  public RequestAuthWithSessionTokens(String macAlogorithm, String publicKey, String secretKey, int maxOutstandingTokens, long tokenTtlMillis) {
    super(macAlogorithm, publicKey, secretKey);
    this.maxOutstandingTokens = maxOutstandingTokens;
    this.tokenTtlMillis = tokenTtlMillis;
  }

  protected void preventReplayAttack(HttpServletRequest request) throws SecurityException {
    String token = getRequiredParam(request, PARAM_NAME_TOKEN);
    Map<String, Boolean> tokenCache = getOrCreateSessionTokenCache(request);
    if (!tokenCache.containsKey(token))
      throw new SecurityException("Invalid session auth token received");
    // invalidate the token now that it's been used
    tokenCache.remove(token);
  }

  /** Expects the caller to have already added a token to the params */
  @Override
  public void addSigningParams(String method, String url, SortedMap<String, String> paramMap) {
    if (!paramMap.containsKey(PARAM_NAME_TOKEN))
      throw new IllegalArgumentException("The given parameters must contain '" + PARAM_NAME_TOKEN + "' (obtain it by calling generateNewToken)");
    super.addSigningParams(method, url, paramMap);
  }


  /**
   * Adds the given token to the param map if the token is not null.
   * Exposed for unit testing.
   */
  public void addSigningParams(String method, String url, SortedMap<String, String> paramMap, String token) {
    if (token != null)
      paramMap.put(PARAM_NAME_TOKEN, token);
    super.addSigningParams(method, url, paramMap);
  }

  /** Creates a new token and saves it in the session */
  public String generateNewToken(HttpServletRequest request) {
    Map<String, Boolean> tokenCache = getOrCreateSessionTokenCache(request);
    // save the token in the session
    return generateAndSaveToken(tokenCache);
  }

  private String generateAndSaveToken(Map<String, Boolean> tokenCache) {
    String token = newToken();
    tokenCache.put(token, Boolean.TRUE);
    return token;
  }

  /**
   * Generates up to N/2 new tokens and saves them in the request's session cache,
   * where N is the number of tokens that can be generated without overflowing
   * the request's token cache.
   * @return the generated tokens, possibly an array of size 0, but not null
   */
  public String[] generateSpareTokens(HttpServletRequest request) {
    Map<String, Boolean> cache = getOrCreateSessionTokenCache(request);
    synchronized (cache) {
      // we don't want to issue more tokens than the cache can hold
      // (otherwise outstanding tokens could get evicted),
      // so we only generate up to cacheSpareCapacity / 2 tokens
      double cacheSpareCapacity = maxOutstandingTokens - cache.size();
      int nNewTokens = (int)Math.max(0d, Math.floor(cacheSpareCapacity / 2d));
      String[] newTokens = new String[nNewTokens];
      for (int i = 0; i < newTokens.length; i++) {
        newTokens[i] = generateAndSaveToken(cache);
      }
      return newTokens;
    }
  }

  /** Returns the session token cache from the request's session, creating it if necessary */
  public Map<String,Boolean> getOrCreateSessionTokenCache(HttpServletRequest request) {
    HttpSession session = request.getSession(false); // assumes the session exists
    FixedTimeCache<String, Boolean> cache = (FixedTimeCache<String, Boolean>)session.getAttribute(SESSION_ATTR);
     if (cache == null) {
       synchronized (session) {  // double-checked locking
         cache = (FixedTimeCache<String, Boolean>)session.getAttribute(SESSION_ATTR);
         if (cache == null) {
           cache = new FixedTimeCache<String, Boolean>(tokenTtlMillis, maxOutstandingTokens);
           session.setAttribute(SESSION_ATTR, cache);
         }
       }
     }
    return Collections.synchronizedMap(cache);  // the cache must be synchronized
  }

  /** Exposed for unit testing */
  String newToken() {
    byte[] bytes = new byte[8];  // 64 bit token; should be good enough to prevent brute-force guessing attacks
    rnd.nextBytes(bytes);
    // unlike for the signature, we can safely use the url-safe base64 encoding scheme for tokens
    return ServerStringUtils.bytesToStringUtf8(UrlSafeBase64.encodeBase64(bytes));
  }

  public int getMaxOutstandingTokens() {
    return maxOutstandingTokens;
  }

  public long getTokenTtlMillis() {
    return tokenTtlMillis;
  }
}
