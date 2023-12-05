package solutions.trsoftware.commons.shared.util.stats;

import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Computes the maximum of a sequence of elements according to the given comparator
 *
 * @author Alex
 * @since 10/1/2023
 */
public class Max<T> extends AbstractMinMax<T, Max<T>> {

  // TODO: probably unnecessary to have both Min and Max classes (can simply use Comparator.reversed to turn one into the other)

  public Max(Comparator<T> comparator) {
    super(comparator);
  }

  @Override
  int getMultiplier() {
    return 1;
  }

  @Override
  public java.util.stream.Collector<T, ?, Max<T>> getCollector() {
    return new Collector<>(comparator);
  }

  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link Max}.
   *
   * @param <T> the input element type
   */
  public static class Collector<T> extends CollectableStats.Collector<T, Max<T>> {

    private final Comparator<T> comparator;

    public Collector(Comparator<T> comparator) {
      this.comparator = comparator;
    }

    @Override
    public Supplier<Max<T>> supplier() {
      return () -> new Max<>(comparator);
    }
  }
}
