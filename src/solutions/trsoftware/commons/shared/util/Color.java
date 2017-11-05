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

/**
 * Represents a color, and provides methods for getting its components and
 * constructing an HTML color literal.  This class is immutable.
 *
 * @author Alex
 */
public final class Color {

  public final static Color WHITE = new Color(255, 255, 255);
  public final static Color LIGHT_GRAY = new Color(192, 192, 192);
  public final static Color GRAY = new Color(128, 128, 128);
  public final static Color DARK_GRAY = new Color(64, 64, 64);
  public final static Color BLACK = new Color(0, 0, 0);
  public final static Color RED = new Color(255, 0, 0);
  public final static Color PINK = new Color(255, 175, 175);
  public final static Color ORANGE = new Color(255, 200, 0);
  public final static Color YELLOW = new Color(255, 255, 0);
  public final static Color GREEN = new Color(0, 255, 0);
  public final static Color MAGENTA = new Color(255, 0, 255);
  public final static Color CYAN = new Color(0, 255, 255);
  public final static Color BLUE = new Color(0, 0, 255);
  
  public final int r, g, b;

  private final NumberRange<Integer> componentValueRange = new NumberRange<Integer>(0, 255);

  /**
   * The components will be cut down to the range 0..255 if they're not in this
   * range.
   */
  public Color(int r, int g, int b) {
    this.r = componentValueRange.coerce(r);
    this.g = componentValueRange.coerce(g);
    this.b = componentValueRange.coerce(b);
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

  public static Color valueOf(String htmlHexString) {
    String hexPart = htmlHexString.trim().substring(htmlHexString.indexOf("#") + 1);
    if (hexPart.length() == 3) {
      return new Color(
          Integer.valueOf(hexPart.substring(0,1), 16),
          Integer.valueOf(hexPart.substring(1,2), 16),
          Integer.valueOf(hexPart.substring(2,3), 16));
    }
    else if (hexPart.length() == 6) {
      return new Color(
          Integer.valueOf(hexPart.substring(0,2), 16),
          Integer.valueOf(hexPart.substring(2,4), 16),
          Integer.valueOf(hexPart.substring(4,6), 16));
    }
    else {
      throw new IllegalArgumentException("Hexidecimal color string must be either length 3 or 6");
    }
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Color color = (Color)o;

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
