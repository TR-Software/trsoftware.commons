package solutions.trsoftware.commons.client.websocket;

import com.google.gwt.dom.client.NativeEvent;

/**
 * @author Alex, 7/28/2017
 */
public abstract class WebSocketClient {

  private WebSocket webSocket;

  public WebSocketClient(String endpointUrl) {
    webSocket = WebSocket.create(endpointUrl);
    webSocket.addHandlers(this);
  }

  public void send(String message) {
    webSocket.send(message);
  }

  public void close() {
    webSocket.close();
  }

  public abstract void onOpen();
  public abstract void onClose(CloseEvent closeEvent);
  public abstract void onMessage(String message);
  public abstract void onError(NativeEvent event);
}
