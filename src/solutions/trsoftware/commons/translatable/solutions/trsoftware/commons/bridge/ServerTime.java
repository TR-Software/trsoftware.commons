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

package solutions.trsoftware.commons.bridge;

import solutions.trsoftware.commons.client.util.time.ServerTimeClientImpl;
import solutions.trsoftware.commons.shared.util.time.Time;

/**
 * Client-side ("emulated") version of {@link solutions.trsoftware.commons.bridge.ServerTime}.
 *
 *  @author Alex
 */
public abstract class ServerTime extends Time {

  // GWT Version -- GWT Version -- GWT Version -- GWT Version -- GWT Version

  public static final ServerTime INSTANCE = new ServerTimeClientImpl();

  public abstract double getAccuracy();

  public abstract void update(double serverTimestamp, double requestStartLocalTime, double requestEndLocalTime);
}
