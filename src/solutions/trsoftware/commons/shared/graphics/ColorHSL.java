/*
 * Copyright 2022 TR Software Inc.
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

import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.ValueBox;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import java.text.ParseException;

/**
 * Represents a color specified as HSL, and provides methods for getting its components and
 * constructing a CSS color literal.
 *
 * @see <a href="https://en.wikipedia.org/wiki/HSL_and_HSV">Widipedia: HSL and HSV</a>
 * @see ColorRGB
 * @author Alex
 */
public final class ColorHSL {

  /**
   * The {@link #h}, {@link #s}, {@link #l} component values will be rounded to this number of decimal places when
   * working with string representations (parsing and rendering), as well as comparisons using {@link #equals(Object)}
   * and {@link #hashCode()}.
   */
  public static final int MAX_FRACTION_DIGITS = 2;

  public enum Component {HUE, SATURATION, LIGHTNESS}

  /** Hue (specified in degrees, range: [0, 360]) */
  private double h;
  /** Saturation (as a percentage, range: [0, 1]) */
  private double s;
  /** Lightness (as a percentage, range: [0, 1]) */
  private double l;

  /**
   * @param h hue (specified in degrees, range: [0, 360])
   * @param s saturation (as a percentage, range: [0,1])
   * @param l lightness (as a percentage, range: [0,1])
   */
  public ColorHSL(double h, double s, double l) {
    this.h = h;
    this.s = s;
    this.l = l;
  }

  public ColorHSL(Number... components) {
    this(components[0].doubleValue(), components[1].doubleValue(), components[2].doubleValue());
  }

  public double getH() {
    return h;
  }

  public void setH(double h) {
    this.h = h;
  }

  public double getS() {
    return s;
  }

  public void setS(double s) {
    this.s = s;
  }

  public double getL() {
    return l;
  }

  public void setL(double l) {
    this.l = l;
  }

  public ColorHSL copy() {
    return new ColorHSL(h, s, l);
  }

  public double get(Component component) {
    switch (component) {
      case HUE:
        return h;
      case SATURATION:
        return s;
      case LIGHTNESS:
        return l;
      default:
        throw new IllegalArgumentException();
    }
  }

  public void set(Component component, double value) {
    switch (component) {
      case HUE:
        h = value;
        break;
      case SATURATION:
        s = value;
        break;
      case LIGHTNESS:
        l = value;
        break;
    }
  }

  /**
   * @return the CSS color value, e.g. {@code hsl(hue, saturation%, lightness%)}
   */
  @Override
  public String toString() {
    return getRenderer().render(this);
  }

  private String renderComponents() {
    return Renderer.renderComponents(this);
  }

  /**
   * @param str a CSS HSL color value, e.g. {@code hsl(hue, saturation%, lightness%)}
   * @return the instance of this class corresponding to the given string
   * @see #toString()
   */
  public static ColorHSL valueOf(String str) {
    try {
      return getParser().parse(str);
    }
    catch (ParseException e) {
      return null;
    }
  }

  private static Renderer renderer;

  public static Renderer getRenderer() {
    if (renderer == null)
      renderer = new Renderer();
    return renderer;
  }

  private static Parser parser;

  public static Parser getParser() {
    if (parser == null)
      parser = new Parser();
    return parser;
  }

  public static class Renderer extends AbstractRenderer<ColorHSL> {
    private static final SharedNumberFormat degFormat = new SharedNumberFormat(1, 0, MAX_FRACTION_DIGITS, false);
    private static final SharedNumberFormat pctFormat = new SharedNumberFormat(1, 0, MAX_FRACTION_DIGITS, true);
    @Override
    public String render(ColorHSL object) {
      if (object == null)
        return "";
      return "hsl(" + renderComponents(object) + ")";
    }

    private static String renderComponents(ColorHSL object) {
      return StringUtils.join(", ", degFormat.format(object.h), pctFormat.format(object.s), pctFormat.format(object.l));
    }
  }

  public static class Parser implements com.google.gwt.text.shared.Parser<ColorHSL> {
    private static final String number = "\\d*\\.?\\d*";
    private static final RegExp regEx = RegExp.compile("hsl\\((" + number + "), (" + number + "%), (" + number + "%)\\)");
    @Override
    public ColorHSL parse(CharSequence text) throws ParseException {
      if (text != null) {
        String input = text.toString();
        MatchResult match = regEx.exec(input);
        if (match != null) {
          SharedNumberFormat deg = Renderer.degFormat;
          SharedNumberFormat pct = Renderer.pctFormat;
          return new ColorHSL(
              deg.parse(match.getGroup(1)),
              pct.parse(match.getGroup(2)),
              pct.parse(match.getGroup(3)));
        }
      }
      return null;
    }
  }

  /**
   * Uses the string representation to avoid double precision issues
   * (e.g. {@code 0.5013} might be parsed as {@code 0.5013000000000001}).
   * @return {@code true} iff {@code o} is an instance of this class and has the same string repr
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    return renderComponents().equals(((ColorHSL)o).renderComponents());
  }

  @Override
  public int hashCode() {
    return renderComponents().hashCode();
  }

  public ColorRGB toRGB() {
    return toRGB(h, s, l);
  }

  public static ColorRGB toRGB(double h, double s, double l) {
    // code adapted from https://gist.github.com/mjackson/5311256
    // have to massage the hue (which we represent in degrees), since the original JS implementation expects that "h, s, and l are contained in the set [0, 1]"
    h /= 360;
    double r, g, b;
    if (s == 0) {
      r = g = b = l; // achromatic
    }
    else {
      double q = l < 0.5 ? l * (1 + s) : l + s - l * s;
      double p = 2 * l - q;
      r = hue2rgb(p, q, h + 1d / 3);
      g = hue2rgb(p, q, h);
      b = hue2rgb(p, q, h - 1d / 3);
    }

    return new ColorRGB(Math.round((float)(r * 255)), Math.round((float)(g * 255)), Math.round((float)(b * 255)));
  }

  private static double hue2rgb(double p, double q, double t) {
    if (t < 0)
      t += 1;
    if (t > 1)
      t -= 1;
    if (t < 1d / 6)
      return p + (q - p) * 6 * t;
    if (t < 1d / 2)
      return q;
    if (t < 2d / 3)
      return p + (q - p) * (2d / 3 - t) * 6;
    return p;
  }

  /**
   * A {@link ValueBox} for input of {@link ColorHSL} values.
   */
  public static class InputBox extends ValueBox<ColorHSL> {
    public InputBox() {
      super(Document.get().createTextInputElement(), getRenderer(), getParser());
    }
  }
}
