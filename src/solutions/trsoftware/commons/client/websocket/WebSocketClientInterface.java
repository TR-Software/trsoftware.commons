package solutions.trsoftware.commons.client.websocket;

import com.google.gwt.dom.client.NativeEvent;

/**
 * Interface for a native {@link WebSocket}.
 *
 * @author Alex
 * @since 6/5/2024
 */
public interface WebSocketClientInterface {
  void send(String message);

  void close();

  boolean isOpen();

  void onOpen();

  void onClose(CloseEvent closeEvent);

  void onMessage(String message);

  void onError(NativeEvent event);
}
