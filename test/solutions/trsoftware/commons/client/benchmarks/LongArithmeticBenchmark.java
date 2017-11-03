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

package solutions.trsoftware.commons.client.benchmarks;

import com.google.gwt.benchmarks.client.Benchmark;
import com.google.gwt.benchmarks.client.IntRange;
import com.google.gwt.benchmarks.client.Operator;
import com.google.gwt.benchmarks.client.RangeField;


/**
 * This benchmarks measures the performance of long emulation introduced in GWT 1.5.
 * It applies every kind of arithmetic operation to longs and doubles, to compare
 * their performance across browsers.
 */
public class LongArithmeticBenchmark extends Benchmark {
  // NOTE: must increase the slow script warning threshold for IE in order for this benchmark to work in web mode (see http://support.microsoft.com/?kbid=175500)

  // NOTE: fields used as parameter ranges (with @RangeField annotation) must not be private
  IntRange iterationsRange = new IntRange(1024, 1024*16, Operator.MULTIPLY, 2);
  IntRange typeRange = new IntRange(0, 1, Operator.ADD, 1);  // 1 = long, 0 = double

  private long result;
  private double dResult;

  @Override
  public String getModuleName() {
    return "solutions.trsoftware.commons.TestCommons";
  }

//  public void testFoo() {}
//  public void testFoo(@RangeField("iterationsRange") Integer iterations) {
//    result = 12345L;
//    for (int i = 1; i <= iterations; i++)
//      result += iterations;
//  }



  public void testLongAddition(@RangeField("iterationsRange") Integer iterations, @RangeField("typeRange") Integer useLong) {
    if (useLong == 1) {
      result = 12345L;
      for (int i = 1; i <= iterations; i++)
        result += i;
    }
    else {
      dResult = 12345d;
      for (int i = 1; i <= iterations; i++)
        dResult += i;
    }
  }
  // Required for JUnit
  public void testLongAddition() {
  }


  public void testLongDivision(@RangeField("iterationsRange") Integer iterations, @RangeField("typeRange") Integer useLong) {
    if (useLong == 1) {
      result = Long.MAX_VALUE-12345L;  // subtract a little to get a number that's not a power of 2;
      for (int i = 1; i <= iterations; i++)
        result /= i;
    }
    else {
      dResult = Long.MAX_VALUE-12345L;
      for (int i = 1; i <= iterations; i++)
        dResult /= i;
    }
  }
  // Required for JUnit
  public void testLongDivision() {
  }


  public void testLongEverything(@RangeField("iterationsRange") Integer iterations, @RangeField("typeRange") Integer useLong) {
    if (useLong == 1) {
      result = 12345L;
      for (int i = 1; i <= iterations; i++)
        result = result * i - 123L + (result / i);
    }
    else {
      dResult = 12345d;
      for (int i = 1; i <= iterations; i++) {
        dResult = dResult * i - 123d + (dResult / i);
      }
    }
  }
  // Required for JUnit
  public void testLongEverything() {
  }

}