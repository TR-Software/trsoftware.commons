package solutions.trsoftware.commons.server.auth;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides just the basic authentication using the signature and public key
 * parameters.  No replay attack prevention.
 * Feb 7, 2013
 *
 * @author Alex
 */
public class RequestAuthWithoutReplayDefense extends RequestAuth {
  
  public RequestAuthWithoutReplayDefense(String macAlogorithm, String publicKey, String secretKey) {
    super(macAlogorithm, publicKey, secretKey);
  }

  protected void preventReplayAttack(HttpServletRequest request) throws SecurityException {
    // this class doesn't do anything to prevent a replay attack
  }
}
