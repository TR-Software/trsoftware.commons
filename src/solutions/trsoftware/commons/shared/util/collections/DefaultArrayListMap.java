package solutions.trsoftware.commons.shared.util.collections;

import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link DefaultMap} that inserts a new instance of {@link ArrayList} when {@link #get(Object)}
 * is invoked with a key that's not already present.  This is similar to (but not quite the same as) Guava's {@link Multimap}.
 */
public class DefaultArrayListMap<K, V> extends DefaultMap<K, List<V>> {

  public DefaultArrayListMap() {
  }

  public DefaultArrayListMap(Map<K, List<V>> delegate) {
    super(delegate);
  }

  @Override
  public List<V> computeDefault(K key) {
    return new ArrayList<V>();
  }
}
