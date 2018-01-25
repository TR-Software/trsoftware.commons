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

package solutions.trsoftware.commons.shared.util.stats;

/**
 * Like {@link MaxComparable}, but instead of finding a max value in a sequence, this class finds the argument that
 * produced that max value in the sequence.
 *
 * Similar to the mathematical <a href="https://en.wikipedia.org/wiki/Arg_max">argmax</a> function.
 *
 * @author Alex
 * @since Mar 2, 2010
 */
public class ArgMax<A, V extends Comparable<V>> extends AbstractArgMinMax<A, V> {
  @Override
  protected int getMultiplier() {
    return 1;
  }
}
