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
