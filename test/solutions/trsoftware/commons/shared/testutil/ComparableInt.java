package solutions.trsoftware.commons.shared.testutil;

import org.jetbrains.annotations.NotNull;
import solutions.trsoftware.commons.shared.util.ArrayUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.compare.RichComparable;
import solutions.trsoftware.commons.shared.util.text.CharRange;

import javax.annotation.Nonnull;

/**
 * An {@code int} wrapper that defines {@link #compareTo(ComparableInt)} but doesn't override {@link Object#equals(Object)},
 * thereby making its <i>natural ordering</i> purposely inconsistent with <i>equals</i>.
 *
 * The intended purpose of this class is to facilitate testing duplicate elements in a sorted list.
 */
public class ComparableInt implements RichComparable<ComparableInt> {
  private final int value;
  private final String id;

  /**
   * @param value the value for {@link #compareTo(ComparableInt)}
   * @param id will be appended to the value in {@link #toString()}
   */
  public ComparableInt(int value, int id) {
    this.value = value;
    this.id = StringUtils.parenthesize(Integer.toString(id));
  }

  public ComparableInt(int value, String id) {
    this.value = value;
    this.id = id;
  }

  public int getValue() {
    return value;
  }

  public String getId() {
    return id;
  }

  @Override
  public int compareTo(@NotNull ComparableInt o) {
    return Integer.compare(value, o.value);
  }

  @Override
  public String toString() {
    return value+id;
  }

  /**
   * Creates a {@code new ComparableInt[nValues][nInstances]} matrix (with size {@code nValues * nInstances})
   * such that the row {@code i} contains {@code nInstances} unique instances of this class representing
   * the integer {@code i}.  These instances will be "equal" according to {@link #compareTo(ComparableInt)}
   * but not according to {@link Object#equals(Object)}.
   *
   * @param nValues the number of unique {@code int} values to represent, i.e. the data will contain a row for each
   *   integer in the range <tt>[0, nValues)</tt>
   * @param nInstances the number of unique {@link ComparableInt} instances for each integer
   */
  @Nonnull
  public static ComparableInt[][] createTestData(int nValues, int nInstances) {
    return ArrayUtils.fill(new ComparableInt[nValues][nInstances],
        i -> createInstances(i, nInstances));
  }

  /**
   * Creates an array of {@code nInstances} different {@link ComparableInt} instances representing the same
   * {@code int} value
   *
   * @param nValues the number of unique {@code int} values to represent, i.e. the data will contain a row for each
   *   integer in the range <tt>[0, nValues)</tt>
   * @param nInstances the number of unique {@link ComparableInt} instances for each integer
   */
  @Nonnull
  public static ComparableInt[] createInstances(int value, int nInstances) {
    ComparableInt[] ret = new ComparableInt[nInstances];
    CharRange chars = new CharRange('a', 'z');
    // this test ret array will be filled with different instances of ComparableInt(i) for each row i
    return ArrayUtils.fill(ret, i -> new ComparableInt(value, String.valueOf(chars.charAt(i))));
  }
}
