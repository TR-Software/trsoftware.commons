package solutions.trsoftware.commons.server.testutil.websocket;

import solutions.trsoftware.commons.server.util.Clock;

import javax.websocket.RemoteEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * @author Alex
 * @since 11/21/2022
 */
public class RemoteEndpointStub implements RemoteEndpoint {

  private boolean batchingAllowed;

  protected final ArrayList<Message<?>> messages = new ArrayList<>();

  @Override
  public void setBatchingAllowed(boolean batchingAllowed) throws IOException {
    this.batchingAllowed = batchingAllowed;
  }

  @Override
  public boolean getBatchingAllowed() {
    return batchingAllowed;
  }

  @Override
  public void flushBatch() throws IOException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.testutil.websocket.RemoteEndpointStub.flushBatch has not been fully implemented yet.");
  }

  @Override
  public void sendPing(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
    sendMessage(new PingMessage(applicationData));
  }

  @Override
  public void sendPong(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
    sendMessage(new PongMessage(applicationData));
  }

  protected void sendMessage(Message<?> message) {
    messages.add(message);
  }

  public ArrayList<Message<?>> getMessages() {
    return messages;
  }

  public interface Message<T> {
    T getPayload();

    long getTimestamp();
  }

  public static abstract class MessageBase<T> implements Message<T> {
    protected T payload;
    private final long timestamp;

    public MessageBase(T payload) {
      this.payload = payload;
      timestamp = Clock.currentTimeMillis();
    }

    @Override
    public T getPayload() {
      return payload;
    }

    @Override
    public long getTimestamp() {
      return timestamp;
    }
  }

  public interface TextMessage extends Message<String> {}
  public interface BinaryMessage extends Message<ByteBuffer> {}


  public static class TextMessageImpl extends MessageBase<String> implements TextMessage {
    public TextMessageImpl(String payload) {
      super(payload);
    }
  }

  public static class BinaryMessageImpl extends MessageBase<ByteBuffer> implements BinaryMessage {
    public BinaryMessageImpl(ByteBuffer payload) {
      super(payload);
    }
  }

  /**
   * @see RemoteEndpoint.Basic#sendObject(Object)
   * @see RemoteEndpoint.Async#sendObject(Object)
   */
  public static class ObjectMessage extends MessageBase<Object> {
    public ObjectMessage(Object payload) {
      super(payload);
    }
  }
  
  public static class PingMessage extends BinaryMessageImpl {
    public PingMessage(ByteBuffer payload) {
      super(payload);
    }
  }
  
  public static class PongMessage extends BinaryMessageImpl {
    public PongMessage(ByteBuffer payload) {
      super(payload);
    }
  }

  public static class PartialMessage<T> extends MessageBase<T> {
    protected final boolean last;

    public PartialMessage(T payload, boolean isLast) {
      super(payload);
      last = isLast;
    }

    public boolean isLast() {
      return last;
    }
  }

  public static class PartialTextMessage extends PartialMessage<String> implements TextMessage {
    public PartialTextMessage(String payload, boolean isLast) {
      super(payload, isLast);
    }
  }

  public static class PartialBinaryMessage extends PartialMessage<ByteBuffer> implements BinaryMessage {
    public PartialBinaryMessage(ByteBuffer payload, boolean isLast) {
      super(payload, isLast);
    }
  }

}
