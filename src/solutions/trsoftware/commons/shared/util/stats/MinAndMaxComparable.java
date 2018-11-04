/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.shared.util.stats;

import java.io.Serializable;

/**
 * Combines {@link MinComparable} and {@link MaxComparable} under one data structure.
 * 
 * Oct 11, 2012
 *
 * @author Alex
 */
public class MinAndMaxComparable<T extends Comparable<T>> implements Serializable {
  private MinComparable<T> min;
  private MaxComparable<T> max;

  public MinAndMaxComparable() {
    min = new MinComparable<T>();
    max = new MaxComparable<T>();
  }

  public MinAndMaxComparable(Iterable<T> candidates) {
    min = new MinComparable<T>(candidates);
    max = new MaxComparable<T>(candidates);
  }

  /** Updates the current min and max value with a new sample */
  public void update(T val) {
    min.update(val);
    max.update(val);
  }

  /** Updates the current min and max value with a new sample */
  public void update(Iterable<T> candidates) {
    min.updateAll(candidates);
    max.updateAll(candidates);
  }

  public T getMin() {
    return min.get();
  }

  public T getMax() {
    return max.get();
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MinAndMaxComparable minAndMax = (MinAndMaxComparable)o;

    if (max != null ? !max.equals(minAndMax.max) : minAndMax.max != null)
      return false;
    if (min != null ? !min.equals(minAndMax.min) : minAndMax.min != null)
      return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (min != null ? min.hashCode() : 0);
    result = 31 * result + (max != null ? max.hashCode() : 0);
    return result;
  }
}
