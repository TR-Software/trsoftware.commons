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

import com.google.gwt.user.client.ui.LabelBase;
import com.google.gwt.user.client.ui.Widget;

import static solutions.trsoftware.commons.client.widgets.Widgets.*;

/**
 * An {@link InlineFlowPanel} containing a label and a widget.
 *
 * @param <T> the type of the child widget
 * @author Alex
 * @since 12/23/2017
 */
public class LabeledWidget<T extends Widget> extends InlineFlowPanel {
  /* TODO(9/28/2026): maybe replace inheritance w/delegation (i.e. make this a Composite with a nested InlineFlowPanel)
       - this would allow passing a different panel type to use as a container (e.g. constructor param Supplier<? extends Panel>
   */

  private final LabelBase<?> label;
  private final T widget;

  /**
   * Places the label before widget
   */
  public LabeledWidget(LabelBase<?> label, T widget) {
    add(this.label = label);
    add(this.widget = widget);
  }

  /**
   * Places the label before widget
   */
  public LabeledWidget(String label, T widget) {
    this(inlineLabel(label), widget);
  }

  /**
   * Places the label after widget
   */
  public LabeledWidget(T widget, LabelBase<?> label) {
    add(this.widget = widget);
    add(this.label = label);
  }

  /**
   * Places the label after widget
   */
  public LabeledWidget(T widget, String label) {
    this(widget, inlineLabel(label));
  }

  public LabelBase<?> getLabel() {
    return label;
  }

  public T getWidget() {
    return widget;
  }
}
