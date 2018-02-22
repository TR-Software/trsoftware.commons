package solutions.trsoftware.commons.client.controller;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Fired from {@link BaseRpcAction#onFailure(Throwable)}.
 *
 * Encapsulates the exception thrown by the RPC call, which can be obtained by calling {@link #getReason()}.
 * The action instance that fired this event can be obtained by calling {@link #getSource()}
 *
 * @see AsyncCallback#onFailure(Throwable)
 *
 * @author Alex
 * @since 2/14/2018
 */
public class FailureEvent extends RpcEvent<FailureEvent.Handler> {

  private final Throwable reason;

  /**
   * Handles the {@link FailureEvent} fired by {@link BaseRpcAction#onFailure(Throwable)}
   */
  public interface Handler extends RpcEvent.Handler {
    void onFailure(FailureEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  public FailureEvent(Throwable reason) {
    this.reason = reason;
  }

  public Throwable getReason() {
    return reason;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onFailure(this);
  }


}
