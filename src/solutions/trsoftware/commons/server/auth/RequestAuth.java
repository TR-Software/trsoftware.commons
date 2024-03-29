/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.server.auth;

import org.apache.commons.codec.binary.Base64;
import solutions.trsoftware.commons.client.util.WebUtils;
import solutions.trsoftware.commons.server.net.http.HttpMethodName;
import solutions.trsoftware.commons.server.servlet.ServletUtils;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.server.util.ServerStringUtils;
import solutions.trsoftware.commons.server.util.UrlSafeBase64;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.SortedMap;

/**
 * Implements OAuth-like request signing and validation.
 * <p>
 * The 2 API methods are:
 * <ol>
 * <li>{@link #addSigningParams} - to sign outgoing requests</li>
 * <li>{@link #authenticateIncomingRequest} - to authenticate incoming requests</li>
 * </ol>
 * <p>
 * Uses the request parameters {@value #PARAM_NAME_ACCESS_KEY}, which provides the client's the public key,
 * and {@value #PARAM_NAME_SIGNATURE}, which contains the signature computed by a signing the request parameters
 * using a MAC algorithm (e.g. HMAC-SHA1) keyed with the client's secret key.
 * <p>
 * Subclasses can specify a strategy for preventing replay attacks by overriding {@link #preventReplayAttack}.
 * One such strategy is to use timestamps (see {@link RequestAuthWithTimestamps}), and
 * another is to use session tokens (see {@link RequestAuthWithSessionTokens}).
 *
 * @author Alex
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html#RESTAuthenticationQueryStringAuth">
 *     Amazon S3 authentication reference, which this class is based on</a>
 * @see RequestAuthWithTimestamps
 * @see RequestAuthWithSessionTokens
 * @see RequestAuthWithoutReplayDefense
 * @since Mar 5, 2010
 */
public abstract class RequestAuth {

  private final String publicKey;
  private final String secretKey;

  /** The name of the message authentication code (MAC) algorithm to be used (e.g. "HmacSHA1") */
  private final String macAlgorithm;

  public static final String PARAM_NAME_ACCESS_KEY = "access_key";
  public static final String PARAM_NAME_SIGNATURE = "sig";

  public RequestAuth(String macAlgorithm, String publicKey, String secretKey) {
    this.publicKey = publicKey;
    this.secretKey = secretKey;
    this.macAlgorithm = macAlgorithm;
  }

  /**
   * Adds the proper {@value #PARAM_NAME_ACCESS_KEY} and {@value #PARAM_NAME_SIGNATURE} to the given parameter map,
   * computed based on the encapsulated credentials.
   *
   * @param method the HTTP method name (will be used to generate the {@linkplain #generateBaseString base string})
   * @param url the request URL (will be used to generate the {@linkplain #generateBaseString base string})
   * @param paramMap the new parameters will be added to this map (should be mutable)
   */
  public void addSigningParams(String method, String url, SortedMap<String, String> paramMap) {
    paramMap.put(PARAM_NAME_ACCESS_KEY, publicKey);
    String baseString = generateBaseString(method, url, paramMap);
//    System.out.println("baseString = " + baseString);
    paramMap.put(PARAM_NAME_SIGNATURE, sign(baseString));
  }

  public final void addSigningParams(HttpMethodName method, String url, SortedMap<String, String> paramMap) {
    addSigningParams(method.name(), url, paramMap);
  }

  /**
   * Checks that the signature of the given request matches our expectation.
   *
   * @param request the incoming request
   * @return true if the request is properly authenticated, otherwise throws
   * @throws SecurityException if the request doesn't authenticate; the exception
   *                           message is safe to display to a user - it doesn't leak any confidential
   *                           info (e.g. the expected signature of the given request).
   */
  public boolean authenticateIncomingRequest(HttpServletRequest request) throws SecurityException {
    String accessKeyParam = getRequiredParam(request, PARAM_NAME_ACCESS_KEY);
    if (!publicKey.equals(accessKeyParam)) {
      throw new SecurityException("Request access key doesn't match expectation");
    }
    String sigParam = getRequiredParam(request, PARAM_NAME_SIGNATURE);
    // generate and validate the base string
    SortedMap<String, String> params = ServletUtils.getRequestParametersAsSortedStringMap(request);
    // the base string should not include the signature parameter
    params.remove(PARAM_NAME_SIGNATURE);
    String baseString = generateBaseString(request.getMethod(), request.getRequestURL().toString(), params);
    String expectedSignature = sign(baseString);
    if (!sigParam.equals(expectedSignature)) {
//      System.out.printf("Signatures don't match; base string: %s; submitted signature %s; expected signature: %s%n", baseString, sigParam, expectedSignature);
      throw new SecurityException("Request signature doesn't match expectation");  // don't disclose the expected signature to the caller for security
    }
    preventReplayAttack(request);
    return true;
  }

  /**
   * @param request an incoming request
   * @throws SecurityException if the request is suspected of duplicating a prior request
   */
  protected abstract void preventReplayAttack(HttpServletRequest request) throws SecurityException;

  /**
   * Generates the string to be used in computing the signature for a request with the given method/url/parameters
   * combination.
   *
   * @return a string that looks like
   *     "<code>{method}&{url}&{name<sub>1</sub>}={value<sub>1</sub>}&...&{name<sub>N</sub>}={value<sub>N</sub>}</code>"
   */
  private String generateBaseString(String method, String url, SortedMap<String, String> paramMap) {
    return new StringBuilder(256).append(method).append("&").append(url).append("&")
        .append(WebUtils.urlQueryString(paramMap)).toString();
  }

  /**
   * Signs the UTF-8 encoded representation of the given string with the MAC algorithm,
   * using the secret key shared between the parties.
   *
   * @return a Base64-encoded representation of the signature
   */
  private String sign(String baseString) {
    SecretKey key = new SecretKeySpec(ServerStringUtils.stringToBytesUtf8(secretKey), macAlgorithm);
    Mac mac = null;
    try {
      mac = Mac.getInstance(macAlgorithm);
      mac.init(key);
    }
    catch (NoSuchAlgorithmException | InvalidKeyException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    byte[] text = ServerStringUtils.stringToBytesUtf8(baseString);
    return ServerStringUtils.bytesToStringUtf8(Base64.encodeBase64(mac.doFinal(text)));
  }

  public String getPublicKey() {
    return publicKey;
  }

  protected String getRequiredParam(HttpServletRequest request, String paramName) {
    String dateParam = request.getParameter(paramName);
    if (StringUtils.isBlank(dateParam))
      throw new SecurityException("Request missing parameter '" + paramName + "'");
    return dateParam;
  }

  public static void main(String[] args) {
    // generates a public/private keypair
    byte[] randomPubKey = new byte[6];  // keep this short, since it will be sent with every request (there are no serious security implications here; it needs to be only long enough to distinguish clients apart)
    byte[] randomPrivKey = new byte[33]; // we want to use at least 256 bits for the private key (which will be used in the keyed hmacSha1 computation), just to be safe; should be secure enough according to this chart: http://www.keylength.com/en/compare/
    // NOTE: the the reason we have numbers of bytes above divisible by 3 is to avoid the extra padding characters that Base64 encoding tacks on when the number of bytes is not divisible by 3 
    // we'll use 2 different self-seeded instances of SecureRandom, just so that there is no dependency between the pub key and the priv key 
    Duration duration = new Duration("Generating secure random keys");
    new SecureRandom().nextBytes(randomPubKey);
    new SecureRandom().nextBytes(randomPrivKey);
    System.out.println(duration);
    System.out.println("New randomly-generated credentials:");
    System.out.println("1) Public Key: " + ServerStringUtils.bytesToStringUtf8(UrlSafeBase64.encodeBase64(randomPubKey)));
    System.out.println("2) Private Key: " + ServerStringUtils.bytesToStringUtf8(UrlSafeBase64.encodeBase64(randomPrivKey)));
  }
}
