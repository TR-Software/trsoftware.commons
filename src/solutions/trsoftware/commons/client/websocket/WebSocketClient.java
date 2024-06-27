package solutions.trsoftware.commons.client.websocket;

import com.google.common.annotations.VisibleForTesting;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * @author Alex, 7/28/2017
 */
public abstract class WebSocketClient implements WebSocketClientInterface {

  private final WebSocket webSocket;

  /**
   * Opens a new {@link WebSocket} with the given URL.
   */
  public WebSocketClient(String endpointUrl) {
    this(WebSocket.create(endpointUrl));
  }

  /**
   * Wraps the given {@link WebSocket} and re-binds its native event listener properties to the corresponding
   * methods of this class.
   * <p>
   * This constructor allows mocking the WS connection in unit tests.
   */
  @VisibleForTesting
  protected WebSocketClient(@Nonnull WebSocket webSocket) {
    this.webSocket = requireNonNull(webSocket, "WebSocket");
    webSocket.bindHandlers(this);
  }

  @Override
  public void send(String message) {
    webSocket.send(message);
  }

  @Override
  public void close() {
    webSocket.close();
  }

  @Override
  public boolean isOpen() {
    return webSocket.getReadyState() == WebSocket.ReadyState.OPEN;
  }

}
