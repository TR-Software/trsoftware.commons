package solutions.trsoftware.commons.client.websocket;

import com.google.gwt.core.client.JavaScriptObject;

import javax.annotation.Nullable;

/**
 * JSO for a <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket">WebSocket</a> that sends
 * and receives plain text messages.
 *
 * @author Alex, 7/28/2017
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API">WebSockets API Reference</a>
 */
public abstract class WebSocket extends JavaScriptObject {

  protected WebSocket() {
  }

  // constructor:

  public static native WebSocket create(String url) /*-{
    return new WebSocket(url);
  }-*/;

  // properties:

  /**
   * The {@code WebSocket.readyState} read-only property returns the current state of the WebSocket connection.
   * 
   * @return the value of {@code WebSocket.readyState}:
   *   {@code 0} (CONNECTING), {@code 1} (OPEN), {@code 2} (CLOSING), or {@code 3} (CLOSED)
   */
  public final native int getReadyStateInt() /*-{
    return this.readyState;
  }-*/;

  /**
   * @return the enum constant corresponding to the value returned by {@link #getReadyStateInt()}
   */
  @Nullable
  public final ReadyState getReadyState() {
    return ReadyState.valueOf(getReadyStateInt());
  }

  // methods:

  public final native void send(String message) /*-{
    this.send(message);
  }-*/;

  public final native void close() /*-{
    this.close();
  }-*/;


  public final native void addHandlers(WebSocketClient client) /*-{
    this.onopen = function (evt) {
      $entry(client.@solutions.trsoftware.commons.client.websocket.WebSocketClient::onOpen()());
    };
    this.onclose = function (evt) {
      $entry(client.@solutions.trsoftware.commons.client.websocket.WebSocketClient::onClose(*)(evt));
    };
    this.onmessage = function (evt) {
      $entry(client.@solutions.trsoftware.commons.client.websocket.WebSocketClient::onMessage(Ljava/lang/String;)(evt.data));
    };
    this.onerror = function (evt) {
      $wnd.console.error("WS.onerror: ", this, evt); // TODO: check if console.error method available
      $entry(client.@solutions.trsoftware.commons.client.websocket.WebSocketClient::onError(*)(evt));
    };
  }-*/;

  public enum ReadyState {
    CONNECTING, OPEN, CLOSING, CLOSED;

    /**
     * @param readyState the value of {@link WebSocket#getReadyStateInt()}
     * @return the enum constant corresponding to the given value, or {@code null} if none match
     */
    @Nullable
    public static ReadyState valueOf(int readyState) {
      ReadyState[] values = values();
      if (readyState >= 0 && readyState < values.length)
        return values[readyState];
      return null;  // unknown value
    }

  }

}
