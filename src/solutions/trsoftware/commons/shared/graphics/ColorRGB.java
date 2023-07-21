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

import solutions.trsoftware.commons.shared.util.MathUtils;
import solutions.trsoftware.commons.shared.util.stats.MinAndMaxDouble;

import java.io.Serializable;

/**
 * Represents a color specified as RGB, and provides methods for getting its components and
 * constructing an HTML color (hex) literal.  This class is immutable.
 *
 * @author Alex
 * @see ColorHSL
 */
public final class ColorRGB implements Serializable {

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

  /**
   * The internal representation of the RGBA color:
   * the alpha component in bits 24-31, the red component in bits 16-23,
   * the green component in bits 8-15, and the blue component in bits 0-7.
   * <p>
   * NOTE: this representation was chosen to match the storage format used by {@link java.awt.Color}
   *
   * @see #getRGB()
   */
  private int value;  // Note: not declared final to support GWT serialization

  private ColorRGB() {  // default constructor for serialization
  }

  /**
   * Constructs a new instance from the given RGB components.
   * The given components will be forced into the range {@code [0,255]} if they're not in this range already.
   *
   * @param r red (range: [0, 255])
   * @param g green (range: [0, 255])
   * @param b blue (range: [0, 255])
   */
  public ColorRGB(int r, int g, int b) {
    this(r, g, b, 0xFF);
  }

  /**
   * Constructs a new instance from the given RGBA components.
   * NOTE: the given components will be forced into the range {@code [0,255]} if they're not in this range already.
   *
   * @param r red (range: [0, 255])
   * @param g green (range: [0, 255])
   * @param b blue (range: [0, 255])
   * @param a alpha (range: [0, 255])
   */
  public ColorRGB(int r, int g, int b, int a) {
    value = ((coerceComponent(a) & 0xFF) << 24) |
        ((coerceComponent(r) & 0xFF) << 16) |
        ((coerceComponent(g) & 0xFF) << 8) |
        ((coerceComponent(b) & 0xFF));
  }

  /**
   * Creates an opaque sRGB color with the specified combined RGB value
   * consisting of the red component in bits 16-23, the green component
   * in bits 8-15, and the blue component in bits 0-7.  The actual color
   * used in rendering depends on finding the best match given the
   * color space available for a particular output device.  Alpha is
   * defaulted to 255.
   *
   * @param rgb the combined RGB components
   * @see java.awt.Color#Color(int)
   * @see java.awt.image.ColorModel#getRGBdefault
   * @see #getRed
   * @see #getGreen
   * @see #getBlue
   * @see #getRGB
   */
  public ColorRGB(int rgb) {
    this(rgb, false);
  }

  /**
   * Creates an sRGB color with the specified combined RGBA value consisting
   * of the alpha component in bits 24-31, the red component in bits 16-23,
   * the green component in bits 8-15, and the blue component in bits 0-7.
   * If the <code>hasAlpha</code> argument is <code>false</code>, alpha
   * is defaulted to 255.
   *
   * @param rgba the combined RGBA components
   * @param hasAlpha <code>true</code> if the alpha bits are valid;
   *     <code>false</code> otherwise
   * @see java.awt.Color#Color(int, boolean)
   * @see java.awt.image.ColorModel#getRGBdefault
   * @see #getRed
   * @see #getGreen
   * @see #getBlue
   * @see #getAlpha
   * @see #getRGB
   */
  public ColorRGB(int rgba, boolean hasAlpha) {
    if (hasAlpha)
      value = rgba;
    else
      value = 0xff000000 | rgba;
  }

  /**
   * Ensures that an R/G/B/A component is within range [0, 255]
   * @return the given value (if it's in range), 0 (if it's negative), or 255 (if it's > 255)
   */
  public static int coerceComponent(int component) {
    return MathUtils.restrict(component, 0, 0xFF);
  }

  public ColorHSL toHSL() {
    return toHSL(this.getRed(), this.getGreen(), this.getBlue());
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

  /**
   * @return a hex string that can be used to represent this color in HTML/CSS. <strong>NOTE:</strong> browser support
   *     for representing alpha in hex is still limited (see <a href="https://caniuse.com/#feat=css-rrggbbaa">browser
   *     support table</a>)
   * @see <a href="https://en.wikipedia.org/wiki/Web_colors#Hex_triplet">Wikipedia: Web colors &raquo; Hex triplet</a>
   */
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder("#")
        .append(toHex(getRed()))
        .append(toHex(getGreen()))
        .append(toHex(getBlue()));
    // add alpha only if needed (i.e. it's < 0xff)
    if (getAlpha() != 0xFF) {
      str.append(toHex(getAlpha()));
    }
    return str.toString();
  }

  private static String toHex(int i) {
    String hex = Integer.toHexString(i);
    if (hex.length() == 1)
      hex = "0" + hex;
    return hex;
  }

  /**
   * Parses HTML/CSS hex string specifying a color.
   *
   * @param htmlHexString a hex string like {@code #abc} or {@code #aabbcc}
   * @return an instance whose RGB components were derived from the given string
   * @throws IllegalArgumentException if the argument doesn't have the expected format
   * @see <a href="https://en.wikipedia.org/wiki/Web_colors#Hex_triplet">Wikipedia: Web colors &raquo; Hex triplet</a>
   */
  public static ColorRGB valueOf(String htmlHexString) {
    String hexPart = htmlHexString.trim().substring(htmlHexString.indexOf("#") + 1);
    switch (hexPart.length()) {
      case 3:
        // RGB shorthand (each hex digit needs to be doubled up)
        return new ColorRGB(
            parseHex(hexPart.substring(0, 1)),
            parseHex(hexPart.substring(1, 2)),
            parseHex(hexPart.substring(2, 3)));
      case 4:
        // RGBA shorthand (each hex digit needs to be doubled up); NOTE: browser support is still limited (see https://caniuse.com/#feat=css-rrggbbaa)
        return new ColorRGB(
            parseHex(hexPart.substring(0, 1)),
            parseHex(hexPart.substring(1, 2)),
            parseHex(hexPart.substring(2, 3)),
            parseHex(hexPart.substring(3, 4)));
      case 6:
        // RGB longhand
        return new ColorRGB(
            parseHex(hexPart.substring(0, 2)),
            parseHex(hexPart.substring(2, 4)),
            parseHex(hexPart.substring(4, 6)));
      case 8:
        // RGBA longhand; NOTE: browser support is still limited (see https://caniuse.com/#feat=css-rrggbbaa)
        return new ColorRGB(
            parseHex(hexPart.substring(0, 2)),
            parseHex(hexPart.substring(2, 4)),
            parseHex(hexPart.substring(4, 6)),
            parseHex(hexPart.substring(6, 8)));
      default:
        throw new IllegalArgumentException("Malformatted hex color: " + htmlHexString);
    }
  }

  /**
   * Parses the given hex component, expanding shorthand if needed (if the argument contains only 1 char, it
   * will be doubled up)
   *
   * @see <a href="https://en.wikipedia.org/wiki/Web_colors#Shorthand_hexadecimal_form">Wikipedia: Web colors &raquo;
   *     Shorthand hexadecimal form</a>
   */
  private static int parseHex(String hex) {
    if (hex.length() == 1)
      hex = hex + hex;  // expand hex shorthand by doubling up the char
    return Integer.parseInt(hex, 16);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ColorRGB colorRGB = (ColorRGB)o;

    return value == colorRGB.value;
  }

  @Override
  public int hashCode() {
    return value;
  }

  /**
   * Returns the internal representation of this RGBA value representing the color in the default sRGB
   * {@link java.awt.image.ColorModel}:
   * <table border=1>
   * <tr>
   * <th colspan=4 align=center>Bits</th>
   * </tr>
   * <tr>
   * <th>31 - 24</th>
   * <th>23 - 16</th>
   * <th>15 - 8</th>
   * <th>7 - 0</th>
   * </tr>
   * <tr>
   * <td>alpha</td>
   * <td>red</td>
   * <td>green</td>
   * <td>blue</td>
   * </tr>
   * </table>
   * (This is the same as the internal representation of {@link java.awt.Color}).
   *
   * @return the RGB value of the color in the default sRGB {@link java.awt.image.ColorModel}
   *     (bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue)
   * @see java.awt.Color#getRGB()
   * @see java.awt.image.ColorModel#getRGBdefault
   * @see #getRed
   * @see #getGreen
   * @see #getBlue
   */
  public int getRGB() {
    return value;
  }

  /**
   * Returns the red component in the range 0-255 in the default sRGB
   * space.
   *
   * @return the red component.
   * @see #getRGB
   */
  public int getRed() {
    return (getRGB() >> 16) & 0xFF;
  }

  /**
   * Returns the green component in the range 0-255 in the default sRGB
   * space.
   *
   * @return the green component.
   * @see #getRGB
   */
  public int getGreen() {
    return (getRGB() >> 8) & 0xFF;
  }

  /**
   * Returns the blue component in the range 0-255 in the default sRGB
   * space.
   *
   * @return the blue component.
   * @see #getRGB
   */
  public int getBlue() {
    return getRGB() & 0xFF;
  }

  /**
   * Returns the alpha component in the range 0-255.
   *
   * @return the alpha component.
   * @see #getRGB
   */
  public int getAlpha() {
    return (getRGB() >> 24) & 0xff;
  }
}
