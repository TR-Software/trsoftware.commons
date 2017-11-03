package solutions.trsoftware.commons.client.util;

/**
 * A container (i.e. cache) for an instance that will be created only once
 * on first use.
 *
 * TODO: rename this class (and subclasses) to LazyReference
 *
 * @author Alex
 */
public abstract class LazyInitFactory<T> {
  protected volatile T value;

  public T get(boolean create) {
    if (value == null && create) {
      // lazy init using the double-checked locking paradigm
      synchronized (this) {
        if (value == null)
          value = create();
      }
    }
    return value;
  }

  public T get() {
    return get(true);
  }

  public boolean hasValue() {
    return value != null;
  }

  protected abstract T create();
}