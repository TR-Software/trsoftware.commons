package solutions.trsoftware.commons.server.servlet.gwt;

import solutions.trsoftware.commons.client.server.MockRpcService;
import solutions.trsoftware.commons.shared.util.Area2d;

/**
 * A simple RPC servlet that allows testing the functionality provided by {@link BaseRpcServlet}
 *
 * @author Alex
 * @since 7/29/2019
 */
public class MockRpcServiceServlet extends BaseRpcServlet implements MockRpcService {

  /**
   * Allows overriding the value of {@link BaseRpcServlet#maxContentLength}.
   *
   * Normally, it be initialized from a servlet {@code init-param} in {@code web.xml}, but instances of
   * {@link com.google.gwt.junit.client.GWTTestCase} don't have a way of doing that.
   */
  private long maxContentLength;

  /**
   * Allows examining the RPC payload of the current request.
   * Initialized by {@link #onBeforeRequestDeserialized(String)}.
   */
  private ThreadLocal<String> threadLocalRequestPayload = new ThreadLocal<>();

  @Override
  public String sayHello(String name) {
    return "Hello " + name;
  }

  @Override
  public Area2d enlargeArea(Area2d area, int width, int height) {
    return new Area2d(area.getWidth() + width, area.getHeight() + height);
  }

  /**
   * Echoes the RPC payload of this request.  This is a "meta" operation that allows the client-side app to determine
   * the size of the payload for a particular request, which is useful for testing {@link #setMaxContentLength}
   *
   * @return the RPC payload posted for this request
   */
  @Override
  public String echoRpcPayload(String arg) {
    return threadLocalRequestPayload.get();
  }

  @Override
  protected void onBeforeRequestDeserialized(String serializedRequest) {
    threadLocalRequestPayload.set(serializedRequest);
    System.out.printf("%s received a %d-char RPC payload:%n%s%n", getClass().getSimpleName(), serializedRequest.length(), serializedRequest);
    super.onBeforeRequestDeserialized(serializedRequest);
  }

  @Override
  protected void onAfterResponseSerialized(String serializedResponse) {
    System.out.printf("%s responding with a %d-char RPC payload:%n%s%n", getClass().getSimpleName(), serializedResponse.length(), serializedResponse);
    super.onAfterResponseSerialized(serializedResponse);
  }

  @Override
  protected void onRequestFinished() {
    super.onRequestFinished();
    threadLocalRequestPayload.remove();
  }

  @Override
  public long getMaxContentLength() {
    return maxContentLength;
  }

  @Override
  public void setMaxContentLength(long maxContentLength) {
    this.maxContentLength = maxContentLength;
  }
}
