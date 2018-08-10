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

package solutions.trsoftware.commons.shared.util.compare;

/**
 * Provides an abstraction for numerical comparison operators.
 *
 * @author Alex, 1/11/14
 */
public enum ComparisonOperator {
  GT() {
    @Override
    protected boolean eval(int cmp) {
      return cmp > 0;
    }
    @Override
    public String toString() {
      return ">";
    }
  },
  GTE() {
    @Override
    protected boolean eval(int cmp) {
      return cmp >= 0;
    }
    @Override
    public String toString() {
      return ">=";
    }
  },
  EQ() {
    @Override
    protected boolean eval(int cmp) {
      return cmp == 0;
    }
    @Override
    public String toString() {
      return "==";
    }
  },
  NEQ() {
    @Override
    protected boolean eval(int cmp) {
      return cmp != 0;
    }
    @Override
    public String toString() {
      return "!=";
    }
  },
  LTE() {
    @Override
    protected boolean eval(int cmp) {
      return cmp <= 0;
    }
    @Override
    public String toString() {
      return "<=";
    }
  },
  LT() {
    @Override
    protected boolean eval(int cmp) {
      return cmp < 0;
    }
    @Override
    public String toString() {
      return "<";
    }
  };

  protected abstract boolean eval(int cmp);

  public <T> boolean compare(Comparable<T> lhs, T rhs) {
    return eval(lhs.compareTo(rhs));
  }

  /**
   * Looks up the {@link ComparisonOperator} that most-accurately describes the natural ordering relationship
   * between the given elements.
   * <p>
   *   <b>Examples</b>:
   * <table>
   *   <tr>
   *     <th>Inputs</th>
   *     <th>Result</th>
   *   </tr>
   *   <tr>
   *     <td>{@code 1, 1}</td>
   *     <td>{@link #EQ} ({@code ==})</td>
   *   </tr>
   *   <tr>
   *     <td>{@code 1, 2}</td>
   *     <td>{@link #LT} ({@code <})</td>
   *   </tr>
   *   <tr>
   *     <td>{@code 2, 1}</td>
   *     <td>{@link #GT} ({@code >})</td>
   *   </tr>
   * </table>
   * </p>
   * @return the operator that most-accurately describes the natural ordering relationship of the given elements
   */
  public static <T extends Comparable<? super T>> ComparisonOperator lookup(T lhs, T rhs) {
    int cmp = lhs.compareTo(rhs);
    if (cmp < 0)
      return LT;
    else if (cmp == 0)
      return EQ;
    else
      return GT;
  }

}
