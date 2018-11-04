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
  String formatNumber(double number, int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping);

  /**
   * A batch version of formatNumber.
   */
  String[] formatNumber(double[] number, int[] minIntegerDigits, int[] minFractionalDigits, int[] maxFractionalDigits, boolean[] digitGrouping);

  // the following methods check float serialization
  String[] generateFloats(int n);

  boolean checkFloats(String[] clientString);
}
