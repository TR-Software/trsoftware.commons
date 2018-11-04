/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.client.util.time;

import solutions.trsoftware.commons.bridge.ServerTime;

/**
 * A self-updating timer that shows the time remaining until some absolute time on the server.  Uses
 * {@link ServerTime} to determine the corresponding delay on the client.
 * Every time it fires, the timer checks for changes in the referenced {@link ServerTime}
 * instance and updates itself accordingly.
 *
 * @author Alex
 */
public class ServerCountdownTimer extends CountdownTimer {

  public ServerCountdownTimer() {
    this(DEFAULT_UPDATE_INTERVAL_MILLIS);
  }

  /**
   * @param refreshInterval The delay, in milliseconds between consecutive firings of the timer.
   */
  public ServerCountdownTimer(final int refreshInterval) {
    super(ServerTime.INSTANCE, refreshInterval);
  }

}
