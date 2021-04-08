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
