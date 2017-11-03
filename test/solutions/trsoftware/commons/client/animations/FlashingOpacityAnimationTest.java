package solutions.trsoftware.commons.client.animations;

import solutions.trsoftware.commons.client.util.StringUtils;
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