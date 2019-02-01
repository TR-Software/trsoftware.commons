package solutions.trsoftware.commons.shared.util.iterators;

import solutions.trsoftware.commons.shared.util.ArrayUtils;

/**
 * Same as {@link ArrayIterator}, but optimized for primitive {@code int} arrays:
 * provides the {@link #nextInt()} method to avoid auto-boxing.
 *
 * @author Alex
 * @since 1/11/2019
 */
public class IntArrayIterator extends IndexedIterator<Integer> implements IntIterator {

  private final int[] arr;

  public IntArrayIterator(int[] arr) {
    super(arr.length);
    this.arr = arr;
  }

  public IntArrayIterator(int[] arr, int limit) {
    super(limit);
    this.arr = arr;
  }

  public IntArrayIterator(int[] arr, int start, int limit) {
    super(start, limit);
    this.arr = arr;
    checkBounds();
  }

  private void checkBounds() throws ArrayIndexOutOfBoundsException {
    // force an ArrayIndexOutOfBoundsException if the starting index isn't valid (in client-side GWT code might throw a generic JavaScriptException otherwise)
    ArrayUtils.checkBounds(arr.length, min);
    ArrayUtils.checkBounds(arr.length, limit);
  }

  @Override
  public int nextInt() {
    maybeThrowNoSuchElement();
    return arr[i++];
  }

  @Override
  protected Integer get(int idx) {
    return arr[idx];
  }
}
