package solutions.trsoftware.commons.client.event;

/**
 * Date: Nov 14, 2007
 * Time: 9:52:44 PM
 *
 * @author Alex
 */
public abstract class DataChangeListener<T> {

  /**
   * References to the listener set that holds this listener - enforces the
   * restriction of each listener only belonging to one listener set.
   */
  private ListenerSet containingSet;

  public abstract void onChange(DataChangeEvent<T> event);

  void addedToSet(ListenerSet listenerSet) {
    if (containingSet != null && containingSet != listenerSet)
      throw new IllegalStateException("Listener already belongs to another ListenerSet.");
    containingSet = listenerSet;
  }

  void removedFromSet(ListenerSet listenerSet) {
    containingSet = null;
  }

  public ListenerSet getListenerSet() {
    return containingSet;
  }

  public void removeFromListenerSet() {
    if (containingSet != null)
      containingSet.remove(this);
  }
}
