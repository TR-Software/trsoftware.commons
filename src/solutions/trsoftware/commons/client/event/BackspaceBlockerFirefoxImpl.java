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

package solutions.trsoftware.commons.client.event;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.user.client.Event;

/**
 * @author Alex
 * @since Jul 26, 2013
 */
public class BackspaceBlockerFirefoxImpl extends BackspaceBlocker {

  @Override
  protected boolean isKeyDownEvent(Event.NativePreviewEvent event) {
    // Some users are getting an error in firefox here: Logged JS Error (Firefox): "JavaScriptException: (Error) : Permission denied to access property 'type'" @ BackspaceBlocker.onPreviewNativeEvent(DOMImpl.java:164)
    // since I can't understand what's causing it; will just trap the exception and ignore the event
    try {
      return super.isKeyDownEvent(event);
    }
    catch (JavaScriptException ex) {
      return false;
    }
  }

}
