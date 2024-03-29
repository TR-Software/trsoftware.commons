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
import solutions.trsoftware.commons.server.util.Clock;
import solutions.trsoftware.commons.shared.util.time.Time;

/**
 * Singleton oracle for the time on the server.  This is the server-side version of this class, which uses the server's
 * actual clock ({@link Clock}).  The client-side version of this class is located in the {@code <super-source>} directory,
 * and uses {@link ServerTimeClientImpl} to approximate the time on the server.
 * <p>
 * NOTE: There are actually two different implementations of this class in two different directories:
 * <ol>
 *   <li>The Java version in the normal directory for this package</li>
 *   <li>
 *     The GWT version in the {@code translatable} subdirectory of the module, which will replace all usages
 *     of the Java version when running in web mode (the same way the JRE emulation classes replace their original Java
 *     counterparts in web mode. This is configured using the {@code <super-source>} element of the module XML.
 *   </li>
 * </ol>
 *
 * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideOrganizingProjects.html#DevGuideModuleXml">DevGuideModuleXml</a>
 *
 * @author Alex
 */
public abstract class ServerTime extends Time {

  // Java Version -- Java Version -- Java Version -- Java Version -- Java Version

  /** In the GWT version of this class, this field contains an instance of {@link ServerTimeClientImpl} */
  public static final ServerTime INSTANCE = new ServerTime() {
    @Override
    public double getAccuracy() {
      return 0;
    }

    @Override
    public void update(double serverTimestamp, double requestStartLocalTime, double requestEndLocalTime) {
      throw new UnsupportedOperationException();
    }

    @Override
    public double currentTimeMillis() {
      return Clock.currentTimeMillis();
    }
  };

  public abstract double getAccuracy();

  public abstract void update(double serverTimestamp, double requestStartLocalTime, double requestEndLocalTime);
}
