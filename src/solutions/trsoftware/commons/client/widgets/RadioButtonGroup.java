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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * Encapsulates the UI logic for selecting a value from a finite set of possibilities using {@link RadioButton} widgets.
 * This class creates a radio button for each value in the set, and represents the selection state of the group
 * (which can be obtained by calling {@link #getValue()}.
 *
 * NOTE: This class is layout-agnostic, so in order to actually display the radio buttons, call {@link #get(Object)}
 * for each value in {@link #valueSet} and add the returned widgets to a {@link Panel}.
 *
 * @param <V> The value type of this radio group's value set.
 * @author Alex, 7/5/2016
 */
public class RadioButtonGroup<V> implements HasValue<V>, ClickHandler, KeyPressHandler {

  /**
   * Where these radio buttons will ultimately reside - we only use this widget to implement event handling support.
   * @see #addValueChangeHandler(ValueChangeHandler)
   * @see #fireEvent(GwtEvent)
   */
  private final Widget parent;
  /** The set of values over which the radio buttons operate (allow selecting one of them) */
  private final Set<V> valueSet;
  /** The current value of this radio group (selected from {@link #valueSet} */
  private V value;

  private Map<V, RadioButton> rbMap = new LinkedHashMap<V, RadioButton>();

  public RadioButtonGroup(Widget parent, Set<V> valueSet) {
    this.parent = parent;
    this.valueSet = valueSet;
    createRadioButtons();
  }

  private void createRadioButtons() {
    String radioButtonGroupId = HTMLPanel.createUniqueId();
    for (V val : valueSet) {
      RadioButton rb = new RadioButton(radioButtonGroupId, " " + val);
      /*
        NOTE: technically, RadioButton provides an addValueChangeHandler(ValueChangeHandler<Boolean>) method,
        but its implementation is buggy - for instance the value change is only fired in response to clicks
        (but not keyboard events), so instead of a ValueChangeHandler, we add both a ClickHandler and
        a KeyPressHandler to each RadioButton widget, and determine the new value of the group when handling those events
        TODO: report this GWT bug (and maybe submit a patch)
       */

      rb.addClickHandler(this);
      rb.addKeyPressHandler(this);
      rbMap.put(val, rb);
    }
  }

  /**
   * @return the radio button that represents the given value.
   */
  public RadioButton get(V key) {
    return rbMap.get(key);
  }

  private void maybeUpdateValue() {
    V newValue = value;
    for (V val : valueSet) {
      if (get(val).getValue()) {
        newValue = val;
        break;
      }
    }
    setValue(newValue, true);
  }

  @Override
  public V getValue() {
    return value;
  }

  @Override
  public void setValue(V value) {
    setValue(value, false);
  }

  @Override
  public void setValue(V value, boolean fireEvents) {
    assert valueSet.contains(value);
    V oldValue = this.value;
    this.value = value;
    // make sure the radio buttons reflect this value
    RadioButton rb = get(value);
    if (!rb.getValue())
      rb.setValue(true);
    if (fireEvents)
      ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<V> handler) {
    return parent.addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    parent.fireEvent(event);
  }

  @Override
  public void onKeyPress(KeyPressEvent event) {
    maybeUpdateValue();
  }

  @Override
  public void onClick(ClickEvent event) {
    maybeUpdateValue();
  }
}
