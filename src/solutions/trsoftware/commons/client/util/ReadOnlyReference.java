package solutions.trsoftware.commons.client.util;

/**
 * A simple immutable container (i.e. box) for any object reference that cannot be modified
 * after being set.  Similar to a final field, with the only difference being
 * that the value can be initialized at any time.
 *
 *
 * @author Alex
 */
public final class ReadOnlyReference<T> {

  private volatile T referent;

  public ReadOnlyReference() {
  }

  public ReadOnlyReference(T referent) {
    this.referent = referent;
  }

  /**
   * @throws IllegalStateException if the value has already been set (!= null)
   */
  public synchronized void set(T newReferent) throws IllegalStateException {
    if (referent != null)
      throw new IllegalStateException("Attempt to modify a ReadOnlyReference");
    referent = newReferent;
  }

  public T get() {
    return referent;
  }
}