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

package solutions.trsoftware.commons.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A JSNI overlay type for a native JS error.
 *
 * Supports a subset of the methods & properties provided by the various browser implementations.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Error">Mozilla Error Object Reference</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/ie/dww52sbt(v=vs.94).aspx">MSDN Error Object Reference</a>
 * 
 * @since Mar 26, 2013
 * @author Alex
 */
public class JsError extends JavaScriptObject {

  // Overlay types always have protected, zero-arg constructors, because the object must have been instantiated in javascript
  protected JsError() { }

  public final native String getName() /*-{
    return this.name;
  }-*/;

  public final native String getMessage() /*-{
    return this.message;
  }-*/;

}
