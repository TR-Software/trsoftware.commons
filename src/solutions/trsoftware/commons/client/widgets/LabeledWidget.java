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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LabelBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * An {@link InlineFlowPanel} containing a label and a widget.
 *
 * @param <T> the type of the child widget
 * @author Alex
 * @since 12/23/2017
 */
public class LabeledWidget<T extends Widget> extends InlineFlowPanel {
  private LabelBase label;
  private T widget;

  public LabeledWidget(LabelBase label, T widget) {
    add(this.label = label);
    add(this.widget = widget);
  }

  public LabeledWidget(String label, T widget) {
    this(new InlineLabel(label), widget);
  }


}
