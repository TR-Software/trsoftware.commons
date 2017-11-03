/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * @author Alex, 1/11/14
 */
public enum ComparisonOperator {
  GREATER_THAN() {
    @Override
    protected boolean eval(int cmp) {
      return cmp > 0;
    }
    @Override
    public String toString() {
      return ">";
    }
  },
  GEQ() {
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
      return "=";
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
  LEQ() {
    @Override
    protected boolean eval(int cmp) {
      return cmp <= 0;
    }
    @Override
    public String toString() {
      return "<=";
    }
  },
  LESS_THAN() {
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

}
