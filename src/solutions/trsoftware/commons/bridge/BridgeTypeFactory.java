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

import solutions.trsoftware.commons.client.bridge.json.JSONParser;
import solutions.trsoftware.commons.client.bridge.text.NumberFormatter;
import solutions.trsoftware.commons.client.bridge.util.URIComponentEncoder;
import solutions.trsoftware.commons.server.bridge.json.GsonJSONParser;
import solutions.trsoftware.commons.server.bridge.text.NumberFormatterJavaImpl;
import solutions.trsoftware.commons.server.bridge.util.URIComponentEncoderJavaImpl;
import solutions.trsoftware.commons.shared.util.Duration;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Java Version -- Java Version -- Java Version -- Java Version -- Java Version
 *
 * <p>
 * IMPORTANT: This is the Java (non-GWT) version of this class.
 * Look in the {@code translatable} subdirectory of the module for the "emulated" GWT version.
 * <p>
 * This class serves to overcome the problem of having references to non-GWT-compilable classes from those that
 * are GWT-compilable, by keeping all occurrences of such in a central place - namely the
 * implementation of this class.
 * <p>
 * There are actually two different implementations of this class in two different directories:
 * <ol>
 *   <li>The Java version in the normal directory for this package</li>
 *   <li>
 *     The GWT version in the {@code translatable} subdirectory of the module, which will replace all usages
 *     of the Java version when running in web mode (the same way the JRE emulation classes replace their original Java
 *     counterparts in web mode. This is configured using the {@code <super-source>} element of the module XML.
 *   </li>
 * </ol>
 *
 * Note that we could technically do away with this factory class and
 * instead emulate each class directly using the {@code <super-source>} technique,
 * but this interface/factory approach seems easier - so that we just
 * have this emulation weirdness happening with one file instead of many.
 *
 * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideOrganizingProjects.html#DevGuideModuleXml">DevGuideModuleXml</a>
 *
 * @author Alex
 */
public class BridgeTypeFactory {

  // Java Version -- Java Version -- Java Version -- Java Version -- Java Version

  /** This class should not be instantiated */
  private BridgeTypeFactory() {
  }

  // in the version of this class found in the src/gwt/... content root,
  // these methods would return the GWT implementations of the same classes

  public static JSONParser newJSONParser() {
    return new GsonJSONParser();
  }

  /**
   * @deprecated use {@link SharedNumberFormat} instead.
   */
  public static NumberFormatter newNumberFormatter(int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping, boolean percent) {
    return new NumberFormatterJavaImpl(minIntegerDigits, minFractionalDigits, maxFractionalDigits, digitGrouping, percent);
  }

  public static Duration newDuration() {
    return new solutions.trsoftware.commons.server.util.Duration();
  }

  public static Duration newDuration(String name) {
    return new solutions.trsoftware.commons.server.util.Duration(name);
  }

  public static Duration newDuration(String name, String action) {
    return new solutions.trsoftware.commons.server.util.Duration(name, action);
  }

  public static URIComponentEncoder getURIComponentEncoder() {
    return URIComponentEncoderJavaImpl.getInstance();
  }

  public static Random newSecureRandom() {
    return new SecureRandom();
  }
}
