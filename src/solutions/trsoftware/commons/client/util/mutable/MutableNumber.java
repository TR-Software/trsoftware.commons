package solutions.trsoftware.commons.client.util.mutable;

import solutions.trsoftware.commons.client.util.stats.Mergeable;

/**
 * Superclass for GWT-compatible imitations of {@link java.util.concurrent}'s {@code Atomic*} classes
 * and {@link org.apache.commons.lang3.mutable}'s {@code Mutable*} classes.
 *
 * @author Alex
 */
public abstract class MutableNumber extends Number implements Mergeable<MutableNumber> {

  /** Returns the value of this number as a primitive wrapper type */
  public abstract Number toPrimitive();

  @Override
  public final String toString() {
    // WARNING: do not change this - it's important that the toString method
    // match that of other Number subclasses, otherwise bizarre things could happen,
    // like issues JSON serialization and the like
    return toPrimitive().toString();
  }


  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof MutableNumber
        && toPrimitive().equals(((MutableNumber)obj).toPrimitive());
  }
}
