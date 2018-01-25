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

import solutions.trsoftware.commons.shared.util.stats.MinAndMaxDouble;

/**
 * Represents a color specified as RGB, and provides methods for getting its components and
 * constructing an HTML color literal.  This class is immutable.
 *
 * @see ColorHSL
 * @author Alex
 */
public final class ColorRGB {

  public final static ColorRGB WHITE = new ColorRGB(255, 255, 255);
  public final static ColorRGB LIGHT_GRAY = new ColorRGB(192, 192, 192);
  public final static ColorRGB GRAY = new ColorRGB(128, 128, 128);
  public final static ColorRGB DARK_GRAY = new ColorRGB(64, 64, 64);
  public final static ColorRGB BLACK = new ColorRGB(0, 0, 0);
  public final static ColorRGB RED = new ColorRGB(255, 0, 0);
  public final static ColorRGB PINK = new ColorRGB(255, 175, 175);
  public final static ColorRGB ORANGE = new ColorRGB(255, 200, 0);
  public final static ColorRGB YELLOW = new ColorRGB(255, 255, 0);
  public final static ColorRGB GREEN = new ColorRGB(0, 255, 0);
  public final static ColorRGB MAGENTA = new ColorRGB(255, 0, 255);
  public final static ColorRGB CYAN = new ColorRGB(0, 255, 255);
  public final static ColorRGB BLUE = new ColorRGB(0, 0, 255);
  public final static ColorRGB BISQUE = new ColorRGB(255, 228, 196);

  public static final int MAX_COMPONENT_VALUE = 255;

  public final int r, g, b;

  private final NumberRange<Integer> componentValueRange = new NumberRange<Integer>(0, MAX_COMPONENT_VALUE);

  /**
   * The components will be cut down to the range {@code [0,255]} if they're not in this range.
   */
  public ColorRGB(int r, int g, int b) {
    this.r = componentValueRange.coerce(r);
    this.g = componentValueRange.coerce(g);
    this.b = componentValueRange.coerce(b);
  }

  public ColorHSL toHSL() {
    return toHSL(this.r, this.g, this.b);
  }

  public static ColorHSL toHSL(double r, double g, double b) {
    // code adapted from https://gist.github.com/mjackson/5311256
    r /= 255;
    g /= 255;
    b /= 255;

    double min, max;
    {
      MinAndMaxDouble minMax = new MinAndMaxDouble();
      minMax.update(r);
      minMax.update(g);
      minMax.update(b);
      min = minMax.getMin();
      max = minMax.getMax();
    }

    double h, s, l = (min + max) / 2;

    if (max == min) {
      h = s = 0; // achromatic
    }
    else {
      double d = max - min;
      s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
      if (max == r)
        h = (g - b) / d + (g < b ? 6 : 0);
      else if (max == g)
        h = (b - r) / d + 2;
      else {
        assert max == b;
        h = (r - g) / d + 4;
      }
      h /= 6;
    }
    // the above algorithm gives h as a fraction of a circle (360 degrees)
    return new ColorHSL(h * 360, s, l);
  }

  @Override
  public String toString() {
    return "#" + toHex(r) + toHex(g) + toHex(b);
  }

  private String toHex(int i) {
    String hex = Integer.toHexString(i);
    if (hex.length() == 1)
      hex = "0" + hex;
    return hex;
  }

  /**
   * Parses HTML/CSS hex string specifying a color.
   * @param htmlHexString a hex string like {@code #abc} or {@code #aabbcc}
   * @return an instance whose RGB components were derived from the given string
   * @throws IllegalArgumentException if the argument doesn't have the expected format
   */
  public static ColorRGB valueOf(String htmlHexString) {
    String hexPart = htmlHexString.trim().substring(htmlHexString.indexOf("#") + 1);
    if (hexPart.length() == 3) {
      return new ColorRGB(
          Integer.valueOf(hexPart.substring(0,1), 16),
          Integer.valueOf(hexPart.substring(1,2), 16),
          Integer.valueOf(hexPart.substring(2,3), 16));
    }
    else if (hexPart.length() == 6) {
      return new ColorRGB(
          Integer.valueOf(hexPart.substring(0,2), 16),
          Integer.valueOf(hexPart.substring(2,4), 16),
          Integer.valueOf(hexPart.substring(4,6), 16));
    }
    else {
      throw new IllegalArgumentException("Malformatted hex color");
    }
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ColorRGB color = (ColorRGB)o;

    if (b != color.b) return false;
    if (g != color.g) return false;
    if (r != color.r) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = r;
    result = 31 * result + g;
    result = 31 * result + b;
    return result;
  }
}
