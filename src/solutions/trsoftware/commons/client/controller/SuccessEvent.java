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
 * Fired from {@link BaseRpcAction#onSuccess(Object)}.
 *
 * Encapsulates the RPC return value, which can be obtained by calling {@link #getResult()}.
 * The action instance that fired this event can be obtained by calling {@link #getSource()}
 *
 * @param <T> the return type of the RPC call
 *
 * @see AsyncCallback#onSuccess(Object)
 *
 * @author Alex
 * @since 2/14/2018
 */
public class SuccessEvent<T> extends RpcEvent<SuccessEvent.Handler> {

  /**
   * Handles the {@link SuccessEvent} fired by {@link BaseRpcAction#onSuccess(Object)}
   *
   * @param <T> the return type of the RPC call
   */
  interface Handler<T> extends RpcEvent.Handler {
    void onSuccess(SuccessEvent<T> event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  private T result;

  public SuccessEvent(T result) {
    this.result = result;
  }

  /**
   * @return the value received by {@link BaseRpcAction#onSuccess(Object)}
   * @see AsyncCallback#onSuccess(Object)
   */
  public T getResult() {
    return result;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void dispatch(Handler handler) {
    handler.onSuccess(this);
  }

}
