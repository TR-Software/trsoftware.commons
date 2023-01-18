package solutions.trsoftware.commons.client.websocket;

import com.google.gwt.core.client.JavaScriptObject;

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

  public static native WebSocket create(String url) /*-{
    return new WebSocket(url);
  }-*/;

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

}
