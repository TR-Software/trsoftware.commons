/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.bridge;

import solutions.trsoftware.commons.client.bridge.json.JSONParser;
import solutions.trsoftware.commons.client.bridge.text.NumberFormatter;
import solutions.trsoftware.commons.client.bridge.util.Duration;
import solutions.trsoftware.commons.client.bridge.util.RandomGen;
import solutions.trsoftware.commons.client.bridge.util.UrlEncoder;
import solutions.trsoftware.commons.server.bridge.json.GsonJSONParser;
import solutions.trsoftware.commons.server.bridge.text.NumberFormatterJavaImpl;
import solutions.trsoftware.commons.server.bridge.util.RandomGenJavaImpl;
import solutions.trsoftware.commons.server.bridge.util.UrlEncoderJavaImpl;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Java Version -- Java Version -- Java Version -- Java Version -- Java Version
 *
 * 
 * IMPORTANT: This is the Java (non-GWT version) of BridgeTypeFactory.java
 * Look in the src/gwt/... content root for the GWT version.
 *
 * This class serves to overcome the problem of having references to
 * non-GWT-compilable classes from those that are GWT compilable, by keeping
 * all occurrences of such in a central place - namely the
 * implementation of this class.
 *
 * There are actually two different implementations of this class
 * in two different source roots - the Java version in /src/java/...
 * and the GWT version ins /src/gwt/..., which will replace all usages
 * of the Java version when running in web mode, the same way the JRE
 * emulation classes replace their original Java counterparts in web mode.
 * This is configured using the super-source module XML element.
 *
 * Note that we could technically do away with this factory class and
 * instead emulate each class directly using the super-source technique,
 * but this interface/factory approach seems easier - so that we just
 * have this emulation weirdness happening with one file instead of many.
 *
 * @see <a href="http://code.google.com/docreader/#p=google-web-toolkit-doc-1-5&s=google-web-toolkit-doc-1-5&t=DevGuideModuleXml">DevGuideModuleXml</a>
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
   * @deprecated simply use {@link Random} now that it is available in GWT's emulation library
   */
  public static RandomGen newRandomGen() {
    return new RandomGenJavaImpl();
  }

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
    return new solutions.trsoftware.commons.server.util.Duration(action);
  }

  public static UrlEncoder getUrlEncoder() {
    return UrlEncoderJavaImpl.getInstance();
  }

  public static Random newSecureRandom() {
    return new SecureRandom();
  }
}
