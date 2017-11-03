package solutions.trsoftware.commons.client.util;

/**
 * Date: Nov 3, 2008 Time: 12:10:57 PM
 *
 * @author Alex
 */
public class ImmutablePair<K, V> extends Pair<K, V> {
  public ImmutablePair(K first, V second) {
    super(first, second);
  }

  @Override
  public V setValue(V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFirst(K first) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSecond(V second) {
    throw new UnsupportedOperationException();
  }
}
