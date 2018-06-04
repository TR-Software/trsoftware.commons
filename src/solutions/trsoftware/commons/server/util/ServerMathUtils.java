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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.Random;

/**
 * Mar 19, 2010
 *
 * @author Alex
 */
public class ServerMathUtils {
  /**
   * @return the next pseudo-random number in the given generator's sequence,
   * coerced into a Gaussian distribution with the given {@code mean} and {@code stdev}.
   * @deprecated use {@link RandomUtils#nextGaussian(double, double)} or {@link RandomUtils#nextGaussian(int, int)}
   */
  public static double randomGaussian(Random rnd, double mean, double stdev) {
    return rnd.nextGaussian() * stdev + mean;
  }
}
