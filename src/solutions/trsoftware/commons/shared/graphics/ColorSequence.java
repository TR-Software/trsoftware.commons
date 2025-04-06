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

package solutions.trsoftware.commons.shared.graphics;

import solutions.trsoftware.commons.shared.util.RandomUtils;

/**
 * Generates a pseudo-random sequence of unique colors, such that each color has a distinct hue.
 * <p>
 * Uses the "golden ratio" algorithm (described in the blog post linked below) to select consecutive hues to
 * maximize contrast when the number of items is small, while evenly distributing colors around the spectrum otherwise,
 * and ensuring that the same color never repeats twice.
 * </p>
 * @see <a href="http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/">http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/</a>
 * @see <a href="http://devmag.org.za/2012/07/29/how-to-choose-colours-procedurally-algorithms/">http://devmag.org.za/2012/07/29/how-to-choose-colours-procedurally-algorithms/</a>
 * @see <a href="https://en.wikipedia.org/wiki/HSL_and_HSV">https://en.wikipedia.org/wiki/HSL_and_HSV</a>
 *
 * @author Alex, 8/3/2017
 */
public class ColorSequence {

  /**
   * The "golden ratio conjugate."  Will be added (mod 1) to generate the next color hue in the sequence.
   *
   */
  private static final double addend = 0.618033988749895;
  private double nextHue;

  /**
   * The sequence will start at hue = 0
   */
  public ColorSequence() {
    this(0);
  }

  /**
   * The sequence will be seeded with the given hue
   *
   * @param startingHue should be in range [0, 1]
   */
  public ColorSequence(double startingHue) {
    this.nextHue = startingHue;
  }

  /**
   * Copy constructor
   */
  public ColorSequence(ColorSequence other) {
    nextHue = other.nextHue;
  }

  /**
   * @return An HTML/CSS RGB color hex string representing the next color in the sequence.
   * @see <a href="https://en.wikipedia.org/wiki/Web_colors#Hex_triplet">Wikipedia: Web colors &raquo; Hex triplet</a>
   * @see ColorRGB#toString()
   */
  public String nextColorHex() {
    return nextColor().toString();
  }

  /**
   * @return the next color in the sequence.
   * @see ColorRGB
   */
  public ColorRGB nextColor() {
    double hue = nextHue;
    nextHue = (hue + addend) % 1;
    // sat between 0.1 and 0.3
    double saturation = (RandomUtils.rnd().nextInt(2000) + 1000) / 10000d;
    double brightness = 0.9d;
    // TODO: try using HSL instead of HSB?
    int sRGB = ColorUtils.HSBtoRGB((float)hue, (float)saturation, (float)brightness);
    return new ColorRGB(sRGB);
  }

}
