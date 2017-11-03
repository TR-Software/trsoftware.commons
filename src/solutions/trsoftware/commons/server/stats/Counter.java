package solutions.trsoftware.commons.server.stats;

import solutions.trsoftware.commons.client.util.JsonBuilder;

/**
 * A named counter data type.
 *
 * @author Alex
 */
public abstract class Counter {
  protected final String name;

  public Counter(String name) {
    this.name = name;
  }

  /** @return The name of the counter. */
  public String getName() {
    return name;
  }

  /** Increments the counter */
  public final void incr() {
    add(1);
  }

  /** Decrements the counter */
  public final void decr() {
    add(-1);
  }

  /** Adds the given value to the counter */
  public abstract void add(int delta);

  /** @return The value of the counter. */
  public abstract int getCount();

  @Override
  public String toString() {
    return new JsonBuilder().beginObject().key(name).value(getCount()).endObject().toString();
  }

}
