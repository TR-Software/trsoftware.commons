/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
