package solutions.trsoftware.commons.client.controller;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;

/**
 * Base class for all events fired by {@link BaseRpcAction}.
 *
 * The action instance that fired this event can be obtained by calling {@link #getSource()}
 *
 * @author Alex
 * @since 2/14/2018
 */
public abstract class RpcEvent<H extends RpcEvent.Handler> extends Event<H> {

  protected interface Handler extends EventHandler { }

  @Override
  public BaseRpcAction getSource() {
    return (BaseRpcAction)super.getSource();
  }
}
