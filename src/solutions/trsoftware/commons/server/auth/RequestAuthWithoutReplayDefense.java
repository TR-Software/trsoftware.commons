/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

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
