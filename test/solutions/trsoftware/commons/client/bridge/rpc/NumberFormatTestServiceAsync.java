package solutions.trsoftware.commons.client.bridge.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Allows comparing the output of GWT's NumberFormat to that of
 * java.text.NumberFormat by running the former client-side
 * and the latter server-side.
 *
 * @author Alex
 */
public interface NumberFormatTestServiceAsync {

  /**
   * Invokes the callback with the output of invoking java.text.NumberFormat with the
   * given parameters.
   */
  public void formatNumber(double number, int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping, AsyncCallback<String> callback);

  /**
   * A batch version of formatNumber.
   */
  public void formatNumber(double[] number, int[] minIntegerDigits, int[] minFractionalDigits, int[] maxFractionalDigits, boolean[] digitGrouping, AsyncCallback<String[]> callback);

  public void generateFloats(int n, AsyncCallback<String[]> callback);

  public void checkFloats(String[] clientStrings, AsyncCallback<Boolean> callback);
}