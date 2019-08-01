package solutions.trsoftware.commons.client.server;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import solutions.trsoftware.commons.shared.util.Area2d;
import solutions.trsoftware.commons.shared.util.LazyReference;

public interface MockRpcServiceAsync {

  LazyReference<MockRpcServiceAsync> INSTANCE = new LazyReference<MockRpcServiceAsync>() {
    @Override
    protected MockRpcServiceAsync create() {
      return GWT.create(MockRpcService.class);
    }
  };

  /**
   * A very simple method that takes a string and returns a string,
   * representing a very simple RPC payload.
   */
  void sayHello(String name, AsyncCallback<String> async);

  /**
   * This method tests a more complex RPC payload that contains a serializable object and 2 other args.
   *
   * @return an area that covers the given area plus the added width/height offsets
   */
  void enlargeArea(Area2d area, int width, int height, AsyncCallback<Area2d> async);

  void getMaxContentLength(AsyncCallback<Long> async);

  void setMaxContentLength(long maxContentLength, AsyncCallback<Void> async);

  /**
   * Echoes the RPC payload of this request.  This is a "meta" operation that allows the client-side app to determine
   * the size of the payload for a particular request, which is useful for testing {@link #setMaxContentLength}
   *
   * @return the RPC payload posted for this request
   */
  void echoRpcPayload(String arg, AsyncCallback<String> async);
}
