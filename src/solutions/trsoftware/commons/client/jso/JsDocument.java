/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.jso;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Element;

/**
 * Overlays the native browser document object.
 *
 * @author Alex, 10/6/2015
 */
public class JsDocument extends JsObject {

  protected JsDocument() {
  }

  public static JsDocument get() {
    return Document.get().cast();
  }

  /**
   * @return {@code document.activeElement}, which is the currently focused element
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/activeElement#Browser_compatibility">Document.activeElement browser compatibility</a>
   */
  public final Element getActiveElement() {
    return (Element)getObject("activeElement");
  }

  /**
   * @return the result of invoking the JS DOM method <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/getSelection">document.getSelection()</a>
   * or {@code null} if the browser doesn't support this API.
   */
  public final native JsSelection getSelection()/*-{
    if (this.getSelection)
      return this.getSelection();
    return null;
  }-*/;
}
