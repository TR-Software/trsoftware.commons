package solutions.trsoftware.commons.shared.util;

/**
 * @author Alex, 3/30/2016
 */
public class HasValuePair<L, R> implements OrderedPair<L, R> {

  private final HasValue<L> left;
  private final HasValue<R> right;

  public HasValuePair(HasValue<L> left, HasValue<R> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public L getLeft() {
    return left.getValue();
  }

  @Override
  public R getRight() {
    return right.getValue();
  }
}
