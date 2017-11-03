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

package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;

/**
 * All dialogs in the web app should extend this class.  It simply ensures a consistent
 * look & feel by providing a shared CSS class name.
 *
 * @see #STYLE_NAME
 * @see #getSecondaryStyleName()
 *
 * @author Alex, 6/29/2016
 */
public class PopupDialog extends PopupWithIcon {

  /** Defines a consistent CSS class name to use for all dialog popup within our app. */
  public static final String STYLE_NAME = "trPopupDialog";

  @Override
  protected String getSecondaryStyleName() {
    return STYLE_NAME;
  }

  public PopupDialog(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName) {
    super(autoHide, icon, headingText, styleName);
  }

  public PopupDialog(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, String closeLinkText) {
    super(autoHide, icon, headingText, styleName, closeLinkText);
  }

  public PopupDialog(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, Widget bodyWidget) {
    super(autoHide, icon, headingText, styleName, bodyWidget);
  }

  public PopupDialog(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, Widget bodyWidget, String closeLinkText) {
    super(autoHide, icon, headingText, styleName, bodyWidget, closeLinkText);
  }

  public PopupDialog(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, String bodyText, String closeLinkText) {
    super(autoHide, icon, headingText, styleName, bodyText, closeLinkText);
  }
}
