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

package solutions.trsoftware.commons.shared.util.iterators;

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
