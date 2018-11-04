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

package solutions.trsoftware.commons.client.dom;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Allows dynamically adding a stylesheet to the DOM.
 * 
 * Jan 9, 2013
 * 
 * @author Alex
 */
public class DynamicStylesheet {
  private JavaScriptObject stylesheet = createDynamicStylesheet();


  public DynamicStylesheet() {
  }

  /** Appends the given css markup to the stylesheet object represented by this class */
  public void append(String css) {
    appendCssToStyleElement(stylesheet, css);
  }

  /**
   * Add a dynamic stylesheet element into the head of the page.
   * @return a reference to that style element (can be used for appending individual rules later)
   */
  // NOTE: this duplicates some code in dynamic_content.js
  private static native JavaScriptObject createDynamicStylesheet() /*-{
    // code borrowed from http://stackoverflow.com/questions/524696/how-to-create-a-style-tag-with-javascript
    var head = $wnd.document.getElementsByTagName('head')[0];
    var style = $wnd.document.createElement('style');
    style.type = 'text/css';
    head.appendChild(style);
    return style;
  }-*/;

  private static native void appendCssToStyleElement(JavaScriptObject style, String css) /*-{
    // code borrowed from http://stackoverflow.com/questions/524696/how-to-create-a-style-tag-with-javascript
    if (style.styleSheet) {
      style.styleSheet.cssText += css;
    } else {
      style.appendChild(document.createTextNode(css));
    }
  }-*/;


}
