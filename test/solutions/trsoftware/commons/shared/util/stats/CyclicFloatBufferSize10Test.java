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

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter;
import solutions.trsoftware.commons.client.bridge.text.NumberFormatter;
import solutions.trsoftware.commons.server.util.PrimitiveFloatArrayList;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex, 1/2/14
 */
public class CyclicFloatBufferSize10Test extends TestCase {

  private CyclicFloatBufferSize10 buf;
  private ArrayList<Float> valuesAdded;

  public void setUp() throws Exception {
    super.setUp();
    buf = new CyclicFloatBufferSize10();
    valuesAdded = new ArrayList<Float>();
  }

  public void testInit() throws Exception {
    assertEquals(0, buf.size());
    assertEquals(0d, buf.mean());
    checkBounds();
  }

  public void testAdd() throws Exception {
    // try adding a bunch of values, verifying assertions every time
    NumberFormatter nf = AbstractNumberFormatter.getInstance(2, 0, 0, false, false);
    for (int i = 0; i < 35; i++) {
      float newValue = Float.parseFloat("1." + nf.format(i));
      System.out.println("newValue = " + newValue);
      buf.add(newValue);
      valuesAdded.add(newValue);
      ArrayList<Float> bufAsList = buf.asList();
      System.out.println(bufAsList);
      assertEquals(Math.min(10, i + 1), buf.size());
      assertEquals(newValue, buf.get(Math.min(9, i)));
      assertEquals(meanOfLast10(), buf.mean(), .0001f);
      List<Float> last10List = listOfLast10();
      assertEquals(last10List, bufAsList);
      assertEquals(last10List, new PrimitiveFloatArrayList(buf.asArray()));
    }
  }

  /** Computes the mean of a list of floats */
  private double meanOfLast10() {
    List<Float> last10 = listOfLast10();
    double sum = 0;
    double n = 0;
    for (Float f : last10) {
      sum += f;
      n++;
    }
    return sum / n;
  }

  private List<Float> listOfLast10() {
    int size = valuesAdded.size();
    return size <= 10 ? valuesAdded : valuesAdded.subList(size - 10, size);
  }

  private void checkBounds() {
    int size = buf.size();
    for (int i = 0; i < 20; i++) {
      final int idx = i + size;
      AssertUtils.assertThrows(IndexOutOfBoundsException.class, new Runnable() {
        @Override
        public void run() {
          buf.get(idx);
        }
      });
    }
    assertEquals(size, buf.size());  // make sure the size wasn't modified as a result of this test
  }

/*

Performance Testing Results of 4 CyclicFloatBuffer implementations.

I. Methodology

CyclicFloatBufferSize10: A class with the constant value 10 placed directly in the code wherever it's needed. Also, this
                         class does not inherit from any abstract class, only an interface (that allows the comparisons).
CyclicFloatBufferSize10_2: The value 10 is returned by an abstract method implemented from an abstract class that
                           contains all the logic.
CyclicFloatBufferFixedSize: The value is represented as a final instance field. No inheritance at all, just like
                            CyclicFloatBufferSize10
CyclicFloatBufferFixedSize_2: Extends the same abstract class as CyclicFloatBufferSize10_2, but the max size method
                              returns the value of a final instance field


II. Results

CPU benchmarks results
                        name run count   total time  per 1000 multiplier
     CyclicFloatBufferSize10     4,096       119 ms     29 ms       1.00
   CyclicFloatBufferSize10_2     4,096       130 ms     31 ms       1.09
  CyclicFloatBufferFixedSize     4,096       167 ms     40 ms       1.40
CyclicFloatBufferFixedSize_2     2,048       101 ms     49 ms       1.71

Memory benchmarks results
                        name run count total memory  per 1000 multiplier
     CyclicFloatBufferSize10   524,288     40.00 MB  78.13 KB       1.00
   CyclicFloatBufferSize10_2   524,288     40.00 MB  78.13 KB       1.00
  CyclicFloatBufferFixedSize   524,288     44.00 MB  85.94 KB       1.10
CyclicFloatBufferFixedSize_2   524,288     44.00 MB  85.94 KB       1.10

III. Conclusions

It seems that both the implementations that get the size via method call (*_2) are slightly slower than their counterparts
that don't use a method call.  The same is true with and without the -server flag.

However, it's clear that having the size hardcoded is at least 50% faster than having it stored in a field.  This shows
that accessing a field in Java is 50% slower than a hard-coded constant or a method call that returns a hard-coded constant.

In terms of memory, the results are as expected: specifying the size in a field uses 10% more memory.

*/
}
