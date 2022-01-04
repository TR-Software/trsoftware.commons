/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.client.dom;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Node;

/**
 * JSNI overlay for the native {@code AbstractRange} interface.
 *
 * <blockquote cite="https://developer.mozilla.org/en-US/docs/Web/API/AbstractRange">
 * The AbstractRange abstract interface is the base class upon which all DOM range types are defined. A range is an
 * object that indicates the start and end points of a section of content within the document.
 * </blockquote>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/AbstractRange">MDN Reference</a>
 * @see <a href="https://dom.spec.whatwg.org/#abstractrange">DOM Specification</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/AbstractRange#browser_compatibility">Browser compatibility</a>
 */
public abstract class JsAbstractRange extends JavaScriptObject {

  protected JsAbstractRange() {
  }

  /**
   * The DOM Node in which the beginning of the range, as specified by the {@link #startOffset} property, is located.
   *
   * @see #startOffset()
   */
  public final native Node startContainer() /*-{
    return this.startContainer;
  }-*/;

  /**
   * The offset (in characters) into the start node of the range's start position.
   *
   * @see #startContainer()
   */
  public final native int startOffset() /*-{
    return this.startOffset;
  }-*/;

  /**
   * The DOM Node in which the end of the range, as specified by the {@link #endOffset} property, is located.
   *
   * @see #endOffset()
   */
  public final native Node endContainer() /*-{
    return this.endContainer;
  }-*/;

  /**
   * The offset (in characters) into the end node of the range's end position.
   *
   * @see #endContainer()
   */
  public final native int endOffset() /*-{
    return this.endOffset;
  }-*/;

  /**
   * A boolean value which is {@code true} if the range is collapsed. A collapsed range is one whose start position and
   * end position are the same, resulting in a zero-character-long range.
   */
  public final native boolean collapsed() /*-{
    return this.collapsed;
  }-*/;
}
