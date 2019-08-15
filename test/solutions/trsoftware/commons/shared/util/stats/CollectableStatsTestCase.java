package solutions.trsoftware.commons.shared.util.stats;

import com.google.common.base.Strings;
import junit.framework.Assert;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.ArrayUtils;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * @author Alex
 * @since 8/6/2019
 */
public abstract class CollectableStatsTestCase extends TestCase {

  /**
   * Should be implemented to test using an instance of the {@link CollectableStats} class under test
   * with {@link Stream#collect(Collector)}.
   *
   * @see #doTestAsCollector(CollectableStats, BiConsumer, Object[])
   */
  public abstract void testAsCollector() throws Exception;

  /**
   * Helper method for {@link #testAsCollector()}: takes a new instance of the class being tested,
   * updates it with all of the expected inputs using {@link CollectableStats#updateAll(Object[])},
   * then calls {@link Stream#collect(Collector)} on a stream of the same inputs.  Finally, uses
   * the given equality assertion function to assert that the result of {@link Stream#collect(Collector)}
   * is equal to the original instance that was populated using the given inputs.
   * <p>
   * The test will be performed on both sequential and parallel streams.
   *
   * @param newInstance a new instance of the {@link CollectableStats} subclass being tested
   * @param equalityAssertion a function that asserts that 2 instances of the class are equal,
   * for example: could pass a method reference to {@link #assertEquals(Object, Object)}.
   * If {@code null}, the instances will be tested using {@link #assertEquals(Object, Object)}.
   * @param inputs will be used to populate the given instance using {@link CollectableStats#updateAll(Object[])},
   * as well as to create the {@link Stream} for testing {@link Stream#collect(Collector)}.  Should contain
   * enough elements to meaningfully test the collector on a parallel stream (at least 5 should be good enough).
   * @return the result of {@link Stream#collect(Collector)}
   *
   * @param <T> the type of inputs accepted by the class being tested
   * @param <R> the type of the {@link CollectableStats} class being tested
   */
  @SafeVarargs
  public static <T, R extends CollectableStats<T, R>> R doTestAsCollector(R newInstance, BiConsumer<R, R> equalityAssertion, T... inputs) {
    int minInputsNeeded = 5;
    assertTrue(Strings.lenientFormat("Expected at least %s inputs to properly test this collector with a parallel stream (only %s given)", minInputsNeeded, inputs.length),
        inputs.length >= minInputsNeeded);
    newInstance.updateAll(inputs);
    if (equalityAssertion == null)
      equalityAssertion = Assert::assertEquals;
    Collector<T, ?, R> collector = newInstance.getCollector();
    assertNotNull(collector);
    // we test both sequential and parallel streams
    R collectorResult = Arrays.stream(inputs).collect(collector);
    R collectorResultParallel = Arrays.stream(inputs).parallel().collect(collector);
    // assert that both results are equal to the original
    assertNotSame(newInstance, collectorResult);
    equalityAssertion.accept(newInstance, collectorResult);
    assertNotSame(newInstance, collectorResultParallel);
    equalityAssertion.accept(newInstance, collectorResultParallel);
    // since we've verified that both results are equal, we can return either one
    return collectorResult;
  }

  /**
   * Helper method for {@link #testAsCollector()} for {@link CollectableStats} instances that can also
   * be used to collect a stream of primitive doubles.
   * <p>
   * First tests the given instance as {@link Collector} of {@link Double} objects by calling
   * {@link #doTestAsCollector(CollectableStats, BiConsumer, Object[])} with the given args, and then
   * repeats the same test for {@link DoubleStream#collect} using the given  {@link DoubleStream} collection function
   * on a {@link DoubleStream} containing the given inputs.
   * <p>
   * The test will be performed on both sequential and parallel streams.
   *
   * @param <R> the type of the {@link CollectableStats} class being tested
   * @param newInstance a new instance of the {@link CollectableStats} subclass being tested
   * @param doubleStreamCollector reference to a method that calls {@link DoubleStream#collect(Supplier,
   *     ObjDoubleConsumer, BiConsumer)} with the appropriate args.
   * @param equalityAssertion a function that asserts that 2 instances of the class are equal,
   *     for example: could pass a method reference to {@link #assertEquals(Object, Object)}.
   *     If {@code null}, the instances will be tested using {@link #assertEquals(Object, Object)}.
   * @param inputs will be used to populate the given instance using {@link CollectableStats#updateAll(Object[])},
   *     as well as to create the {@link Stream} for testing {@link Stream#collect(Collector)} and the
   *     {@link DoubleStream} for testing {@link DoubleStream#collect}.
   *     Should contain enough elements to meaningfully test the collector on a parallel stream (at least 5 should be
   *     good enough).
   * @return the result of {@link Stream#collect(Collector)}
   */
  public static <R extends CollectableStats<Double, R> & UpdatableDouble> R doTestAsDoubleStreamCollector(
      R newInstance, Function<DoubleStream, R> doubleStreamCollector, BiConsumer<R, R> equalityAssertion,
      double... inputs) {
    // 1) test with Stream<Double>.collect(Collector<Double, R>)
    R expectedResult = doTestAsCollector(newInstance, equalityAssertion, ArrayUtils.box(inputs));
    // 2) repeat the same test with DoubleStream.collect
    // we test both sequential and parallel streams
    R doubleStreamCollectorResult = doubleStreamCollector.apply(Arrays.stream(inputs));
    R doubleStreamCollectorResultParallel = doubleStreamCollector.apply(Arrays.stream(inputs).parallel());
    // assert that both results are equal to the original
    equalityAssertion.accept(expectedResult, doubleStreamCollectorResult);
    equalityAssertion.accept(expectedResult, doubleStreamCollectorResultParallel);
    // since we've verified that both results are equal, we can return either one
    return doubleStreamCollectorResult;
  }
}
