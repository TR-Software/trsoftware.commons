/*
 * Copyright 2022 TR Software Inc.
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

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletRequest;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;

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
  @Slow
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
      AssertUtils.assertThrows(SecurityException.class, (Runnable)() -> auth.authenticateIncomingRequest(request));
    }
    {
      final DummyHttpServletRequest request = newSignedRequest();
      request.removeParameter(RequestAuth.PARAM_NAME_SIGNATURE);
      AssertUtils.assertThrows(SecurityException.class, (Runnable)() -> auth.authenticateIncomingRequest(request));
    }
    // should throw if either parameter is incorrect
    {
      final DummyHttpServletRequest request = newSignedRequest();
      request.replaceParameterValues(RequestAuth.PARAM_NAME_ACCESS_KEY, "bad value");
      AssertUtils.assertThrows(SecurityException.class, (Runnable)() -> auth.authenticateIncomingRequest(request));
    }
    {
      final DummyHttpServletRequest request = newSignedRequest();
      request.replaceParameterValues(RequestAuth.PARAM_NAME_SIGNATURE, "bad value");
      AssertUtils.assertThrows(SecurityException.class, (Runnable)() -> auth.authenticateIncomingRequest(request));
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