package solutions.trsoftware.commons.client.server;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.server.servlet.gwt.BaseRpcServlet;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * A "full-stack" client-side test of the functionality provided by {@link BaseRpcServlet}.
 * <p>
 * Complements the lightweight unit tests in {@link solutions.trsoftware.commons.server.servlet.gwt.BaseRpcServletTest}
 *
 * @author Alex
 * @since 7/29/2019
 */
public class BaseRpcServletTest extends CommonsGwtTestCase {

  private MockRpcServiceAsync mockRpcServiceAsync;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    mockRpcServiceAsync = GWT.create(MockRpcService.class);
  }

  @Override
  protected void gwtTearDown() throws Exception {
    mockRpcServiceAsync = null;
    super.gwtTearDown();
  }

  /**
   * Tests that RPC calls will fail if their payload exceeds the limit specified by
   * {@link BaseRpcServlet#getMaxContentLength()}
   */
  public void testReadContent() throws Exception {
    String dummyString = "There is no pain, you are receding. A distant ship smoke on the horizon. You are only coming through in waves. Your lips move but I can't hear what you're saying. When I was a child, I had a fever. My hands felt just like two balloons. Now I've got that feeling once again. I can't explain you will not understand. This is not how I am.";
    delayTestFinish(2000);
    /*
    fire off a sequence of RPC calls:
      1) without having set maxContentLength on the servlet (the RPC call should succeed)
      2) set a maxContentLength
      3) a request with payload under the maxContentLength (should succeed)
      4) a request with payload exceeding the maxContentLength (should fail)
    */
    // 1) first do a request to find out the actual payload size of our dummyString, before having set maxContentLength on the servlet (the RPC call should succeed)
    new EchoRpcPayloadAction(dummyString) {
      @Override
      public void onSuccess(String result) {
        super.onSuccess(result);
        assertFalse(StringUtils.isBlank(result));
        int dummyPayloadLength = result.length();
        // 2) now set a maxContentLength
        int maxContentLength = dummyPayloadLength - 1;
        new SetMaxContentLengthAction(maxContentLength) {
          @Override
          public void onSuccess(Void result) {
            super.onSuccess(result);
            // 3) now do a request with payload with size <= maxContentLength (should succeed)
            new EchoRpcPayloadAction(dummyString.substring(1)) {
              @Override
              public void onSuccess(String result) {
                super.onSuccess(result);
                // 4) finally, attempt the original request again (it should fail this time because it exceeds the max length that we've just set)
                new EchoRpcPayloadAction(dummyString) {
                  @Override
                  public void onFailure(Throwable caught) {
                    if (caught instanceof StatusCodeException) {
                      // this is expected, just check the status code (we want it to be 413 ("Payload Too Large" or "Request Entity Too Large")
                      logFailure(" failed (as expected!)", caught);
                      StatusCodeException statusCodeException = (StatusCodeException)caught;
                      assertEquals(413, statusCodeException.getStatusCode());
                      finishTest();
                    }
                    else {
                      super.onFailure(caught);  // will re-throw as RuntimeException and fail the test
                    }
                  }
                  @Override
                  public void onSuccess(String result) {
                    super.onSuccess(result);
                    fail("Expected " + actionName + " to fail because RPC payload length should have exceeded the limit of "
                        + maxContentLength);
                  }
                }.execute();
              }
            }.execute();
          }
        }.execute();
      }
    }.execute();

  }

  /**
   * A simplified version of {@link solutions.trsoftware.commons.client.controller.BaseRpcAction}
   *
   * @param <T> the return type of the RPC method
   */
  private abstract class MockRpcServiceAction<T> implements AsyncCallback<T>, Command {

    protected final String actionName = getActionName();

    @Nonnull
    protected String getActionName() {
      Class<? extends MockRpcServiceAction> cls = getClass();
      String ret = cls.getSimpleName();
      if (!ret.isEmpty())
        return ret;
      else {
        // this is an anonymous class, so use the name of the superclass instead (the superclass cannot be anonymous, by definition)
        return cls.getSuperclass().getSimpleName();
      }
    }

    public MockRpcServiceAction() {
    }

    @Override
    public void onFailure(Throwable caught) {
      logFailure(caught);
      throw new RuntimeException(caught);
    }

    protected void logFailure(Throwable caught) {
      logFailure(" failed", caught);
    }

    protected void logFailure(String messageSuffix, Throwable caught) {
      getLogger().log(Level.WARNING, getDebugMessagePrefix(messageSuffix), caught);
    }

    @Override
    public void onSuccess(T result) {
      getLogger().info(getDebugMessagePrefix(" result: ") + result);
    }

    @Nonnull
    private String getDebugMessagePrefix(String messageSuffix) {
      return getName() + " invocation of " + actionName + messageSuffix;
    }
  }

  public class SetMaxContentLengthAction extends MockRpcServiceAction<Void> {
    private final long maxContentLength;

    public SetMaxContentLengthAction(long maxContentLength) {
      this.maxContentLength = maxContentLength;
    }

    @Override
    public void execute() {
      mockRpcServiceAsync.setMaxContentLength(maxContentLength, this);
    }
  }

  /**
   * @author Alex
   * @since 7/30/2019
   */
  private abstract class AbstractStringAction extends MockRpcServiceAction<String> {

    protected final String arg;

    public AbstractStringAction(String arg) {
      this.arg = arg;
    }

  }

  public class EchoRpcPayloadAction extends AbstractStringAction {
    public EchoRpcPayloadAction(String arg) {
      super(arg);
    }

    @Override
    public void execute() {
      mockRpcServiceAsync.echoRpcPayload(arg, this);
    }
  }


  public class SayHelloAction extends AbstractStringAction {

    public SayHelloAction(String arg) {
      super(arg);
    }

    @Override
    public void execute() {
      mockRpcServiceAsync.sayHello(arg, this);
    }
  }

}