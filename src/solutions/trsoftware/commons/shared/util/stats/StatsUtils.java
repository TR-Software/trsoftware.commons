package solutions.trsoftware.commons.shared.util.stats;

import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;

import java.util.stream.Collector;

/**
 * Utilities for the {@link solutions.trsoftware.commons.shared.util.stats} package.
 *
 * @author Alex
 * @since 7/27/2019
 */
public class StatsUtils {

  /**
   * Creates a {@link Collector} from a class that implements both {@link Updatable} and {@link Mergeable}.
   *
   * @param resultType the class that implements both {@link Updatable} and {@link Mergeable}
   * @param <T> the type of objects being collected
   * @param <R> the accumulator and result type (e.g. {@link NumberSampleOnline})
   * @return
   */
  public static <T, R extends Updatable<T> & Mergeable<R>> Collector<T, R, R> newCollector(Class<R> resultType) {
    return Collector.of(ReflectionUtils.newInstanceSupplier(resultType), Updatable::update, Mergeable::combine, Collector.Characteristics.IDENTITY_FINISH);
  }

}
