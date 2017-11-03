package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.shared.util.TakesValue;

/**
 * A container for a value, useful for inside closures, where all references
 * must be final.
 *
 * @author Alex
 */
public class Box<T> implements TakesValue<T> {
  private T value;
  private boolean initialized;

  public Box() {
  }

  public Box(T value) {
    this.value = value;
    initialized = true;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
    initialized = true;
  }

  /**
   * Sets a new value and returns the old one.
   * @return the old {@link #value}
   */
  public T replaceValue(T value) {
    T oldValue = this.value;
    setValue(value);
    return oldValue;
  }

  public boolean hasValue() {
    return initialized;
  }
}
