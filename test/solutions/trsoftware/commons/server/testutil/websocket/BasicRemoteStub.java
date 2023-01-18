package solutions.trsoftware.commons.server.testutil.websocket;

import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;

/**
 * @author Alex
 * @since 11/21/2022
 */
public class BasicRemoteStub extends RemoteEndpointStub implements RemoteEndpoint.Basic {

  @Override
  public void sendText(String text) throws IOException {
    sendMessage(new TextMessageImpl(text));
  }

  @Override
  public void sendBinary(ByteBuffer data) throws IOException {
    sendMessage(new BinaryMessageImpl(data));
  }

  @Override
  public void sendText(String fragment, boolean isLast) throws IOException {
    sendMessage(new PartialTextMessage(fragment, isLast));
  }

  @Override
  public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {
    sendMessage(new PartialBinaryMessage(partialByte, isLast));
  }

  @Override
  public void sendObject(Object data) throws IOException, EncodeException {
    sendMessage(new ObjectMessage(data));
  }

  @Override
  public OutputStream getSendStream() throws IOException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.testutil.websocket.RemoteEndpointStub.BasicRemoteStub.getSendStream has not been fully implemented yet.");
  }

  @Override
  public Writer getSendWriter() throws IOException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.testutil.websocket.RemoteEndpointStub.BasicRemoteStub.getSendWriter has not been fully implemented yet.");
  }
}
