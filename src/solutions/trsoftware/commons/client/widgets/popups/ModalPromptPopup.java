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

package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;

import javax.annotation.Nonnull;

/**
 * @author Alex
 * @since 12/8/2017
 */
public class ModalPromptPopup extends PopupDialog implements ClickHandler {

  public static final String STYLE_NAME = CommonsClientBundleFactory.INSTANCE.getCss().ModalPromptPopup();

  public ModalPromptPopup(AbstractImagePrototype icon, String headingText, String bodyText, String closeLinkText, boolean glass) {
    super(false, icon, headingText, STYLE_NAME, closeLinkText);
    setModal(true);
    setGlassEnabled(glass);
    FlowPanel pnlBody = new FlowPanel();
    if (bodyText != null)
      pnlBody.add(new Label(bodyText));
    super.setBodyWidget(pnlBody);
  }

  @Override
  public FlowPanel getBodyWidget() {
    return (FlowPanel)super.getBodyWidget();
  }

  @Override
  public void setBodyWidget(@Nonnull Widget bodyWidget) {
    throw new UnsupportedOperationException(STYLE_NAME + " bodyWidget set by constructor");
  }

  public ModalPromptPopup addBodyWidget(Widget widget) {
    getBodyWidget().add(widget);
    return this;
  }

  /**
   * Adds a button to the popup body, and attaches a {@link ClickHandler} to hide this popup
   * after the button was clicked (and presumably after its other click handlers have executed).
   * @return {@code this}, for method chaining
   */
  public ModalPromptPopup addResponseButton(Button btn) {
    getBodyWidget().add(btn);
    btn.addClickHandler(this); // hide popup after response handled
    return this;
  }

  @Override
  public void onClick(ClickEvent event) {
    hide();
  }
}
