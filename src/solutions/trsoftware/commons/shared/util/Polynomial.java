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

/**
 * A polynomial represented as an array of doubles.
 *
 * This class is immutable.
 *
 * @author Alex
 */
public class Polynomial {
  /** The value at index i is the coefficient for x^i */
  private final double[] coefficients;

  /**
   * @param coefficients The value at index i is the coefficient for x^i
   */
  public Polynomial(double[] coefficients) {
    if (coefficients == null || coefficients.length == 0)
      this.coefficients = new double[]{0};  // 0-polynomial
    else
      this.coefficients = coefficients;
  }


  /** Factory method which allows creating the polynomial with coefficients specified in a human-readable order, high to low */
  public static Polynomial fromCoefficientsDescending(double... coefficients) {
    // reverse the coefficients, because the constructor expects them ordered low to high
    int n = coefficients.length;
    double[] reverse = new double[n];
    for (int i = 0; i < reverse.length; i++) {
      reverse[i] = coefficients[n-1-i];
    }
    return new Polynomial(reverse);

  }

  /** The degree of the polynomial, which is the value of the largest coefficient */
  public int degree() {
    return coefficients.length + 1;
  }

  /** Computes the y-value of the polynomial at the given x-value */
  public double evaluate(double x) {
    double sum = coefficients[0];  // we can be sure there is at least one coefficient defined
    double xpower = x;  // accumulator for the current power of x; starts at 1; will be multiplied by x on each iteration; this dynamic programming approach should be faster than calling Math.pow(x, i) each time
    for (int i = 1; i < coefficients.length; i++) {  // we start the loop at index 1 to reduce the number of multiplications
      sum += coefficients[i] * xpower;
      xpower *= x;
    }
    return sum;
  }
}
