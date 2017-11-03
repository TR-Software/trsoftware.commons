package solutions.trsoftware.commons.server.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Uses an {@link AtomicInteger} to implement {@link Counter}, and therefore, should be thread-safe.
 *
 * @author Alex, 10/31/2017
 */
public class SimpleCounter extends Counter {

  private AtomicInteger count = new AtomicInteger();

  public SimpleCounter(String name) {
    super(name);
  }

  @Override
  public void add(int delta) {
    count.addAndGet(delta);
  }

  @Override
  public int getCount() {
    return count.get();
  }
}
