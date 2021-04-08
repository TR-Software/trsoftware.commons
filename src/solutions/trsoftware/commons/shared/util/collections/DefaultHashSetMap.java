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

package solutions.trsoftware.commons.shared.util.collections;

import com.google.common.collect.Multimap;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of {@link DefaultMap} that inserts a new instance of {@link LinkedHashSet} when {@link #get(Object)}
 * is invoked for a key that's not already present.
 *
 * <p style="font-style: italic;">
 *   NOTE: this is similar to (but not quite the same as) Guava's {@link Multimap}.
 * </p>
 */
public class DefaultHashSetMap<K, V> extends DefaultMap<K, Set<V>> {

  public DefaultHashSetMap() {
  }

  public DefaultHashSetMap(Map<K, Set<V>> delegate) {
    super(delegate);
  }

  @Override
  public Set<V> computeDefault(K key) {
    return new LinkedHashSet<>();
  }
}
