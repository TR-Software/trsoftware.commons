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

package solutions.trsoftware.commons.client.animations;

import junit.framework.TestCase;

/**
 * @author Alex, 9/23/2017
 */
public class FlashingOpacityAnimationTest extends TestCase {
  public void testOnUpdate() throws Exception {
    // TODO: temp: figure out how to use sin/cos to generate the periodic progress
    double limit = 1;
    double nFlashes = 5;
    for (double i = 0; i <= limit; i+=.001) {
      double x = Math.PI * i * nFlashes;
      double cosX = Math.abs(Math.cos(x));
      System.out.printf("%.2f: cos(%.3f) = %.3f; sin(%.3f) = %.3f%n", i, x, cosX, x, Math.abs(Math.sin(x)));
      /*
      cosX == 1.0 at the following i values:
        0, .2, .4, .6, .8, 1.0
      */
    }
  }

}