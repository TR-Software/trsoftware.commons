package solutions.trsoftware.commons.client.util;

/**
 * Nov 15, 2010
 *
 * @author Alex
 */
public abstract class MutableLazyInitFactory<T> extends LazyInitFactory<T> {

  public synchronized void set(T value) {
    this.value = value;
  }
}
