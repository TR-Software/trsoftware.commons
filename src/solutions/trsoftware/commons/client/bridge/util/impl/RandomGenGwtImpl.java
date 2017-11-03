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

package solutions.trsoftware.commons.client.bridge.util.impl;

import com.google.gwt.user.client.Random;
import solutions.trsoftware.commons.client.bridge.util.RandomGen;

/**
 * Date: Nov 26, 2008 Time: 6:37:31 PM
 *
 * @author Alex
 */
public class RandomGenGwtImpl extends RandomGen {
  public boolean nextBoolean() {
    return Random.nextBoolean();
  }

  public double nextDouble() {
    return Random.nextDouble();
  }

  public int nextInt() {
    return Random.nextInt();
  }

  public int nextInt(int upperBound) {
    // make sure the upperBound is positive, to match java.util.Random's behavior
    if (upperBound <= 0)
      throw new IllegalArgumentException("upperBound must be positive");
    return Random.nextInt(upperBound);
  }
}
