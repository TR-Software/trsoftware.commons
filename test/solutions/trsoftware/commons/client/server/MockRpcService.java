package solutions.trsoftware.commons.client.server;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import solutions.trsoftware.commons.shared.util.Area2d;

/**
 * @author Alex
 * @since 7/29/2019
 */
@RemoteServiceRelativePath("mockRpcService")
public interface MockRpcService extends RemoteService {

  /**
   * A very simple method that takes a string and returns a string,
   * representing a very simple RPC payload.
   */
  String sayHello(String name);

  /**
   * This method tests a more complex RPC payload that contains a serializable object and 2 other args.
   *
   * @return an area that covers the given area plus the added width/height offsets
   */
  Area2d enlargeArea(Area2d area, int width, int height);

  /**
   * Echoes the RPC payload of this request.  This is a "meta" operation that allows the client-side app to determine
   * the size of the payload for a particular request, which is useful for testing {@link #setMaxContentLength}
   *
   * @return the RPC payload posted for this request
   */
  String echoRpcPayload(String arg);

  long getMaxContentLength();

  void setMaxContentLength(long maxContentLength);
}
