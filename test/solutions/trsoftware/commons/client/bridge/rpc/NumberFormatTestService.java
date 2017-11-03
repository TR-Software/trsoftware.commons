package solutions.trsoftware.commons.client.bridge.rpc;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Allows comparing the output of GWT's NumberFormat to that of
 * java.text.NumberFormat by running the former client-side
 * and the latter server-side.
 *
 * @author Alex
 */
public interface NumberFormatTestService extends RemoteService {

  /**
   * Returns the output of invoking java.text.NumberFormat with the
   * given parameters.
   * @return The number formatted with java.text.NumberFormat using the given
   * tuning parameters.
   */
  public String formatNumber(double number, int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping);

  /**
   * A batch version of formatNumber.
   */
  public String[] formatNumber(double[] number, int[] minIntegerDigits, int[] minFractionalDigits, int[] maxFractionalDigits, boolean[] digitGrouping);

  // the following methods check float serialization
  public String[] generateFloats(int n);

  public boolean checkFloats(String[] clientString);
}
