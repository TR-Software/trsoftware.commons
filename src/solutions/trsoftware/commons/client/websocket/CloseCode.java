package solutions.trsoftware.commons.client.websocket;

/**
 * A WebSocket close code.
 *
 * @author Alex
 * @since 11/19/2022
 * @see javax.websocket.CloseReason
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent/code"><code>CloseEvent.code</code> on MDN</a>
 */
public interface CloseCode {
  int getCode();
}
