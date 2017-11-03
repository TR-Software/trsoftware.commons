package solutions.trsoftware.commons.client.util.iterators;

import java.util.Map;

/**
 * A TransformingIterator over map entries.
 *
 * @author Alex, 1/12/14
 */
public abstract class MapEntryTransformingIterator<K,V,O> extends TransformingIterator<Map.Entry<K,V>,O> {

  public MapEntryTransformingIterator(Map<K,V> inputMap) {
    super(inputMap.entrySet().iterator());
  }

  /** Transforms an input element into the corresponding output element */
  protected final O transform(Map.Entry<K, V> entry) {
    return transformEntry(entry.getKey(), entry.getValue());
  }

  /** Transforms an input element into the corresponding output element */
  public abstract O transformEntry(K key, V value);

}
