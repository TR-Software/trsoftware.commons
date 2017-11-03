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
