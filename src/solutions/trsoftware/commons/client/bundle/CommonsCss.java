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

package solutions.trsoftware.commons.client.bundle;

import com.google.gwt.resources.client.CssResource;

/**
 * @author Alex
 * @since 12/27/2017
 */
public interface CommonsCss extends CssResource {

  CommonsCss INSTANCE = CommonsClientBundle.INSTANCE.css();

  @SuppressWarnings("GwtCssResourceErrors")
  static CommonsCss get() {
    return CommonsClientBundle.INSTANCE.css();
  }

  // TODO(12/18/2024): replace usages of CommonsClientBundleFactory.INSTANCE.getCss() with CommonsCss.get()

  String timeDisplay();

  String SoftModalDialogBox();

  String dialogMessage();

  String xShape();

  @ClassName("gwt-TabBarItem-selected")
  String gwtTabBarItemSelected();

  String ImageButton();

  String ImageButtonWithText();

  String CaptionWithIcon();

  String dialogInput();

  @ClassName("time-flashOn")
  String timeFlashOn();

  String dialogButtons();

  String bodyWidgetHolder();

  @ClassName("gwt-Button")
  String gwtButton();

  String loadingMessage();

  @ClassName("gwt-PopupPanelGlass")
  String gwtPopupPanelGlass();

  String DialogBox();

  @ClassName("gwt-TabPanelBottom")
  String gwtTabPanelBottom();

  @ClassName("gwt-TabBarItem")
  String gwtTabBarItem();

  String ModalPromptPopup();

  String fieldErrorMsg();

  String ErrorMessagePopup();

  String trPopupDialog();

  String CustomActionCell();

  String Caption();

  String DefaultCaption();

  String dialogContent();

  String xButton();

  @ClassName("gwt-TabPanel")
  String gwtTabPanel();

  String BasicInputForm();

  @ClassName("gwt-TabBar")
  String gwtTabBar();

  String time();

  String flashOn();

  String timeDisplayCaption();

  String PopupGlassSvg();

  String glassBackground();

  String contentSection();
}
