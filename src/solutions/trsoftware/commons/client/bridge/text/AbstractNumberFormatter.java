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

package solutions.trsoftware.commons.client.bridge.text;

import solutions.trsoftware.commons.bridge.BridgeTypeFactory;

/**
 * This is a number formatting abstraction, used to bridge the GWT i18n implementation
 * and the java.text implementation.  This is similar to the JSONParser bridge
 * implementation.
 *
 * Instances of this class are immutable and can be cached.  But you should
 * never initialize a static reference to an instance of this class,
 * because in a Java (non-GWT) context, the BridgeTypeFactory instance
 * might not have been "injected" yet before the static field declaration
 * is processed.
 *
 * @author Alex
 */
public abstract class AbstractNumberFormatter implements NumberFormatter {
  protected final int minIntegerDigits;
  protected final int minFractionalDigits;
  protected final int maxFractionalDigits;
  protected final boolean digitGrouping;


  protected AbstractNumberFormatter(int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping) {
    this.minIntegerDigits = minIntegerDigits;
    this.minFractionalDigits = minFractionalDigits;
    this.maxFractionalDigits = maxFractionalDigits;
    this.digitGrouping = digitGrouping;
    if (minIntegerDigits < 0)
      throw new IllegalArgumentException("minIntegerDigits must be nonnegative");
    if (minFractionalDigits < 0 || maxFractionalDigits < 0 || maxFractionalDigits < minFractionalDigits)
      throw new IllegalArgumentException("The given min/max fractional digits pair is invalid");
  }

  /** Factory method, which returns the appropriate instance for the current context. */
  public static NumberFormatter getInstance(int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping, boolean percent) {
    return BridgeTypeFactory.newNumberFormatter(minIntegerDigits, minFractionalDigits, maxFractionalDigits, digitGrouping, percent);
  }
}
