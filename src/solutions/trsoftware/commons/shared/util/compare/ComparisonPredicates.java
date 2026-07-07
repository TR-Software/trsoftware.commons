/*
 * Copyright 2026 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.shared.util.compare;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.shared.util.compare.ComparisonOperator.*;

/**
 * Provides fluent factory methods for {@link Predicate}s that operate on {@link Comparable} values.
 * <p>
 * The main feature of this class is the {@link #comparing(Function)} builder, which provides a fluent API for creating
 * a chain of {@linkplain Comparison comparisons} that can be passed to {@link Stream#filter(Predicate)}
 * to filter a stream based on a {@link Comparable} property of its elements.
 *
 * @author Alex
 * @since 6/4/2026
 * @see RichComparable
 * @see ComparisonOperator
 */
public abstract class ComparisonPredicates {

  /**
   * Returns a predicate that tests whether a property of object {@link T} is
   * greater than {@code value} according the natural order of the {@link Comparable} type {@link V}.
   *
   * @param keyExtractor extracts the {@link Comparable} property of object {@link T}
   *   to be compared against {@code value};  this will be used as the LHS operand for the comparison
   * @param value the RHS operand for the comparison
   * @return predicate evaluating <code>(t) -> keyExtractor.apply(t).{@link Comparable#compareTo compareTo}(value) > 0</code>
   * @throws NullPointerException if {@code value} is null
   */
  public static <T, V extends Comparable<? super V>> Predicate<T> isGreaterThan(
      Function<? super T, ? extends V> keyExtractor, V value) {
    requireNonNull(value, "value");
    return t -> GT.compare(keyExtractor.apply(t), value);
  }

  /**
   * Returns a predicate that tests whether a property of object {@link T} is
   * greater than or equal to {@code value} according the natural order of the {@link Comparable} type {@link V}.
   *
   * @param keyExtractor extracts the {@link Comparable} property of object {@link T}
   *   to be compared against {@code value};  this will be used as the LHS operand for the comparison
   * @param value the RHS operand for the comparison
   * @return predicate evaluating <code>(t) -> keyExtractor.apply(t).{@link Comparable#compareTo compareTo}(value) >= 0</code>
   * @throws NullPointerException if {@code value} is null
   */
  public static <T, V extends Comparable<? super V>> Predicate<T> isGreaterThanOrEqualTo(
      Function<? super T, ? extends V> keyExtractor, V value) {
    requireNonNull(value, "value");
    return t -> GE.compare(keyExtractor.apply(t), value);
  }

  /**
   * Returns a predicate that tests whether a property of object {@link T} is
   * equal to {@code value} according the natural order of the {@link Comparable} type {@link V}.
   *
   * @param keyExtractor extracts the {@link Comparable} property of object {@link T}
   *   to be compared against {@code value};  this will be used as the LHS operand for the comparison
   * @param value the RHS operand for the comparison
   * @return predicate evaluating <code>(t) -> keyExtractor.apply(t).{@link Comparable#compareTo compareTo}(value) == 0</code>
   * @throws NullPointerException if {@code value} is null
   */
  public static <T, V extends Comparable<? super V>> Predicate<T> isEqualTo(
      Function<? super T, ? extends V> keyExtractor, V value) {
    requireNonNull(value, "value");
    return t -> EQ.compare(keyExtractor.apply(t), value);
  }

  /**
   * Returns a predicate that tests whether a property of object {@link T} is
   * not equal to {@code value} according the natural order of the {@link Comparable} type {@link V}.
   *
   * @param keyExtractor extracts the {@link Comparable} property of object {@link T}
   *   to be compared against {@code value};  this will be used as the LHS operand for the comparison
   * @param value the RHS operand for the comparison
   * @return predicate evaluating <code>(t) -> keyExtractor.apply(t).{@link Comparable#compareTo compareTo}(value) != 0</code>
   * @throws NullPointerException if {@code value} is null
   */
  public static <T, V extends Comparable<? super V>> Predicate<T> isNotEqualTo(
      Function<? super T, ? extends V> keyExtractor, V value) {
    requireNonNull(value, "value");
    return t -> NE.compare(keyExtractor.apply(t), value);
  }

  /**
   * Returns a predicate that tests whether a property of object {@link T} is
   * less than or equal to {@code value} according the natural order of the {@link Comparable} type {@link V}.
   *
   * @param keyExtractor extracts the {@link Comparable} property of object {@link T}
   *   to be compared against {@code value};  this will be used as the LHS operand for the comparison
   * @param value the RHS operand for the comparison
   * @return predicate evaluating <code>(t) -> keyExtractor.apply(t).{@link Comparable#compareTo compareTo}(value) <= 0</code>
   * @throws NullPointerException if {@code value} is null
   */
  public static <T, V extends Comparable<? super V>> Predicate<T> isLessThanOrEqualTo(
      Function<? super T, ? extends V> keyExtractor, V value) {
    requireNonNull(value, "value");
    return t -> LE.compare(keyExtractor.apply(t), value);
  }

  /**
   * Returns a predicate that tests whether a property of object {@link T} is
   * less than {@code value} according the natural order of the {@link Comparable} type {@link V}.
   *
   * @param keyExtractor extracts the {@link Comparable} property of object {@link T}
   *   to be compared against {@code value};  this will be used as the LHS operand for the comparison
   * @param value the RHS operand for the comparison
   * @return predicate evaluating <code>(t) -> keyExtractor.apply(t).{@link Comparable#compareTo compareTo}(value) < 0</code>
   * @throws NullPointerException if {@code value} is null
   */
  public static <T, V extends Comparable<? super V>> Predicate<T> isLessThan(
      Function<? super T, ? extends V> keyExtractor, V value) {
    requireNonNull(value, "value");
    return t -> LT.compare(keyExtractor.apply(t), value);
  }


  /**
   * Returns a fluent builder for predicates that compare a property of an object against one or more comparable values.
   * <p>
   * For example, the following code finds people with age &ge; 21:
   * <pre>
   *   personStream.filter({@link #comparing}(Person::getAge).{@link ComparisonBuilder#isGreaterThanOrEqualTo isGreaterThanOrEqualTo}(21))
   * </pre>
   * The predicates returned by the builder can be chained to allow comparing the same property against more than one value.
   * For example, to find all orders for the year 2020:
   * <pre>
   *   orderStream.filter({@link #comparing}(Order::getDate)
   *     .{@link ComparisonBuilder#isGreaterThanOrEqualTo(Comparable) isGreaterThanOrEqualTo}(Timestamp.valueOf("2020-01-01 00:00:00"))
   *     .{@link Comparison#isLessThan(Comparable) isLessThan}(Timestamp.valueOf("2021-01-01 00:00:00")))
   * </pre>
   *
   * @param keyExtractor extracts a property of object {@link T} to be compared against the values passed to the builder methods
   * @param <T> type of object with a {@link Comparable} property
   * @param <V> the {@link Comparable} value type
   * @throws NullPointerException if {@code keyExtractor} is null
   */
  public static <T, V extends Comparable<? super V>> ComparisonBuilder<T, V> comparing(
        Function<? super T, ? extends V> keyExtractor) {
    return new ComparisonBuilder<>(keyExtractor);
  }

  /**
   * Base class providing the builder methods for {@link ComparisonBuilder}, which are also used to implement chaining
   * of the {@link Comparison} predicates constructed by the builder.
   *
   * @param <T> type of object with a {@link Comparable} property
   * @param <V> the {@link Comparable} value type
   * @see #comparing(Function)
   */
  static abstract class ComparisonBase<T, V extends Comparable<? super V>> {
    /** Function returning a property of object {@link T} to be compared against the values passed to the builder methods */
    protected final Function<? super T, ? extends V> keyExtractor;

    /**
     * @param keyExtractor extracts a property of object {@link T} to be compared against the values passed to the builder methods
     * @throws NullPointerException if {@code keyExtractor} is null
     */
    protected ComparisonBase(Function<? super T, ? extends V> keyExtractor) {
      this.keyExtractor = requireNonNull(keyExtractor, "keyExtractor");
    }

    /**
     * Creates a predicate for the specified comparison, optionally appending it to the current chain representing
     * the logical AND of the predicates.
     */
    protected abstract Comparison<T, V> append(ComparisonOperator op, V value);

    /**
     * Returns a predicate that tests whether a {@linkplain #keyExtractor property} of object {@link T} is
     * greater than {@code value} according the natural order of the {@link Comparable} type {@link V}.
     * <p>
     * If this method is invoked on an existing {@link Comparison} predicate, the new predicate will be chained to this one,
     * similar to {@link Predicate#and(Predicate)}.
     */
    public Comparison<T, V> isGreaterThan(V value) {
      return append(GT, value);
    }
  
    /**
     * Returns a predicate that tests whether a {@linkplain #keyExtractor property} of object {@link T} is
     * greater than or equal to {@code value} according the natural order of the {@link Comparable} type {@link V}.
     * <p>
     * If this method is invoked on an existing {@link Comparison} predicate, the new predicate will be chained to this one,
     * similar to {@link Predicate#and(Predicate)}.
     */
    public Comparison<T, V> isGreaterThanOrEqualTo(V value) {
      return append(GE, value);
    }
  
    /**
     * Returns a predicate that tests whether a {@linkplain #keyExtractor property} of object {@link T} is
     * equal to {@code value} according the natural order of the {@link Comparable} type {@link V}.
     * <p>
     * If this method is invoked on an existing {@link Comparison} predicate, the new predicate will be chained to this one,
     * similar to {@link Predicate#and(Predicate)}.
     */
    public Comparison<T, V> isEqualTo(V value) {
      return append(EQ, value);
    }
  
    /**
     * Returns a predicate that tests whether a {@linkplain #keyExtractor property} of object {@link T} is
     * not equal to {@code value} according the natural order of the {@link Comparable} type {@link V}.
     * <p>
     * If this method is invoked on an existing {@link Comparison} predicate, the new predicate will be chained to this one,
     * similar to {@link Predicate#and(Predicate)}.
     */
    public Comparison<T, V> isNotEqualTo(V value) {
      return append(NE, value);
    }
  
    /**
     * Returns a predicate that tests whether a {@linkplain #keyExtractor property} of object {@link T} is
     * less than or equal to {@code value} according the natural order of the {@link Comparable} type {@link V}.
     * <p>
     * If this method is invoked on an existing {@link Comparison} predicate, the new predicate will be chained to this one,
     * similar to {@link Predicate#and(Predicate)}.
     */
    public Comparison<T, V> isLessThanOrEqualTo(V value) {
      return append(LE, value);
    }
  
    /**
     * Returns a predicate that tests whether a {@linkplain #keyExtractor property} of object {@link T} is
     * less than {@code value} according the natural order of the {@link Comparable} type {@link V}.
     * <p>
     * If this method is invoked on an existing {@link Comparison} predicate, the new predicate will be chained to this one,
     * similar to {@link Predicate#and(Predicate)}.
     */
    public Comparison<T, V> isLessThan(V value) {
      return append(LT, value);
    }

  }

  /**
   * Fluent builder for predicates comparing a {@linkplain #keyExtractor property} of an object with one or more comparable values.
   * <p>
   * For example, the following code finds people with age &ge; 21:
   * <pre>
   *   personStream.filter({@link #comparing}(Person::getAge).{@link ComparisonBuilder#isGreaterThanOrEqualTo isGreaterThanOrEqualTo}(21))
   * </pre>
   * The predicates returned by the builder can be chained to allow comparing the same property against more than one value.
   * For example, to find all orders for the year 2020:
   * <pre>
   *   orderStream.filter({@link #comparing}(Order::getDate)
   *     .{@link ComparisonBuilder#isGreaterThanOrEqualTo(Comparable) isGreaterThanOrEqualTo}(Timestamp.valueOf("2020-01-01 00:00:00"))
   *     .{@link Comparison#isLessThan(Comparable) isLessThan}(Timestamp.valueOf("2021-01-01 00:00:00")))
   * </pre>
   * <p>
   * The builder itself is stateless: it can be safely reused to construct different predicate chains based on the
   * same {@link #keyExtractor}.  The stateful objects are the {@link Comparison} predicates returned by the factory
   * builder methods: each of those represents the start of a new chain of predicates.
   * <p>
   * Note: this class doesn't provide a public constructor: a new instance can be obtained using the factory method
   * {@link #comparing(Function)}.
   *
   * @param <T> type of object with a {@link Comparable} property
   * @param <V> the {@link Comparable} value type
   */
  public static class ComparisonBuilder<T, V extends Comparable<? super V>> extends ComparisonBase<T, V> {

    /**
     * Non-public constructor: use the {@link #comparing(Function)} factory method to create an instance of this class.
     *
     * @param keyExtractor extracts a property of object {@link T} to be compared against the values passed to the builder methods
     * @throws NullPointerException if {@code keyExtractor} is null
     */
    // TODO: maybe make this constructor public?  No real reason to force going through a factory method (except for the shorter syntax)
    protected ComparisonBuilder(Function<? super T, ? extends V> keyExtractor) {
      super(keyExtractor);
    }

    @Override
    protected Comparison<T, V> append(ComparisonOperator op, V value) {
      return new Comparison<>(keyExtractor, op, value);
    }
  }

  /**
   * A predicate constructed by {@link ComparisonBuilder}, comparing a {@linkplain #keyExtractor property} of object {@link T}
   * with a comparable value {@link V}.
   * <p>
   * Can be chained with other {@link Comparison} predicates using the same factory methods.
   * For example, to find all orders for the year 2020:
   * <pre>
   *   orderStream.filter({@link #comparing}(Order::getDate)
   *     .{@link ComparisonBuilder#isGreaterThanOrEqualTo(Comparable) isGreaterThanOrEqualTo}(Timestamp.valueOf("2020-01-01 00:00:00"))
   *     .{@link Comparison#isLessThan(Comparable) isLessThan}(Timestamp.valueOf("2021-01-01 00:00:00")))
   * </pre>
   * All chaining methods invoked on this instance create a new predicate representing a logical AND of the new predicate
   * and this one, similar to {@link Predicate#and(Predicate)}.
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO: document the short-circuiting semantics for chaining (see {@link #test(Object)}), and how it differs from {@link Predicate#and(Predicate)}.
   * </p>
   *
   * @param <T> type of object with a {@link Comparable} property
   * @param <V> the {@link Comparable} value type
   */
  public static class Comparison<T, V extends Comparable<? super V>> extends ComparisonBase<T, V> implements Predicate<T> {
    // Note: this class is a node in a chain of comparisons

    @Nullable
    private final Comparison<T, V> prev;  // linked list
    @Nonnull
    private final ComparisonOperator operator;
    @Nonnull
    private final V value;

    public Comparison(Function<? super T, ? extends V> keyExtractor, ComparisonOperator operator, V value) {
      this(keyExtractor, null, operator, value);
    }

    public Comparison(Function<? super T, ? extends V> keyExtractor, Comparison<T, V> prev, ComparisonOperator operator, V value) {
      super(keyExtractor);
      this.prev = prev;
      this.operator = requireNonNull(operator, "operator");
      this.value = requireNonNull(value, "value");
    }

    @Override
    protected Comparison<T, V> append(ComparisonOperator op, V value) {
      return new Comparison<>(keyExtractor, this, op, value);
    }

    @Override
    public boolean test(T t) {
//      V key = keyExtractor.apply(t);
//      boolean result = true;
//      for (Comparison<T, V> next = this; next != null; next = next.prev) {
//        result &= next.operator.compare(key, next.value);
//      }
//      /* TODO: maybe start evaluating the chain at its root, to have left-to-right short-circuiting semantics
//          match Java's Predicate.and(other) {(t) -> test(t) && other.test(t)}
//          - possible approaches:
//            1) make this method recursive
//               cons: might have slightly worse performance for long chains
//               - problem: how to avoid invoking keyExtractor at every step of recursion?
//            2) use a Stack to simulate recursion
//               cons: extra object allocation
//            2) add a this.next pointer in addition to this this.prev, making it a doubly-linked list instead of a backward list
//               cons: have to make pointer field mutable and modify existing instances after creation
//            ** (1) seems cleaner
//          ** Otherwise, should document the RTL short-circuiting behavior if keeping it
//
//       */
//      return result;
      return test(keyExtractor.apply(t));
    }

    /**
     * Recursive implementation of {@link #test(Object)}
     * @param key the key (provided by {@link #keyExtractor}) to compare against {@link #value}
     */
    protected boolean test(V key) {
      if (prev == null)
        return operator.compare(key, value);
      return prev.test(key) && operator.compare(key, value);
    }

    @Override
    public String toString() {
      return toStringBuilder().toString();
    }

    protected StringBuilder toStringBuilder() {
      StringBuilder sb = prev != null ? prev.toStringBuilder().append(" && ") : new StringBuilder();
      if (prev != null)
        sb.append(prev.toString()).append(" && ");
      return sb.append("x ").append(operator).append(' ').append(value);
    }
  }
}