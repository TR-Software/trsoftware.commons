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
package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import solutions.trsoftware.commons.shared.util.HtmlElementBuilder;

/**
 * A cell that renders an image and takes a delegate to perform actions on mouseUp and keyDown.
 *
 * @param <C> the type that this Cell represents
 */
public class ImgActionCell<C> extends CustomActionCell<C> {

  private final String imgSrc;

  /**
   * Construct a new {@link ImgActionCell}.
   *
   * @param imgSrc the image URL
   * @param delegate the delegate that will handle events
   */
  public ImgActionCell(String imgSrc, ActionCell.Delegate<C> delegate) {
    super(delegate);
    this.imgSrc = imgSrc;
  }

  @Override
  protected SafeHtml generateHtml() {
    return new SafeHtmlBuilder().appendHtmlConstant(
        new HtmlElementBuilder("img")
            .setAttribute("src", imgSrc)
            .setAttribute("style", "cursor: pointer;")
            .selfClosingTag()).toSafeHtml();
  }
}
