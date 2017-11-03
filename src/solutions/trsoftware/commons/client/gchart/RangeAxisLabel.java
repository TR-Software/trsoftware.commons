package solutions.trsoftware.commons.client.gchart;

import java.util.Map;

/**
 * Date: Jun 22, 2008 Time: 7:22:10 PM
*
* @author Alex
*/
public class RangeAxisLabel extends AxisLabel {
  protected int[] range;

  public RangeAxisLabel(String name, String style, int[] range) {
    super(name, style);
    assert range != null;
    assert range.length == 2;
    this.range = range;
  }

  /** Appends parameter parts for this axis to the overall parameter map */
  @Override
  protected void appendToParamMapImpl(Map<String, String> paramMap) {
    appendAxisParameter(paramMap, GoogleChart.AXIS_LABEL_RANGES, range[0] + "," + range[1], ",", "|");
  }
}
