package solutions.trsoftware.commons.client.websocket;

import com.google.gwt.core.client.JavaScriptObject;

import javax.annotation.Nullable;

/**
 * JSO for a websocket <a href="https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent">CloseEvent</a>
 *
 * @author Alex, 7/28/2017
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API">WebSockets API Reference</a>
 */
public abstract class CloseEvent extends JavaScriptObject {

  /**
   * Enumerates the standard status codes for the WebSocket {@code CloseEvent.code} attribute.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent/code"><code>CloseEvent.code</code> on MDN</a>
   */
  public enum CloseCodes implements CloseCode {
    /**
     * The connection successfully completed the purpose for which it was created.
     */
    NORMAL_CLOSURE(1000),

    /**
     * The endpoint is going away, either because of a server failure or because the browser is navigating away from the page that opened the connection.
     * This code is also used for WS session timeouts.
     */
    GOING_AWAY(1001),

    /**
     * The endpoint is terminating the connection due to a protocol error.
     */
    PROTOCOL_ERROR(1002),

    /**
     * The connection is being terminated because the endpoint received data of a type it cannot accept. (For example, a text-only endpoint received binary data.)
     */
    UNSUPPORTED_DATA(1003),

    /**
     * Reserved. A meaning might be defined in the future.
     */
    RESERVED(1004),

    /**
     * Reserved. Indicates that no status code was provided even though one was expected.
     */
    NO_STATUS_RCVD(1005),

    /**
     * Reserved. Indicates that a connection was closed abnormally (that is, with no close frame being sent) when a status code is expected.
     */
    ABNORMAL_CLOSURE(1006),

    /**
     * The endpoint is terminating the connection because a message was received that contained inconsistent data (e.g., non-UTF-8 data within a text message).
     */
    INVALID_FRAME_PAYLOAD_DATA(1007),

    /**
     * The endpoint is terminating the connection because it received a message that violates its policy. This is a generic status code, used when codes 1003 and 1009 are not suitable.
     */
    POLICY_VIOLATION(1008),

    /**
     * The endpoint is terminating the connection because a data frame was received that is too large.
     */
    MESSAGE_TOO_BIG(1009),

    /**
     * The client is terminating the connection because it expected the server to negotiate one or more extension, but the server didn't.
     */
    MANDATORY_EXT(1010),

    /**
     * The server is terminating the connection because it encountered an unexpected condition that prevented it from fulfilling the request.
     */
    INTERNAL_ERROR(1011),

    /**
     * The server is terminating the connection because it is restarting.
     */
    SERVICE_RESTART(1012),

    /**
     * The server is terminating the connection due to a temporary condition, e.g. it is overloaded and is casting off some of its clients.
     */
    TRY_AGAIN_LATER(1013),

    /**
     * The server was acting as a gateway or proxy and received an invalid response from the upstream server. This is similar to 502 HTTP Status Code.
     */
    BAD_GATEWAY(1014),

    /**
     * Reserved. Indicates that the connection was closed due to a failure to perform a TLS handshake (e.g., the server certificate can't be verified).
     */
    TLS_HANDSHAKE(1015);

    /**
     * The {@code CloseEvent.code} value
     */
    public final int code;

    CloseCodes(int code) {
      this.code = code;
    }

    @Override
    public int getCode() {
      return code;
    }

    /**
     * @return the enum constant corresponding to the given {@code CloseEvent.code} value,
     * or {@code null} if not defined.
     */
    @Nullable
    public static CloseCodes valueOf(int code) {
      for (CloseCodes value : values()) {
        if (value.code == code)
          return value;
      }
      return null;
    }
  }


  protected CloseEvent() {
  }

  /**
   * @return the close reason code sent by the server
   */
  public final native int getCode() /*-{
    return this.code;
  }-*/;

  /**
   * @return the close reason sent by the server
   */
  public final native String getReason() /*-{
    return this.reason;
  }-*/;

  /**
   * @return whether or not the connection was cleanly closed
   */
  public final native boolean wasClean() /*-{
    return this.wasClean;
  }-*/;

  /**
   * Replacement for {@link #toString()}, since we can't override that method (because it's declared {@code final}).
   * @return string repr of this instance
   */
  public final String toDebugString() {
    final StringBuilder sb = new StringBuilder("CloseEvent{");
    sb.append("code=").append(getCode());
    sb.append(", reason='").append(getReason()).append('\'');
    sb.append(", wasClean=").append(wasClean());
    sb.append('}');
    return sb.toString();
  }

}
