package solutions.trsoftware.commons.shared.util.mutable;

import solutions.trsoftware.commons.shared.util.callables.Condition;

/**
 * GWT-compatible replacement for {@link java.util.concurrent.atomic.AtomicBoolean AtomicBoolean}
 * and {@code org.apache.commons.lang3.mutable.MutableBoolean}.
 *
 * @author Alex
 * @since 11/28/2017
 */
public class MutableBoolean implements Condition {

  private volatile boolean value;

  public MutableBoolean() {
  }

  public MutableBoolean(boolean value) {
    this.value = value;
  }

  public boolean get() {
    return value;
  }

  @Override
  public boolean check() {
    return get();
  }

  public void set(boolean value) {
    this.value = value;
  }
}
