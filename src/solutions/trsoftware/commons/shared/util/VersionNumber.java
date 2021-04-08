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

package solutions.trsoftware.commons.shared.util;

import solutions.trsoftware.commons.shared.util.compare.RichComparable;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Represents an application version number (e.g. {@code 67.0.3396.99}).
 * <p>
 * Instances of this class are immutable and mutually comparable based on their {@linkplain #components
 * canonical representation} (e.g. {@code 1.2} is considered equal to {@code 1.2.0}, {@code 1.2.0.0}, etc.)
 *
 * @see #parse(String)
 * @author Alex
 * @since 8/9/2018
 */
public class VersionNumber implements RichComparable<VersionNumber> {

  /**
   * A copy of the components passed to the constructor, stripped of any trailing zeros.
   * This canonical representation is used by {@link #compareTo(VersionNumber)}, {@link #equals(Object)},
   * and {@link #hashCode()}, to make 2 instances mutually comparable.
   */
  private final int[] components;

  /**
   * String representation of the original args passed to the constructor.
   * @see #toString()
   */
  private final String string;

  /**
   * @param components the components of the version number listed in decreasing order of significance.
   * Each element should be &ge; 0 (to ensure the correct behavior of {@link #compareTo(VersionNumber)}),
   * and the array should not be concurrently modified until the constructor has finished.
   */
  public VersionNumber(int... components) {
    /*
    TODO:
      - consider throwing an IllegalArgumentException if any component is < 0 (to ensure the correct behavior of compareTo)
      - consider creating a defensive copy of the arg array in the beginning of the constructor to immunize against concurrent modification
     */
    this.string = StringUtils.join(".", components);  // preserve the original args in the string repr of this instance
    if (components.length == 0)
      this.components = new int[0];
    else {
      // create a canonical copy of the args, with all trailing zeros stripped off
      int lastNonZeroIdx;
      for (lastNonZeroIdx = components.length - 1; lastNonZeroIdx >= 0; lastNonZeroIdx--) {
        if (components[lastNonZeroIdx] != 0)
          break;
      }
      this.components = Arrays.copyOf(components, lastNonZeroIdx + 1);
    }
  }

  @Override
  public int compareTo(@Nonnull VersionNumber other) {
    // we compare the canonical representation (we want the 2 arrays to have the same length)
    int[] a = this.components;
    int[] b = other.components;
    int minLen = Math.min(a.length, b.length);
    int i;
    for (i = 0; i < minLen; i++) {
      if (a[i] < b[i])
        return -1;
      else if (a[i] > b[i])
        return 1;
    }
    if (a.length > i)
      return 1; // a has more non-zero components remaining
    else if (b.length > i)
      return -1; // b has more non-zero components remaining
    return 0;  // both have the same number of components, all of which are equal
  }

  @Override
  public String toString() {
    return string;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    VersionNumber that = (VersionNumber)o;

    return Arrays.equals(components, that.components);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(components);
  }

  /**
   * Parses a string of integers joined by {@code '.'}.
   * <p>
   * <em>NOTE</em>: when performing comparisons on the parsed {@link VersionNumber}s,
   * inputs like {@code "1.0"} and {@code "1.0.0.0"} will be considered equal.
   *
   * @param versionStr a version string, e.g. {@code "67.0.3396.99"}
   * @return an instance with components parsed from the given string, e.g. {@code [67, 0, 3396, 99]}
   */
  public static VersionNumber parse(String versionStr) {
    if (versionStr == null)
      throw new NullPointerException();
    versionStr = versionStr.trim();
    if (versionStr.isEmpty())
      return new VersionNumber();
    // TODO: allow passing a custom regex for splitting the components (to be able to parse strings like "1.7_b123"
    String[] components = versionStr.split("\\.");  // e.g. ["67", "0", "3396", "99"]
    int[] intComponents = new int[components.length];
    for (int i = 0; i < components.length; i++) {
      intComponents[i] = Integer.parseInt(components[i]);
    }
    return new VersionNumber(intComponents);
  }

}
