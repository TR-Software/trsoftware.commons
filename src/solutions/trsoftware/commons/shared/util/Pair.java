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

import java.io.Serializable;
import java.util.Map;

/**
 * Convenience class for representing a pair of two objects. Extends {@link Map.Entry} with some additional convenience
 * methods.
 *
 * @author Alex
 */
public class Pair<K, V> implements Map.Entry<K,V>, Serializable {
  private K first;
  private V second;

  public Pair(K first, V second) {
    this.first = first;
    this.second = second;
  }

  protected Pair() {
  }

  public K getKey() {
    return first;
  }

  public V getValue() {
    return second;
  }

  public void setFirst(K first) {
    this.first = first;
  }

  public void setSecond(V second) {
    this.second = second;
  }
  
  public V setValue(V value) {
    V oldValue = this.second;
    this.second = value;
    return oldValue;
  }

  public K first() {
    return first;
  }

  public V second() {
    return second;
  }

  @Override
  public String toString() {
    return StringUtils.template("($1, $2)", String.valueOf(first), String.valueOf(second));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Pair<?, ?> pair = (Pair<?, ?>)o;

    if (first != null ? !first.equals(pair.first) : pair.first != null)
      return false;
    return second != null ? second.equals(pair.second) : pair.second == null;
  }

  @Override
  public int hashCode() {
    int result = first != null ? first.hashCode() : 0;
    result = 31 * result + (second != null ? second.hashCode() : 0);
    return result;
  }
}
