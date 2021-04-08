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

package solutions.trsoftware.commons.shared.util;

/**
 * A simple builder for CSS strings.  Can be used in conjunction with {@link HtmlBuilder}.
 * <p>
 * <i>Note</i>: newer versions of GWT now provide a more powerful implementation of this concept (see
 * {@link com.google.gwt.dom.builder.shared.HtmlStylesBuilder})
 *
 * @see HtmlBuilder#style(String)
 * @author Alex
 * @since 8/19/2018
 */
public class CssBuilder {

  private StringBuilder out = new StringBuilder();

  /** Will be inserted between declarations */
  private String spacer;

  /**
   * Constructs a {@link CssBuilder} which will separate successive declarations by a single space.
   * @see #CssBuilder(String)
   */
  public CssBuilder() {
    this(" ");
  }

  /**
   * Allows customizing the spacing between declarations.
   *
   * @param spacer will be inserted between successive declarations (e.g. {@code " "} or {@code "\n  "})
   */
  public CssBuilder(String spacer) {
    this.spacer = spacer;
  }

  /**
   * Appends a CSS declaration like {@code background-color: red;}
   *
   * @param property the CSS property to set (e.g. {@code background-color}, {@code font-size}, etc.)
   * @param value the value for the property (e.g. {@code red}, {@code 2em}, etc.)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Learn/CSS/Introduction_to_CSS/Syntax#CSS_declarations">CSS declarations (MDN)</a>
   */
  public CssBuilder append(String property, String value) {
    if (property == null || value == null)
      throw new IllegalArgumentException();
    startNewDecl();
    out.append(property).append(": ").append(value).append(';');
    return this;
  }

  private void startNewDecl() {
    if (out.length() > 0 && spacer != null)
      out.append(spacer);
  }

  @Override
  public String toString() {
    return out.toString();
  }
}
