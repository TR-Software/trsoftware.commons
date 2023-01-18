package solutions.trsoftware.commons.server.testutil.websocket;

import com.google.common.util.concurrent.Futures;

import javax.annotation.Nonnull;
import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * @author Alex
 * @since 11/21/2022
 */
public class AsyncRemoteStub extends RemoteEndpointStub implements RemoteEndpoint.Async {

  private long timeoutMillis;

  @Override
  public long getSendTimeout() {
    return timeoutMillis;
  }

  @Override
  public void setSendTimeout(long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }

  @Override
  public Future<Void> sendText(String text) {
    return sendMessageFuture(new TextMessageImpl(text));
  }

  @Override
  public void sendText(String text, SendHandler handler) {
    sendMessageHandler(new TextMessageImpl(text), handler);
  }

  @Override
  public Future<Void> sendBinary(ByteBuffer data) {
    return sendMessageFuture(new BinaryMessageImpl(data));
  }

  @Override
  public void sendBinary(ByteBuffer data, SendHandler handler) {
    sendMessageHandler(new BinaryMessageImpl(data), handler);
  }

  @Override
  public Future<Void> sendObject(Object data) {
    return sendMessageFuture(new ObjectMessage(data));
  }

  @Override
  public void sendObject(Object data, SendHandler handler) {
    sendMessageHandler(new ObjectMessage(data), handler);
  }

  /**
   * Immediately sends the given message and invokes {@link SendHandler#onResult(SendResult)} with an "OK" result
   */
  protected void sendMessageHandler(Message<?> message, SendHandler handler) {
    sendMessage(message);
    handler.onResult(new SendResult());
  }

  /**
   * Immediately sends the given message and returns an immediate future.
   * @see Futures#immediateVoidFuture()
   */
  @Nonnull
  protected Future<Void> sendMessageFuture(Message<?> message) {
    sendMessage(message);
    return Futures.immediateFuture(null);
  }
}
