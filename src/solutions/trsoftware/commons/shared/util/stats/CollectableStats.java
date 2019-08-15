package solutions.trsoftware.commons.shared.util.stats;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import solutions.trsoftware.commons.shared.util.function.FunctionalUtils;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

/**
 * Marker interface for most of the classes in this package, extending both {@link Updatable} and {@link Mergeable}.
 * Facilitates their usage with {@link Stream#collect}, by providing a skeleton implementation
 * of most of the {@link java.util.stream.Collector} methods in {@link Collector}.
 * <p>
 * Subclasses just need to provide an inner singleton that extends {@link Collector} to implement its
 * {@link Collector#supplier()} method, and use that singleton as the return value for {@link #getCollector()}.
 *
 * @param <T> type of input elements (for {@link Updatable#update(Object)})
 * @param <R> the concrete class implementing this interface (for {@link Mergeable#merge(Object)})
 * @author Alex
 * @apiNote We chose not to directly extend {@link java.util.stream.Collector} because passing a new instance of
 *     this class to {@link Stream#collect} each time is somewhat inefficient: the stream pipeline evaluator will not
 *     actually use this instance, but rather will create additional new instances by invoking its {@link
 *     java.util.stream.Collector#supplier()} function.  Since {@link java.util.stream.Collector} simply describes how
 *     the reduction operation is to be performed, we provide a lightweight implementation of this descriptor in {@link
 *     Collector} that can be extended and cached by implementing classes to reduce heap allocation.
 * @see Stream#collect(java.util.stream.Collector)
 * @since 8/5/2019
 */
public interface CollectableStats<T, R extends CollectableStats<T, R>> extends Updatable<T>, Mergeable<R> {

  // NOTE: the following constants mirror those found in the java.util.stream.Collectors (which are package-private)

  /**
   * Immutable set containing the {@link Characteristics#CONCURRENT CONCURRENT}, {@link Characteristics#UNORDERED
   * UNORDERED}, and {@link Characteristics#IDENTITY_FINISH IDENTITY_FINISH} characteristics.
   * <p>
   * Can be used as the return value for the {@link java.util.stream.Collector#characteristics()} method
   * to avoid allocating a new set every time.
   *
   * @see Characteristics
   */
  ImmutableSet<Characteristics> CH_CONCURRENT_ID = Sets.immutableEnumSet(
      Characteristics.CONCURRENT,
      Characteristics.UNORDERED,
      Characteristics.IDENTITY_FINISH);

  /**
   * Immutable set containing the {@link Characteristics#UNORDERED UNORDERED} and {@link Characteristics#IDENTITY_FINISH
   * IDENTITY_FINISH} characteristics.
   * <p>
   * Can be used as the return value for the {@link java.util.stream.Collector#characteristics()} method
   * to avoid allocating a new set every time.
   *
   * @see Characteristics
   */
  ImmutableSet<Characteristics> CH_UNORDERED_ID = Sets.immutableEnumSet(
      Characteristics.UNORDERED,
      Characteristics.IDENTITY_FINISH);

  /**
   * Immutable singleton set containing the {@link Characteristics#IDENTITY_FINISH IDENTITY_FINISH} characteristic.
   * <p>
   * Can be used as the return value for the {@link java.util.stream.Collector#characteristics()} method
   * to avoid allocating a new set every time.
   *
   * @see Characteristics
   */
  ImmutableSet<Characteristics> CH_ID = Sets.immutableEnumSet(
      Characteristics.IDENTITY_FINISH);


  /**
   * @return a collector descriptor that can be passed to {@link Stream#collect}
   *     to collect the stream elements into a new instance of this class.
   * @implSpec The nested abstract class {@link Collector} offers a partial implementation of
   *     {@link java.util.stream.Collector} that can be extended as a nested singleton.
   *     Implementations should return a cached instance of such, if possible, to reduce heap allocation.
   * @see Collector
   */
  java.util.stream.Collector<T, ?, R> getCollector();


  /**
   * A descriptor that can be passed to {@link Stream#collect} to collect the input elements into an instance
   * of a particular subclass of {@link CollectableStats}.
   * <p>
   * Subclasses of {@link CollectableStats} should implement {@link #getCollector()} to return a cached singleton
   * implementation of this class that provides the appropriate {@link #supplier()} (to reduce heap allocation).
   */
  abstract class Collector<T, R extends CollectableStats<T, R>> implements java.util.stream.Collector<T, R, R> {
    @Override
    public BiConsumer<R, T> accumulator() {
      return Updatable::update;
    }

    @Override
    public BinaryOperator<R> combiner() {
      return Mergeable::combine;
    }

    /**
     * @return a function that casts an instance of this class to its own type, to implement the
     *     {@link Characteristics#IDENTITY_FINISH IDENTITY_FINISH} characteristic of this collector.
     */
    @Override
    public Function<R, R> finisher() {
      return FunctionalUtils.cast();
    }

    /**
     * By default, this collector has the {@link Characteristics#IDENTITY_FINISH IDENTITY_FINISH} and {@link
     * Characteristics#UNORDERED UNORDERED} characteristics.
     * <p>
     * Subclasses may override to return a different set of characteristics (e.g. adding {@link
     * Characteristics#CONCURRENT CONCURRENT} or removing {@link Characteristics#UNORDERED UNORDERED}).
     * <i>NOTE:</i> the {@link ImmutableSet} constants defined in this class can be used as the return value for this
     * method to avoid allocating a new set every time.
     *
     * @return an immutable set of the collector {@link Characteristics characteristics}
     * @implSpec If overriding this method, must be sure to include the {@link Characteristics#IDENTITY_FINISH
     *     IDENTITY_FINISH} characteristic, unless {@link #finisher()} is also overridden accordingly.
     *     <i>NOTE:</i> the {@link ImmutableSet} constants defined in {@link CollectableStats} are provided to
     *     facilitate overriding this method.
     * @see #CH_UNORDERED_ID
     * @see #CH_ID
     * @see #CH_CONCURRENT_ID
     */
    @Override
    public Set<Characteristics> characteristics() {
      return CH_UNORDERED_ID;
    }


    /**
     * @return a function that creates a new instance of the appropriate {@link CollectableStats} implementation.
     * @see java.util.stream.Collector#supplier()
     */
    @Override
    public abstract Supplier<R> supplier();
  }

}
