package solutions.trsoftware.commons.client.util;

/**
 * A simple container (i.e. box) for an instance of Destroyable.  When the
 * set method is called and there was another instance stored in this box,
 * this class will call the destroy() method on the old instance. 
 *
 * @author Alex
 */
public final class DestroyableReference<T extends Destroyable> {

  private T referent;

  public void set(T newReferent) {
    if (referent != null)
      referent.destroy();
    referent = newReferent;
  }

  public T get() {
    return referent;
  }
}