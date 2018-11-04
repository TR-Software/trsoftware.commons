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

package solutions.trsoftware.commons.client.bridge;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.bridge.rpc.NumberFormatTestService;
import solutions.trsoftware.commons.client.bridge.rpc.NumberFormatTestServiceAsync;

/**
 * Dec 12, 2008
 *
 * @author Alex
 */
public class FloatSerializationTest extends CommonsGwtTestCase {
  NumberFormatTestServiceAsync serversideJavaFormatter;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    serversideJavaFormatter = GWT.create(NumberFormatTestService.class);
    ((ServiceDefTarget)serversideJavaFormatter).setServiceEntryPoint(GWT.getModuleBaseURL() + "numberFormatterServlet");
  }


  public void testFloats() throws Exception {
    delayTestFinish(10000);
    final int n = 1000;
    serversideJavaFormatter.generateFloats(n, new AsyncCallback<String[]>() {
      public void onFailure(Throwable caught) {
        fail(caught.getMessage());
      }
      public void onSuccess(String[] serverStrings) {
        assertEquals(n, serverStrings.length);
        String[] clientStrings = new String[n];
        for (int i = 0; i < n; i++) {
          clientStrings[i] = String.valueOf(Float.parseFloat(serverStrings[i]));
        }
        serversideJavaFormatter.checkFloats(clientStrings, new AsyncCallback<Boolean>() {
          public void onFailure(Throwable caught) {
            fail(caught.getMessage());
          }
          public void onSuccess(Boolean result) {
            assertTrue(result);
            finishTest();
          }
        });
      }
    });
  }

}
