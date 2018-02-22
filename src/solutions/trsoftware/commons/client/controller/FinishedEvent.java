package solutions.trsoftware.commons.client.controller;

/**
 * Fired from {@link BaseRpcAction#onFinished()}, which is invoked after the RPC call returns (either successfully or not).
 * This event will usually be preceded by a {@link SuccessEvent} or a {@link FailureEvent}.
 *
 * The action instance that fired this event can be obtained by calling {@link #getSource()}
 *
 * @author Alex
 * @since 2/14/2018
 */
public class FinishedEvent extends RpcEvent<FinishedEvent.Handler> {

  public interface Handler extends RpcEvent.Handler {
    void onFinished(FinishedEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onFinished(this);
  }


}
