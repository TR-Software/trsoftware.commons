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

package solutions.trsoftware.commons.shared.util;

import com.google.common.collect.Multimap;

import java.util.Arrays;


/**
 * Wraps a {@link Multimap} to allow adding multiple new entries in one expression (via method chaining).
 *
 * @see MapDecorator
 * @author Alex
 * @since 3/8/2018
 */
public class MultimapDecorator<K, V> {

  private Multimap<K, V> multimap;

  public MultimapDecorator(Multimap<K, V> delegate) {
    this.multimap = delegate;
  }

  @SafeVarargs
  public final MultimapDecorator<K, V> putAll(K key, V... values) {
    multimap.putAll(key, Arrays.asList(values));
    return this;
  }

  public Multimap<K, V> getMultimap() {
    return multimap;
  }

}
