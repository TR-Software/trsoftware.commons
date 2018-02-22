package solutions.trsoftware.commons.client.controller;

/**
 * Fired from {@link BaseRpcAction#execute()}, which makes the RPC call.
 *
 * The action instance that fired this event can be obtained by calling {@link #getSource()}
 *
 * @author Alex
 * @since 2/14/2018
 */
public class ExecuteEvent extends RpcEvent<ExecuteEvent.Handler> {

  public interface Handler extends RpcEvent.Handler {
    void onExecute(ExecuteEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onExecute(this);
  }


}
