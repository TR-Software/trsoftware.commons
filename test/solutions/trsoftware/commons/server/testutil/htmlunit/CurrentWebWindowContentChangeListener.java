/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.testutil.htmlunit;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindowAdapter;
import com.gargoylesoftware.htmlunit.WebWindowEvent;

/**
 * @author Alex
 * @since 5/9/2018
 */
public abstract class CurrentWebWindowContentChangeListener extends WebWindowAdapter {

  protected WebClient webClient;

  public CurrentWebWindowContentChangeListener(WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public final void webWindowContentChanged(WebWindowEvent event) {
    if (event.getWebWindow() == webClient.getCurrentWindow())
      onCurrentWindowContentChanged(event);
  }

  /**
   * Will be invoked by {@link #webWindowContentChanged(WebWindowEvent)} if the event pertains to the "current window"
   * of {@link #webClient}.
   * @param event the {@link WebWindowEvent#CHANGE} event
   */
  protected abstract void onCurrentWindowContentChanged(WebWindowEvent event);


}
