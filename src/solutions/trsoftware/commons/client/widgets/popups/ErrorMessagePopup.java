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

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;
import solutions.trsoftware.commons.client.images.CommonsImages;

import javax.annotation.Nullable;

/**
 * @author Alex, 9/26/2017
 */
public class ErrorMessagePopup extends PopupDialog {

  public ErrorMessagePopup(boolean autoHide, String headingText) {
    this(autoHide, headingText, null);
  }

  public ErrorMessagePopup(boolean autoHide, String headingText, @Nullable Widget bodyWidget) {
    super(autoHide, AbstractImagePrototype.create(CommonsImages.INSTANCE.warn24()), headingText, CommonsClientBundleFactory.INSTANCE.getCss().ErrorMessagePopup(), bodyWidget);
  }

  @Override
  protected String getSecondaryStyleName() {
    return super.getSecondaryStyleName() + " ErrorMessagePopup";
  }
}
