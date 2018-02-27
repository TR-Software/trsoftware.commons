package solutions.trsoftware.commons.server.util.collections;

import solutions.trsoftware.commons.shared.util.collections.DefaultMap;

import java.util.Map;

/**
 * Implements {@link #computeDefault(Object)} to return a new instance (by reflection) of the class passed to the
 * constructor.
 *
 * @author Alex
 * @since 2/25/2018
 */
public class DefaultMapByReflection<K, V> extends DefaultMap<K, V> {

  private final Class<V> valueClass;

  public DefaultMapByReflection(Class<V> valueClass) {
    this.valueClass = valueClass;
  }

  public DefaultMapByReflection(Map<K, V> delegate, Class<V> valueClass) {
    super(delegate);
    this.valueClass = valueClass;
  }

  @Override
  public V computeDefault(K key) {
    try {
      return valueClass.newInstance();
    }
    catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
