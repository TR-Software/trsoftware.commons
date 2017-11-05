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

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

/**
 * May 11, 2009
 *
 * @author Alex
 */
public class PolynomialTest extends TestCase {

  public void testEvaluate() throws Exception {
    {
      Polynomial p = new Polynomial(new double[]{1, 2});  // 2x + 1
      assertEquals(1d, p.evaluate(0), 0.000001);
      assertEquals(3d, p.evaluate(1), 0.000001);
      assertEquals(6d, p.evaluate(2.5), 0.000001);
    }
    {
      Polynomial p = new Polynomial(new double[]{0, 1});  // 1x + 0  a.k.a the identity function
      // test a few values in the range -10..10, and make sure the identity holds
      for (double x = -10; x < 10; x += .01) {
        assertEquals(x, p.evaluate(x), 0.000001);
      }
    }
    {
      Polynomial p = new Polynomial(new double[]{0, -1});  // -1x + 0  a.k.a the inverse function
      // test a few values in the range -10..10, and make sure the identity holds
      for (double x = -10; x < 10; x += .01) {
        assertEquals(-x, p.evaluate(x), 0.000001);
      }
    }
    {
      Polynomial p = new Polynomial(new double[]{1, 2, -3});  // -3x^2 + 2x + 1
      assertEquals(1d, p.evaluate(0), 0.000001);
      assertEquals(0d, p.evaluate(1), 0.000001);
      assertEquals(-7d, p.evaluate(2), 0.000001);
    }
    {
      Polynomial p = new Polynomial(new double[]{5});  // 5
      assertEquals(5d, p.evaluate(0), 0.000001);
      assertEquals(5d, p.evaluate(1), 0.000001);
      assertEquals(5d, p.evaluate(2), 0.000001);
    }
  }

  public void testFromCoefficientsDescending() throws Exception {
    {
      Polynomial p = Polynomial.fromCoefficientsDescending(-3, 2, 1);  // -3x^2 + 2x + 1
      assertEquals(1d, p.evaluate(0), 0.000001);
      assertEquals(0d, p.evaluate(1), 0.000001);
      assertEquals(-7d, p.evaluate(2), 0.000001);
    }
    {
      Polynomial p = Polynomial.fromCoefficientsDescending(5);  // 5
      assertEquals(5d, p.evaluate(0), 0.000001);
      assertEquals(5d, p.evaluate(1), 0.000001);
      assertEquals(5d, p.evaluate(2), 0.000001);
    }
  }
}