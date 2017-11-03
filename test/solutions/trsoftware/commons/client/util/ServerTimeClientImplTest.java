package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.client.Duration;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.util.time.ServerTimeClientImpl;

public class ServerTimeClientImplTest extends CommonsGwtTestCase {

  private ServerTimeClientImpl serverTime;
  private double localTime;

  /** Difference between local and server times after 1st update is applied */
  private final int delta1 = 3000;
  /** The RTT value (millis) that will be used for the 1st update) */
  private final int rtt1 = 500;

  /** Difference between local and server times after 2nd update is applied */
  private final int delta2 = -2000;
  /** The RTT value (millis) that will be used for the 2nd update) */
  private final int rtt2 = rtt1 - 200;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    serverTime = new ServerTimeClientImpl();
    localTime = Duration.currentTimeMillis();
  }

  public void testNew() throws Exception {
    assertServerTimeEquals(localTime);  // should return local time until the first update
  }

  /**
   * @return the value of serverTime after the first update is applied
   */
  private double applyFirstUpdate() {
    return applyAndConfirmUpdate(delta1, rtt1);
  }

  /**
   * @return the value of serverTime after the first 2 updates are applied
   */
  private double applyFirst2Updates() {
    applyFirstUpdate();

    // this next update should be ignored because the RTT is 100ms higher than last time
    // NOTE: we add a number <= (higherRTT - bestRTT/2) to the previous delta value here because a delta discrepancy worse than that would force an update despite the higher RTT
    double higherRTT = getHigherRTT();
    applyAndRejectUpdate(delta1 + higherRTT - serverTime.getBestRTT()/2, higherRTT);

    // this next update should be accepted because the RTT is 200ms lower than the best one so far
    return applyAndConfirmUpdate(delta2, rtt2);
  }

  private double getHigherRTT() {
    return serverTime.getBestRTT() + 100;
  }

  public void testUpdate() throws Exception {
    applyFirst2Updates();
  }

  public void testUpdateAfterPositiveTimeJumpChangeOnServer() throws Exception {
    applyFirstUpdate();
    
    // 1) verify that a small positive clock jump on the server will be ignored
    // we add a number <= (higherRTT - bestRTT/2) to the previous delta value here because a delta discrepancy worse than that would force an update despite the higher RTT
    // NOTE: this formula is derived from: higherRTT/2 + (higherRTT - bestRTT)/2
    double higherRTT = getHigherRTT();
    double lastDelta = delta1;
    applyAndRejectUpdate(lastDelta += higherRTT - serverTime.getBestRTT()/2, higherRTT);

    // 2) verify that a large positive clock jump on the server will be accepted
    // the previous attempt used the highest possible delta that would still be rejected, so now we just need to add an extra millis to that and verify that it will be accepted
    applyAndConfirmUpdate(lastDelta += 1, higherRTT);
  }

  public void testUpdateAfterNegativeTimeJumpChangeOnServer() throws Exception {
    applyFirstUpdate();
    
    // 1) verify that a small negative clock jump on the server will be ignored
    // we subtract number <= bestRTT/2 from the previous delta value here because a delta discrepancy worse than that would force an update despite the higher RTT
    // NOTE: this formula is derived from: higherRTT/2 - (higherRTT + bestRTT)/2   which is the opposite of the positive test case
    double higherRTT = getHigherRTT();
    double lastDelta = delta1;
    applyAndRejectUpdate(lastDelta -= serverTime.getBestRTT()/2, higherRTT);

    // 2) verify that a large negative clock jump on the server will be accepted
    // the previous attempt used the highest possible delta that would still be rejected, so now we just need to subtract an extra millis to that and verify that it will be accepted
    applyAndConfirmUpdate(lastDelta -= 1, higherRTT);
  }


  /**
   * Applies the given update and verifies that it was accepted by the {@link ServerTimeClientImpl}
   * instance.
   * @return the value of the {@link ServerTimeClientImpl} instance after this update is applied
   */
  private double applyAndConfirmUpdate(double delta, double rtt) {
    applyUpdate(delta, rtt);
    return verifyServerTime(delta, rtt);
  }

  /**
   * Applies the given update and verifies that it was rejected by the {@link ServerTimeClientImpl} instance.
   * @return the value of the {@link ServerTimeClientImpl} instance after this update is applied
   */
  private double applyAndRejectUpdate(double delta, double rtt) {
    double oldValue = serverTime.currentTimeMillis();
    applyUpdate(delta, rtt);
    return assertServerTimeEquals(oldValue);
  }

  private void applyUpdate(double delta, double rtt) {
    double serverTimestamp = localTime + delta;
    double requestStartLocalTime = localTime;
    double requestEndLocalTime = localTime + rtt;
    String reprBefore = serverTime.toString();
    serverTime.update(serverTimestamp, requestStartLocalTime, requestEndLocalTime);
    System.out.println("Attempted ServerTime update with delta=" + delta + " and rtt=" + rtt + "; BEFORE: " + reprBefore + ", AFTER: " + serverTime.toString());
  }

  private double verifyServerTime(double delta, double rtt) {
    double expectedDelta = delta - rtt / 2;
    double st = assertServerTimeEquals(Duration.currentTimeMillis() + expectedDelta);
    assertEquals(expectedDelta, serverTime.getDelta());
    assertEquals(rtt, serverTime.getBestRTT());
    assertEquals(rtt/2, serverTime.getAccuracy());
    return st;
  }

  public void testConversions() throws Exception {
    double st1 = applyFirstUpdate();
    double ct2 = Duration.currentTimeMillis() - 7000;
    double st2 = st1 - 7000;
    assertEquals(ct2, serverTime.toClientTime(st2), 5);
    assertEquals(st2, serverTime.toServerTime(ct2), 5);
  }


  public void test_getLocalMillisUntil() throws Exception {
    double st1 = applyFirstUpdate();
    assertEquals(0, serverTime.getMillisUntil(st1), 5.0); // allow a few millis of leeway since we're not using a mocked clock
    assertEquals(100, serverTime.getMillisUntil(st1 + 100), 5.0); // allow a few millis of leeway since we're not using a mocked clock
    assertEquals(-100, serverTime.getMillisUntil(st1 - 100), 5.0); // allow a few millis of leeway since we're not using a mocked clock
  }

  private double assertServerTimeEquals(double expected) {
    double serverTime = this.serverTime.currentTimeMillis();
    assertEquals(expected, serverTime, 5.0);  // allow a few millis of leeway since we're not using a mocked clock
    return serverTime;
  }

}