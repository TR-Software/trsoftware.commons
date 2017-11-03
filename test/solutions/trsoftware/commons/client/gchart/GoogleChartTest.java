package solutions.trsoftware.commons.client.gchart;
/**
 *
 * Date: Sep 7, 2008
 * Time: 11:28:04 PM
 * @author Alex
 */

import junit.framework.TestCase;

public class GoogleChartTest extends TestCase {

  public void testGChartExtendedEncode() throws Exception {
    assertEquals("AA", GoogleChart.extendedEncode(0));
    assertEquals("AH", GoogleChart.extendedEncode(7));
    assertEquals("CF", GoogleChart.extendedEncode(133));
    assertEquals("-H", GoogleChart.extendedEncode(3975));
    assertEquals(".F", GoogleChart.extendedEncode(4037));
    assertEquals("..", GoogleChart.extendedEncode(4095));
  }

  public void testGChartExtendedEncodePercentage() throws Exception {
    assertEquals("AA", GoogleChart.extendedEncodePercentage(0.0));
    assertEquals("AH", GoogleChart.extendedEncodePercentage(0.0017094017094017094017094017094017));
    assertEquals("CF", GoogleChart.extendedEncodePercentage(0.032478632478632478632478632478632));
    assertEquals("-H", GoogleChart.extendedEncodePercentage(0.97069597069597069597069597069597));
    assertEquals(".F", GoogleChart.extendedEncodePercentage(0.98583638583638583638583638583639));
    assertEquals("..", GoogleChart.extendedEncodePercentage(1.0));
  }
}