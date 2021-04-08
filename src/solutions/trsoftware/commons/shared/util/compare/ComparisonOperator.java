/*
 * Copyright 2021 TR Software Inc.
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Provides a general abstraction for comparison operators.
 *
 * Can be used as a {@link IntPredicate predicate} on the result of {@link Comparable#compareTo(Object)} (see {@link #test(int)})
 *
 * @author Alex, 1/11/14
 */
public enum ComparisonOperator implements IntPredicate {

  /**
   * Abstraction for the &gt; operator
   */
  GT() {
    @Override
    public boolean test(int cmp) {
      return cmp > 0;
    }
    @Override
    public String toString() {
      return ">";
    }
  },
  /**
   * Abstraction for the &ge; operator
   */
  GE() {
    @Override
    public boolean test(int cmp) {
      return cmp >= 0;
    }
    @Override
    public String toString() {
      return ">=";
    }
  },
  /**
   * Abstraction for the "=" operator.
   */
  EQ() {
    @Override
    public boolean test(int cmp) {
      return cmp == 0;
    }
    @Override
    public String toString() {
      return "==";
    }
  },
  /**
   * Abstraction for the &ne; operator
   */
  NE() {
    @Override
    public boolean test(int cmp) {
      return cmp != 0;
    }
    @Override
    public String toString() {
      return "!=";
    }
  },
  /**
   * Abstraction for the &le; operator
   */
  LE() {
    @Override
    public boolean test(int cmp) {
      return cmp <= 0;
    }
    @Override
    public String toString() {
      return "<=";
    }
  },
  /**
   * Abstraction for the &lt; operator
   */
  LT() {
    @Override
    public boolean test(int cmp) {
      return cmp < 0;
    }
    @Override
    public String toString() {
      return "<";
    }
  };

  /**
   * Takes the result of {@link Comparable#compareTo(Object)} and tests it against this operator.
   * <p>
   * Example:
   * <pre>
   *   {@link #LT}.test("foo".compareTo("bar")); // returns false
   *   {@link #NE}.test("foo".compareTo("bar")); // returns true
   * </pre>
   *
   * @param cmp a result of {@link Comparable#compareTo(Object)}
   * @return {@code true} iff the arg satisfies this operator
   */
  @Override
  public abstract boolean test(int cmp);


  /**
   * Tests whether the given args satisfy this operator.
   * Can also be used as a {@link BiPredicate} lambda or method refence (e.g. {@code GT::compare}).
   * <p>
   * Example:
   * <pre>
   *   {@link #LT}.compare(4, 5);  // returns true
   *   {@link #GE}.compare(4, 5);  // returns false
   * </pre>
   *
   * @param <T> the type of object being compared
   * @return {@code true} iff the arg satisfies this operator
   */
  public <T> boolean compare(Comparable<T> lhs, T rhs) {
    return test(lhs.compareTo(rhs));
  }

  /**
   * This is a higher order function that returns a partial application of {@link #compare(Comparable, Object)} with
   * the RHS argument fixed to the given value.
   *
   * @param rhs fixed value for the second arg to {@link #compare(Comparable, Object)}
   * @param <T> the arg type
   * @return a predicate that compares its input (LHS) to the given value (RHS) using this comparison operator.
   *
   * @see solutions.trsoftware.commons.shared.util.function.FunctionalUtils#partial1(BiFunction, Object)
   * @see <a href="https://en.wikipedia.org/wiki/Partial_application">Partial application</a>
   * @see <a href="https://en.wikipedia.org/wiki/Higher-order_function">Higher-order function</a>
   */
  public <T extends Comparable<T>> Predicate<T> comparingTo(T rhs) {
    return t -> compare(t, rhs);
  }

  /**
   * Looks up the {@link ComparisonOperator} that most-accurately describes the natural ordering relationship
   * between the given elements.
   * <p>
   *   <b>Examples</b>:
   * <table>
   *   <tr>
   *     <th>Inputs</th>
   *     <th>Result</th>
   *   </tr>
   *   <tr>
   *     <td>{@code 1, 1}</td>
   *     <td>{@link #EQ} ({@code ==})</td>
   *   </tr>
   *   <tr>
   *     <td>{@code 1, 2}</td>
   *     <td>{@link #LT} ({@code <})</td>
   *   </tr>
   *   <tr>
   *     <td>{@code 2, 1}</td>
   *     <td>{@link #GT} ({@code >})</td>
   *   </tr>
   * </table>
   * </p>
   * @return the operator that most-accurately describes the natural ordering relationship of the given elements
   */
  public static <T extends Comparable<? super T>> ComparisonOperator describeRelationship(T lhs, T rhs) {
    int cmp = lhs.compareTo(rhs);
    if (cmp < 0)
      return LT;
    else if (cmp == 0)
      return EQ;
    else
      return GT;
  }

  /**
   * @return the name of this operator in plain English; for example:
   * <table>
   *   <tr>
   *     <td>{@link ComparisonOperator}:</td>
   *     <td>{@link #toString()}:</td>
   *     <td>{@link #prettyName()}:</td>
   *   </tr>
   *   <tr>
   *     <td>{@link #GT}</td>
   *     <td>{@code ">"}</td>
   *     <td>{@code "greater than"}</td>
   *   </tr>
   *   <tr>
   *     <td>{@link #GE}</td>
   *     <td>{@code ">="}</td>
   *     <td>{@code "greater than or equal to"}</td>
   *   </tr>
   *   <tr>
   *     <td>{@link #EQ}</td>
   *     <td>{@code "=="}</td>
   *     <td>{@code "equal to"}</td>
   *   </tr>
   *   <tr>
   *     <td>{@link #NE}</td>
   *     <td>{@code "!="}</td>
   *     <td>{@code "not equal to"}</td>
   *   </tr>
   * </table>
   */
  public String prettyName() {
    switch (this) {
      case GT:
        return "greater than";
      case GE:
        return "greater than or equal to";
      case EQ:
        return "equal to";
      case NE:
        return "not equal to";
      case LE:
        return "less than or equal to";
      case LT:
        return "less than";
      default:
        // should never reach this
        throw new UnsupportedOperationException("ComparisonOperator.prettyName not implemented for " + this);
    }
  }


  /**
   * NOTE: this method overrides {@link Enum#toString()} and <i>does not return the name of the enum constant</i>.
   *
   * @return the corresponding Java operator (e.g. {@code ">="} for {@link #GE})
   * @see #name()
   */
  @Override
  public abstract String toString();


  /**
   * The inverse of {@link #toString()}: returns the {@link ComparisonOperator} that corresponds to the given
   * Java primitive operator.
   *
   * @param javaOperator a primitive comparison operator like {@code ">"}, {@code ">="}, {@code "!="}, etc.
   * @return the {@link ComparisonOperator} counterpart for the given Java operator, or {@code null} if not found
   */
  public static ComparisonOperator lookup(String javaOperator) {
    Preconditions.checkNotNull(javaOperator);
    Preconditions.checkArgument(!javaOperator.isEmpty());
    // TODO: maybe also trim the given string?
    // lazy-init the lookup table
    if (lookupTable == null) {
      synchronized (ComparisonOperator.class) {
        if (lookupTable == null) {
          ImmutableBiMap.Builder<String, ComparisonOperator> mapBuilder = ImmutableBiMap.builder();
          for (ComparisonOperator op : values()) {
            mapBuilder.put(op.toString(), op);
          }
          lookupTable = mapBuilder.build();
        }
      }
    }
    return lookupTable.get(javaOperator);
  }

  /**
   * Maps plain Java operator strings to the corresponding enum constants.
   * Will be created on first use by {@link #lookup(String)}.
   */
  private static transient ImmutableBiMap<String, ComparisonOperator> lookupTable = null;
}
