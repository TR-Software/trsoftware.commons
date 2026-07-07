package solutions.trsoftware.commons.shared.util.compare;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.util.ListBuilder;
import solutions.trsoftware.commons.shared.util.time.Clock;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertStreamEquals;
import static solutions.trsoftware.commons.shared.util.compare.ComparisonOperator.values;
import static solutions.trsoftware.commons.shared.util.compare.ComparisonPredicates.*;

/**
 * @author Alex
 * @since 6/5/2026
 */
public class ComparisonPredicatesTest extends BaseTestCase {

  private List<Order> orders;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    Date t0 = Clock.date();
    orders = IntStream.range(0, 10).mapToObj(
        i -> new Order(i, new Date(t0.getTime() + TimeUnit.MINUTES.toMillis(i))))
        .collect(toList());
  }

  /**
   * Tests constructing compound predicate using a builder returned by {@link ComparisonPredicates#comparing(Function)}
   */
  public void testBuilder() {
    int i = orders.size() / 2;  // midpoint
    Order order = orders.get(i);  // the middle order
    ComparisonBuilder<Order, Date> compareDate = comparing(Order::getDate);
    // 1) a single predicate:
    verifyFilteredOrders(orders.subList(0, i), compareDate.isLessThan(order.getDate()));
    // 2) multiple chained predicates (Note: reusing the same builder instance to verify that it's stateless)
    verifyFilteredOrders(ListBuilder.copyOf(orders.subList(1, 5)).remove(orders.get(2)).getList(), compareDate
        .isGreaterThan(orders.get(0).getDate())
        .isLessThanOrEqualTo(orders.get(4).getDate())
        .isNotEqualTo(orders.get(2).getDate()));
  }

  private <V extends Comparable<V>> void verifyFilteredOrders(List<Order> expected, Comparison<Order, V> comparison) {
    assertStreamEquals(expected, orders.stream().filter(comparison));
    // make sure negation of a Comparison chain works as expected (i.e. evaluating `!a || !b || !c` instead of `a && b && c`)
    List<Order> expectedNegation = ListBuilder.copyOf(orders).removeAll(expected).getList();
    assertStreamEquals(expectedNegation, orders.stream().filter(comparison.negate()));
  }

  /**
   * Tests all of the top-level predicate factory methods in {@link ComparisonPredicates} as well as the equivalent
   * methods of {@link ComparisonBuilder}
   */
  public void testPredicates() {
    testPredicates(Order::getDate);
    testPredicates(Order::getAmount);
  }

  private <V extends Comparable<V>> void testPredicates(Function<Order, V> keyExtractor) {
    ComparisonBuilder<Order, V> builder = comparing(keyExtractor);
    for (Order order1 : orders) {
      for (Order order2 : orders) {
        V v1 = keyExtractor.apply(order1);
        V v2 = keyExtractor.apply(order2);
        int cmp = v1.compareTo(v2);
        for (ComparisonOperator op : values()) {
          Predicate<Order> pred = predicateFactory(op, keyExtractor).apply(v2);
          assertEquals(op.test(cmp), pred.test(order1));
          Comparison<Order, V> comparison = predicateFactory(op, builder).apply(v2);
          assertEquals(op.test(cmp), comparison.test(order1));
        }
      }
    }
  }

  private static <T, V extends Comparable<? super V>> Function<V, Predicate<T>> predicateFactory(
      ComparisonOperator op, Function<? super T, ? extends V> keyExtractor) {
    switch (op) {
      case GT:
        return v -> isGreaterThan(keyExtractor, v);
      case GE:
        return v -> isGreaterThanOrEqualTo(keyExtractor, v);
      case EQ:
        return v -> isEqualTo(keyExtractor, v);
      case NE:
        return v -> isNotEqualTo(keyExtractor, v);
      case LE:
        return v -> isLessThanOrEqualTo(keyExtractor, v);
      case LT:
        return v -> isLessThan(keyExtractor, v);
    }
    throw new IllegalArgumentException();  // should never reach this
  }

  private static <T, V extends Comparable<? super V>> Function<V, Comparison<T, V>> predicateFactory(
      ComparisonOperator op, ComparisonBase<T, V> builder) {
    switch (op) {
      case GT:
        return builder::isGreaterThan;
      case GE:
        return builder::isGreaterThanOrEqualTo;
      case EQ:
        return builder::isEqualTo;
      case NE:
        return builder::isNotEqualTo;
      case LE:
        return builder::isLessThanOrEqualTo;
      case LT:
        return builder::isLessThan;
    }
    throw new IllegalArgumentException();  // should never reach this
  }


  private static class Order {
    private final int amount;
    private final Date date;

    public Order(int amount, Date date) {
      this.amount = amount;
      this.date = date;
    }

    public int getAmount() {
      return amount;
    }

    public Date getDate() {
      return date;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("amount", amount)
          .add("date", date)
          .toString();
    }
  }

}