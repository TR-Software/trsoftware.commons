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

package solutions.trsoftware.commons.client.util.time;

import com.google.gwt.core.client.Duration;
import solutions.trsoftware.commons.bridge.ServerTime;

/**
 * Approximates the current date/time on the server using Christian's algorithm (http://en.wikipedia.org/wiki/Cristian%27s_algorithm).
 *
 * @author Alex, 11/3/2014
 */
public class ServerTimeClientImpl extends ServerTime {

  /** Our best guess so far of how the server time differs from the client time */
  private double delta;
  /** The round-trip time of the shortest request that was used to update the delta value */
  private double bestRTT;

  public ServerTimeClientImpl() {
    this(Duration.currentTimeMillis(), Duration.currentTimeMillis());
  }

  /**
   * @param serverTime timestamp reported by server
   * @param clientTime our estimate of the timestamp on the client at the exact moment when the server timestamp was generated
   */
  public ServerTimeClientImpl(double serverTime, double clientTime) {
    set(computeDelta(serverTime, clientTime), Double.MAX_VALUE);
  }

  private static double computeDelta(double serverTime, double clientTime) {
    return serverTime - clientTime;
  }

  private static double computeAccuracy(double rtt) {
    return rtt / 2;
  }

  private void set(double delta, double bestRTT) {
    this.delta = delta;
    this.bestRTT = bestRTT;
  }

  /** @return our best guess so far of how the server time differs from the client time (i.e. serverTime - clientTime) */
  public double getDelta() {
    return delta;
  }

  /** @return the round-trip time of the shortest request that was used to update the delta value */
  public double getBestRTT() {
    return bestRTT;
  }

  /**
   * @return the worst possible deviation of the current estimate from reality
   */
  @Override
  public double getAccuracy() {
    return computeAccuracy(bestRTT);
  }

  /**
   * Changes the delta estimate when one of the following conditions is met:
   * (1) the given RTT is lower than the best one seen so far
   * (2) when the difference between the existing delta and the prior delta is greater than the accuracy of the new estimate
   */
  @Override
  public void update(double serverTimestamp, double requestStartLocalTime, double requestEndLocalTime) {
    double rtt = requestEndLocalTime - requestStartLocalTime;
    double newAccuracy = computeAccuracy(rtt);
    double newDelta = computeDelta(serverTimestamp, requestStartLocalTime + newAccuracy);
    if (rtt < bestRTT || (Math.abs(delta - newDelta) > newAccuracy))
      set(newDelta, rtt);
  }

  /**
   * @return our best guess of the current time on the server.
   */
  @Override
  public double currentTimeMillis() {
    return toServerTime(Duration.currentTimeMillis());
  }

  /**
   * @return the server-side equivalent of the given local client-side time.
   */
  public double toServerTime(double clientTime) {
    return clientTime + delta;
  }

  /**
   * @return the client-side equivalent of the given server-side time.
   */
  public double toClientTime(double serverTime) {
    return serverTime - delta;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ServerTime{");
    sb.append("delta=").append(delta);
    sb.append(", bestRTT=").append(bestRTT);
    sb.append('}');
    return sb.toString();
  }
}
