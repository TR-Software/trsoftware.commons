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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.LegacyHandlerWrapper;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;
import solutions.trsoftware.commons.client.event.MultiHandlerRegistration;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;


/**
 * Encapsulates the UI logic for selecting a value from a finite set of possibilities using {@link RadioButton} widgets.
 * This class creates a radio button for each value in the set, and provides a single accessor for the selection state
 * of the entire group with {@link #getValue()}.
 * <p>
 * <b>Note</b>: This class is layout-agnostic, so in order to actually display the radio buttons,
 * call {@link #getButton(Object)} for each value in {@link #valueSet} and add the returned widgets to a {@link Panel}.
 *
 * @param <V> The value type returned by {@link #getValue()} and {@link ValueChangeEvent#getValue()}
 * @author Alex, 7/5/2016
 */
public class RadioButtonGroup<V> implements HasValue<V> {

  /**
   * The {@code name} attribute of each {@linkplain RadioButton radio button} in this group.
   */
  private final String name;
  /** The set of values over which the radio buttons operate (allow selecting one of them) */
  private final Set<V> valueSet;
  /**
   * The current value of this radio group: either an item selected from {@link #valueSet},
   * or {@code null} if none of the radio buttons have been selected.
   */
  @Nullable
  private V value;

  private final ImmutableMap<V, RadioButton> rbMap;

  private final EventBus eventBus = new SimpleEventBus();

  /**
   * Creates a set of {@linkplain RadioButton radio buttons} corresponding to the values in the given set,
   * with each button having a {@code <label>} generated from its value's {@link Object#toString toString} representation.
   * <p>
   * The generated buttons will not be automatically attached to any widget
   * (that last step is left up to the caller's discretion).
   * They can be accessed via {@link #getButton(Object)} or {@link #getButtons()}.
   *
   * @param valueSet the set of values over which the radio buttons operate:
   *     each button will select a unique value from this set
   */
  public RadioButtonGroup(Set<V> valueSet) {
    this(valueSet, Objects::toString);
  }

  /**
   * @deprecated Use {@link #RadioButtonGroup(Set, Function)}
   */
  public RadioButtonGroup(Set<V> valueSet, Renderer<V> labelRenderer) {
    this(createUniqueId("RadioButtonGroup"), valueSet, labelRenderer);
  }

  /**
   * Creates a set of {@linkplain RadioButton radio buttons} corresponding to the values in the given set.
   * The generated buttons can be accessed via {@link #getButton(Object)} or {@link #getButtons()}.
   * They will not be automatically attached to any widget (that last step is left up to the caller's discretion).
   *
   * @param valueSet the set of values over which the radio buttons operate:
   *     each button will select a unique value from this set
   * @param labelRenderer if not {@code null}, will be used to produce a {@code <label>} for each button
   *     (Note: the rendered string will be interpreted as HTML); if {@code null}, the labels will be omitted
   */
  public RadioButtonGroup(Set<V> valueSet, Function<V, String> labelRenderer) {
    this(createUniqueId("RadioButtonGroup"), valueSet, labelRenderer);
  }

  /**
   * @deprecated Use {@link #RadioButtonGroup(String, Set, Function)}
   */
  public RadioButtonGroup(String name, Set<V> valueSet, Renderer<V> labelRenderer) {
    this(name, valueSet, labelRenderer != null ? labelRenderer::render : null);
  }

  /**
   * Creates a set of {@linkplain RadioButton radio buttons} corresponding to the values in the given set,
   * with each button having a {@code <label>} generated from its value's {@link Object#toString toString} representation.
   * <p>
   * The generated buttons can be accessed via {@link #getButton(Object)} or {@link #getButtons()}.
   * They will not be automatically attached to any widget (that last step is left up to the caller's discretion).
   *
   * @param name a unique group name with which to associate each radio button
   *     (i.e. the {@code name} attribute for each {@code <input type="radio">} element);
   *     see {@link #createUniqueId(String)}
   * @param valueSet the set of values over which the radio buttons operate:
   *     each button will select a unique value from this set
   */
  public RadioButtonGroup(String name, Set<V> valueSet) {
    this(name, valueSet, (Function<V, String>)null);
  }

  /**
   * Creates a set of {@linkplain RadioButton radio buttons} corresponding to the values in the given set.
   * The generated buttons can be accessed via {@link #getButton(Object)} or {@link #getButtons()}.
   * They will not be automatically attached to any widget (that last step is left up to the caller's discretion).
   *
   * @param name a unique group name with which to associate each radio button
   *     (i.e. the {@code name} attribute for each {@code <input type="radio">} element);
   *     see {@link #createUniqueId(String)}
   * @param valueSet the set of values over which the radio buttons operate:
   *     each button will select a unique value from this set
   * @param labelRenderer will be used to produce a {@code <label>} for each button based on the corresponding value
   *     (Note: the rendered string will be interpreted as HTML); if {@code null}, the labels will be omitted
   */
  public RadioButtonGroup(String name, Set<V> valueSet, Function<V, String> labelRenderer) {
    this.name = name;
    this.valueSet = valueSet;
    ImmutableMap.Builder<V, RadioButton> rbMapBuilder = ImmutableMap.builder();
    for (V val : valueSet) {
      RadioButton rb = labelRenderer != null
          ? new RadioButton(name, labelRenderer.apply(val), true)
          : new RadioButton(name);
      /*
        NOTE: technically, RadioButton provides an addValueChangeHandler(ValueChangeHandler<Boolean>) method,
        but its implementation is buggy - for instance the value change is only fired in response to clicks
        (but not keyboard events), so in addition to of a ValueChangeHandler, we add both a ClickHandler and
        a KeyPressHandler to each RadioButton widget, and determine the new value of the group when handling those events
        TODO: report this GWT bug (and maybe submit a patch)
       */
      new RadioButtonHandler().addTo(rb);
      rbMapBuilder.put(val, rb);
    }
    rbMap = rbMapBuilder.build();
  }

  public static String createUniqueId(String prefix) {
    return prefix + "-" + HTMLPanel.createUniqueId();
  }

  public String getName() {
    return name;
  }

  /**
   * @return the radio button that selects the given value
   * @deprecated Use {@link #getButton(Object)} for better clarity (to distinguish from {@link #getValue()})
   */
  public RadioButton get(V key) {
    return getButton(key);
  }

  /**
   * @return the radio button that selects the given value
   */
  public RadioButton getButton(V key) {
    return rbMap.get(key);
  }

  /**
   * @return all the buttons comprising this group, mapped by the corresponding values
   */
  public ImmutableMap<V, RadioButton> getButtons() {
    return rbMap;
  }

  /**
   * Updates this group's {@link #value} depending on which {@link RadioButton} is currently selected.
   * @param sourceEvent an event on one of the {@link RadioButton}'s that might have caused its value to change
   */
  private void maybeUpdateValue(GwtEvent<?> sourceEvent) {
    V newValue = null;
    for (V val : valueSet) {
      if (getButton(val).getValue()) {
        newValue = val;
        break;
      }
    }
    setValue(newValue, true, sourceEvent);
  }

  /**
   * Returns the current value of this radio group: either an item from {@link #valueSet} corresponding to the
   * selected {@link RadioButton}, or {@code null} if none of the radio buttons have been selected.
   */
  @Override
  @Nullable
  public V getValue() {
    return value;
  }

  /**
   * Sets this {@link RadioButtonGroup}'s value without firing any events.
   * If the given value is non-null, the {@link RadioButton} corresponding to this value will become selected, otherwise
   * (if null), all the radio buttons in the group will be un-selected.
   *
   * @param value the new value
   * @see #setValue(Object, boolean)
   */
  @Override
  public void setValue(@Nullable V value) {
    setValue(value, false);
  }

  /**
   * Sets this {@link RadioButtonGroup}'s value, and fires a {@link RadioValueChangeEvent} when {@code fireEvents}
   * is true and the new value does not equal the existing value.
   * <p>
   * <b>Note:</b> the actual event fired by this implementation is a {@link RadioValueChangeEvent}, which
   * extends {@link ValueChangeEvent} to provide additional information about what triggered the change.
   * It can be handled by any {@link ValueChangeHandler} added via {@link #addValueChangeHandler(ValueChangeHandler)}
   * (the {@link ValueChangeHandler#onValueChange(ValueChangeEvent)} argument can be downcast to {@link RadioValueChangeEvent}).
   *
   * @param value the object's new value
   * @param fireEvents fire {@link RadioValueChangeEvent} if true and value is new
   * @see #setValue(Object, boolean, GwtEvent)
   */
  @Override
  public void setValue(@Nullable V value, boolean fireEvents) {
    setValue(value, fireEvents, null);
  }

  /**
   * Sets this object's value.  Fires {@link RadioValueChangeEvent} when {@code fireEvents} is true
   * and the new value does not equal the existing value.
   *
   * @param value the object's new value
   * @param fireEvents fire {@link RadioValueChangeEvent} if true and value is new
   * @param sourceEvent the underlying event (e.g. {@link ClickEvent}, {@link KeyPressEvent}, etc.) that caused
   *     this {@link RadioButtonGroup}'s value to change
   * @see #setValue(Object, boolean)
   */
  protected void setValue(@Nullable V value, boolean fireEvents, @Nullable GwtEvent<?> sourceEvent) {
    Preconditions.checkArgument(value == null || valueSet.contains(value),
        "value (%s) must be null or one of %s", value, valueSet);
    V oldValue = this.value;
    this.value = value;
    // make sure the radio buttons reflect this value
    if (value != null) {
      RadioButton rb = getButton(value);
      if (!rb.getValue())
        rb.setValue(true);
    } else {
      // setting null value should un-select all the radio buttons
      rbMap.values().forEach(rb -> rb.setValue(false));
    }
    if (fireEvents && !Objects.equals(oldValue, value)) {
      RadioValueChangeEvent<V> event = new RadioValueChangeEvent<>(value, this, sourceEvent);
      eventBus.fireEvent(event);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("value", getValue())
        .toString();
  }

  @Override
  public com.google.gwt.event.shared.HandlerRegistration addValueChangeHandler(ValueChangeHandler<V> handler) {
    return new LegacyHandlerWrapper(eventBus.addHandler(ValueChangeEvent.getType(), handler));
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    eventBus.fireEvent(event);
  }

  /**
   * Updates the {@link RadioButtonGroup}'s {@linkplain #getValue() value} if needed, when a click, keypress,
   * or value change event is received by a {@link RadioButton}.
   */
  private class RadioButtonHandler implements KeyPressHandler, ClickHandler, ValueChangeHandler<Boolean> {
    @Override
    public void onKeyPress(KeyPressEvent event) {
      maybeUpdateValue(event);
    }

    @Override
    public void onClick(ClickEvent event) {
      maybeUpdateValue(event);
    }

    @Override
    public void onValueChange(ValueChangeEvent event) {
      maybeUpdateValue(event);
    }

    HandlerRegistration addTo(RadioButton rb) {
      return new MultiHandlerRegistration(
          rb.addClickHandler(this),
          rb.addKeyPressHandler(this),
          rb.addValueChangeHandler(this)
      );
    }
  }

  /**
   * Event fired by the {@link #setValue(Object, boolean)} method of a {@link RadioButtonGroup}.
   * Extends {@link ValueChangeEvent} to provide additional information about what triggered the value change.
   * <p>
   * This event is handled by a normal {@link ValueChangeHandler} whose
   * {@link ValueChangeHandler#onValueChange(ValueChangeEvent)} argument can be downcast to {@link RadioValueChangeEvent}.
   *
   * @param <V> value type of the {@link RadioButtonGroup}'s {@link #valueSet}
   */
  public static class RadioValueChangeEvent<V> extends ValueChangeEvent<V> {

    /**
     * The {@link RadioButtonGroup} whose value changed.
     */
    private final RadioButtonGroup<V> sourceGroup;

    /**
     * The underlying event (e.g. {@link ClickEvent}, {@link KeyPressEvent}, etc.) that caused the
     * {@link RadioButtonGroup}'s value to change.
     */
    @Nullable
    private final GwtEvent<?> cause;

    /**
     * @param value the new value
     * @param sourceGroup the {@link RadioButtonGroup} whose value changed.
     * @param cause the underlying event (e.g. {@link ClickEvent}, {@link KeyPressEvent}, etc.) that caused
     *     this {@link RadioButtonGroup}'s value to change.
     */
    protected RadioValueChangeEvent(V value, RadioButtonGroup<V> sourceGroup, @Nullable GwtEvent<?> cause) {
      super(value);
      this.sourceGroup = sourceGroup;
      this.cause = cause;
    }

    public RadioButtonGroup<V> getSourceGroup() {
      return sourceGroup;
    }

    @Nullable
    public GwtEvent<?> getCause() {
      return cause;
    }
  }

}
