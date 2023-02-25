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

import java.util.*;

import static solutions.trsoftware.commons.shared.util.CollectionUtils.addAll;

/**
 * @author Alex, 1/9/14
 */
public abstract class SetUtils {

  /**
   * @return A new set that represents the {@link Set#removeAll(Collection) <i>asymmetric set difference</i>}
   * of the two sets.  Neither arg is modified.
   */
  public static <T> Set<T> difference(Set<T> s1, Set<T> s2) {
    s1 = newSet(s1);
    s1.removeAll(s2);
    return s1;
  }

  /**
   * @return A new set that represents the {@link Set#retainAll(Collection) <i>intersection</i>} of the two sets.
   * Neither arg is modified.
   */
  public static <T> Set<T> intersection(Set<T> s1, Set<T> s2) {
    s1 = newSet(s1);
    s1.retainAll(s2);
    return s1;
  }
  
  /**
   * @return A new set that represents the {@link Set#addAll(Collection) <i>union</i>} of the two sets.
   * Neither arg is modified.
   */
  public static <T> Set<T> union(Set<T> s1, Set<T> s2) {
    s1 = newSet(s1);
    s1.addAll(s2);
    return s1;
  }

  /** @return a new set initialized from the given collection */
  public static <T> LinkedHashSet<T> newSet(Collection<T> col) {
    return new LinkedHashSet<>(col);
  }

  /** @return a new set initialized from the given array */
  @SafeVarargs
  public static <T> LinkedHashSet<T> newSet(T... items) {
    return addAll(new LinkedHashSet<>(), items);
  }

  /** @return a new set initialized from the given iterator */
  public static <T> LinkedHashSet<T> newSet(Iterator<T> it) {
    return addAll(new LinkedHashSet<>(), it);
  }

  /** @return a new {@link TreeSet} initialized from the given array */
  @SafeVarargs
  public static <E extends Comparable<E>> SortedSet<E> newSortedSet(E... items) {
    return addAll(new TreeSet<>(), items);
  }

  /** @return a new {@link TreeSet} initialized from the given iterator */
  public static <E extends Comparable<E>> SortedSet<E> newSortedSet(Iterator<E> it) {
    return addAll(new TreeSet<>(), it);
  }

  /** @return a new set of strings parsed from a comma-separated string. The inverse of {@link #toString(Set)} */
  public static LinkedHashSet<String> parse(String csv) {
    return new LinkedHashSet<String>(StringUtils.splitAndTrim(csv, ","));
  }

  /**
   * Prints the elements of the given set as a comma-separated string. The opposite of {@link #parse(String)}.
   * <p>
   * Unlike a typical implementation of {@link Set#toString()}, this method doesn't enclose the result
   * within square brackets ("[]").
   */
  public static String toString(Set<?> set) {
    return StringUtils.join(",", set);
  }

  /**
   * @return the powerset (the set of all subsets) of the given set
   */
  public static <T> Set<Set<T>> powerset(Set<T> set) {
    // there is a simpler recursive alg, but we use iteration here for speed
    ArrayList<T> elements = new ArrayList<>(set);
    LinkedHashSet<Set<T>> powerset = new LinkedHashSet<>();

    int powersetSize = 2 << set.size() - 1; // 2^n
    for (int i = 0; i < powersetSize; i++) {
      // take the binary for of i (e.g. 1001) and create a member set with those elts (e.g. {3, 0})
      LinkedHashSet<T> memberSet = new LinkedHashSet<>();  // the i-th member set
      for (int j = 0; j < elements.size(); j++) {
        if (((i >> j) & 1) != 0)
          memberSet.add(elements.get(j));
      }
      powerset.add(memberSet);
    }

    return powerset;
  }
}
