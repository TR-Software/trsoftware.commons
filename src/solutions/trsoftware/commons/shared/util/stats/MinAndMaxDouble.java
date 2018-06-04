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
 * Combines {@link MinDouble} and {@link MaxDouble} under one data structure.
 * 
 * Oct 11, 2012
 *
 * @author Alex
 */
public class MinAndMaxDouble implements Serializable {
  private MinDouble min;
  private MaxDouble max;

  public MinAndMaxDouble() {
    min = new MinDouble();
    max = new MaxDouble();
  }


  public void update(double x) {
    min.update(x);
    max.update(x);
  }

  public void update(Iterable<Double> candidates) {
    min.update(candidates);
    max.update(candidates);
  }

  public void update(double... candidates) {
    min.update(candidates);
    max.update(candidates);
  }

  public double getMin() {
    return min.get();
  }

  public double getMax() {
    return max.get();
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MinAndMaxDouble minAndMax = (MinAndMaxDouble)o;

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
