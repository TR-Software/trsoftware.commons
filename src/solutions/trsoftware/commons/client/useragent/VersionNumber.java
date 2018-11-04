package solutions.trsoftware.commons.client.useragent;

import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Represents an application version number, e.g. {@code 67.0.3396.99}
 *
 *
 * @author Alex
 * @since 8/9/2018
 */
public class VersionNumber implements Comparable<VersionNumber> {

  /**
   * Maximum supported number of components (for performance reasons).
   */
  public static final int MAX_COMPONENTS = 5;

  /**
   * The actual components passed to the constructor. Used by {@link #toString()}
   */
  private int[] components;

  /**
   * The {@link #components} padded with zeros up to {@link #MAX_COMPONENTS}.
   * This canonical representation is used by {@link #compareTo(VersionNumber)}, {@link #equals(Object)},
   * and {@link #hashCode()}, to make 2 instances mutually comparable.
   */
  private int[] paddedComponents;

  /**
   * @param components the components of the version number listed in descending order of significance
   * @throws IllegalArgumentException if the given array is longer than {@link #MAX_COMPONENTS}
   */
  public VersionNumber(int... components) {
    if (components.length == 0)
      this.components = new int[0];
    else {
      // defensive copy
      this.components = new int[components.length];
      System.arraycopy(components, 0, this.components, 0, components.length);
    }
    this.paddedComponents = maybePad(this.components, MAX_COMPONENTS);
  }

  @Override
  public int compareTo(@Nonnull VersionNumber other) {
    // we compare the canonical representation (we want the 2 arrays to have the same length)
    int[] a = this.paddedComponents;
    int[] b = other.paddedComponents;
    int len = a.length;
    assert b.length == len;
    for (int i = 0; i < len; i++) {
      if (a[i] < b[i])
        return -1;
      else if (a[i] > b[i])
        return 1;
    }
    return 0;
  }

  /**
   * If the given array is shorter than the desired length, will copy it into a new array of that length, effectively
   * padding the given array with zeros at the end.
   * @param arr the source array
   * @param desiredLength the desired length of the result
   * @return either the given array or a new array of the desired length containing the same data with zeros at the end
   */
  private int[] maybePad(int[] arr, int desiredLength) {
    if (arr.length < desiredLength) {
      int[] paddedArr = new int[desiredLength];
      System.arraycopy(arr, 0, paddedArr, 0, arr.length);
      return paddedArr;
    }
    else {
      return arr;
    }
  }

  @Override
  public String toString() {
    return StringUtils.join(".", components);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    VersionNumber that = (VersionNumber)o;

    return Arrays.equals(paddedComponents, that.paddedComponents);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(paddedComponents);
  }

  /**
   * Parses a string of integers joined by {@code '.'}.
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
    String[] components = versionStr.split("\\.");  // e.g. ["67", "0", "3396", "99"]
    int[] intComponents = new int[components.length];
    for (int i = 0; i < components.length; i++) {
      intComponents[i] = Integer.parseInt(components[i]);
    }
    return new VersionNumber(intComponents);
  }
}
