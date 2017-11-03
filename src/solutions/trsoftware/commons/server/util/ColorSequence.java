package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.awt.*;

/**
 * Generates a random sequence of unique colors, such that each color has a distinct hue.
 * Uses the "golden ratio" algorithm (described in the blog post linked below)
 * to select consecutive hues as far apart on the color wheel as possible (which maximizes the difference between colors
 * when the number of items is small, while evenly distributing colors around the spectrum otherwise, and ensuring
 * that the same color never repeats twice.

 * @see <a href="http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/">http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/</a>
 * @see <a href="http://devmag.org.za/2012/07/29/how-to-choose-colours-procedurally-algorithms/">http://devmag.org.za/2012/07/29/how-to-choose-colours-procedurally-algorithms/</a>
 * @see <a href="https://en.wikipedia.org/wiki/HSL_and_HSV">https://en.wikipedia.org/wiki/HSL_and_HSV</a>
 *
 * @author Alex, 8/3/2017
 */
public class ColorSequence {

  private static double addend = 0.618033988749895;  // will be added (mod 1) to generate the next color hue in the sequence (default value is the "golden ratio conjugate")
  private double nextHue;

  public ColorSequence() {
  }

  /**
   * @return An HTML RGB color string representing the next color in the sequence.
   */
  public String nextColor() {
    double hue = nextHue;
    nextHue = (hue + addend) % 1;
    // sat between 0.1 and 0.3
    double saturation = (RandomUtils.rnd.nextInt(2000) + 1000) / 10000d;
    double luminance = 0.9d;
    Color color = Color.getHSBColor((float)hue, (float)saturation, (float)luminance);
    return '#' + Integer.toHexString(
        (color.getRGB() & 0xffffff) | 0x1000000).substring(1);
  }

}
