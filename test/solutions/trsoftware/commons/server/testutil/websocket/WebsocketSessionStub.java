package solutions.trsoftware.commons.server.testutil.websocket;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Alex, 9/26/2017
 */
public class WebsocketSessionStub implements Session {

  private static final AtomicInteger nextId = new AtomicInteger();

  private Map<String, List<String>> requestParameterMap = new LinkedHashMap<>();
  private CloseReason closeReason;
  
  private RemoteEndpoint.Async asyncRemote;
  private RemoteEndpoint.Basic basicRemote;
  private URI requestURI;
  private String queryString = "";
  private String id;

  @Override
  public WebSocketContainer getContainer() {
    System.err.println("Method .getContainer has not been fully implemented yet.");
    return null;
  }

  @Override
  public void addMessageHandler(MessageHandler listener) throws IllegalStateException {
    System.err.println("Method .addMessageHandler has not been fully implemented yet.");

  }

  @Override
  public Set<MessageHandler> getMessageHandlers() {
    System.err.println("Method .getMessageHandlers has not been fully implemented yet.");
    return null;
  }

  @Override
  public void removeMessageHandler(MessageHandler listener) {
    System.err.println("Method .removeMessageHandler has not been fully implemented yet.");

  }

  @Override
  public String getProtocolVersion() {
    System.err.println("Method .getProtocolVersion has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getNegotiatedSubprotocol() {
    System.err.println("Method .getNegotiatedSubprotocol has not been fully implemented yet.");
    return null;
  }

  @Override
  public List<Extension> getNegotiatedExtensions() {
    System.err.println("Method .getNegotiatedExtensions has not been fully implemented yet.");
    return null;
  }

  @Override
  public boolean isSecure() {
    System.err.println("Method .isSecure has not been fully implemented yet.");
    return false;
  }

  @Override
  public boolean isOpen() {
    return closeReason == null;
  }

  @Override
  public long getMaxIdleTimeout() {
    System.err.println("Method .getMaxIdleTimeout has not been fully implemented yet.");
    return 0;
  }

  @Override
  public void setMaxIdleTimeout(long seconds) {
    System.err.println("Method .setMaxIdleTimeout has not been fully implemented yet.");

  }

  @Override
  public void setMaxBinaryMessageBufferSize(int max) {
    System.err.println("Method .setMaxBinaryMessageBufferSize has not been fully implemented yet.");

  }

  @Override
  public int getMaxBinaryMessageBufferSize() {
    System.err.println("Method .getMaxBinaryMessageBufferSize has not been fully implemented yet.");
    return 0;
  }

  @Override
  public void setMaxTextMessageBufferSize(int max) {
    System.err.println("Method .setMaxTextMessageBufferSize has not been fully implemented yet.");

  }

  @Override
  public int getMaxTextMessageBufferSize() {
    System.err.println("Method .getMaxTextMessageBufferSize has not been fully implemented yet.");
    return 0;
  }

  @Override
  public RemoteEndpoint.Async getAsyncRemote() {
    if (asyncRemote == null)
      asyncRemote = new AsyncRemoteStub();
    return asyncRemote;
  }

  public WebsocketSessionStub setAsyncRemote(RemoteEndpoint.Async asyncRemote) {
    this.asyncRemote = asyncRemote;
    return this;
  }

  @Override
  public RemoteEndpoint.Basic getBasicRemote() {
    if (basicRemote == null)
      basicRemote = new BasicRemoteStub();
    return basicRemote;
  }

  public WebsocketSessionStub setBasicRemote(RemoteEndpoint.Basic basicRemote) {
    this.basicRemote = basicRemote;
    return this;
  }

  @Override
  public String getId() {
    if (id == null)
      id = Integer.toString(nextId.incrementAndGet());
    return id;
  }

  @Override
  public void close() throws IOException {
    close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, ""));
  }

  @Override
  public void close(CloseReason closeStatus) throws IOException {
    closeReason = closeStatus;
  }

  public CloseReason getCloseReason() {
    return closeReason;
  }

  @Override
  public URI getRequestURI() {
    return requestURI;
  }

  public WebsocketSessionStub setRequestURI(URI requestURI) {
    this.requestURI = requestURI;
    return this;
  }

  @Override
  public Map<String, List<String>> getRequestParameterMap() {
    return requestParameterMap;
  }

  public WebsocketSessionStub setRequestParameterMap(Map<String, List<String>> requestParameterMap) {
    this.requestParameterMap = requestParameterMap;
    return this;
  }

  @Override
  public String getQueryString() {
    return queryString;
  }

  public WebsocketSessionStub setQueryString(String queryString) {
    this.queryString = queryString;
    return this;
  }

  @Override
  public Map<String, String> getPathParameters() {
    System.err.println("Method .getPathParameters has not been fully implemented yet.");
    return null;
  }

  @Override
  public Map<String, Object> getUserProperties() {
    System.err.println("Method .getUserProperties has not been fully implemented yet.");
    return null;
  }

  @Override
  public Principal getUserPrincipal() {
    System.err.println("Method .getUserPrincipal has not been fully implemented yet.");
    return null;
  }

  @Override
  public Set<Session> getOpenSessions() {
    System.err.println("Method .getOpenSessions has not been fully implemented yet.");
    return null;
  }

  @Override
  public <T> void addMessageHandler(Class<T> aClass, MessageHandler.Partial<T> partial) throws IllegalStateException {
    System.err.println("Method solutions.trsoftware.commons.server.testutil.websocket.WebsocketSessionStub.addMessageHandler has not been fully implemented yet.");

  }

  @Override
  public <T> void addMessageHandler(Class<T> aClass, MessageHandler.Whole<T> whole) throws IllegalStateException {
    System.err.println("Method solutions.trsoftware.commons.server.testutil.websocket.WebsocketSessionStub.addMessageHandler has not been fully implemented yet.");

  }
}
