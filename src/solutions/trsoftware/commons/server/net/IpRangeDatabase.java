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

package solutions.trsoftware.commons.server.net;

import solutions.trsoftware.commons.shared.util.IpAddressUtils;
import solutions.trsoftware.commons.shared.util.MathUtils;

import java.util.Arrays;

/**
 * Efficient data structure for mapping sorted 32-bit IP address ranges to country codes.
 *
 * Multithreading Issues: this class is NOT synchronized.  Mutual exclusion
 * must be ensured for calls to addRange and lookupCountry 
 *
 * 
 * Nov 6, 2009
 *
 * @author Alex
 */
public class IpRangeDatabase {

  // all indices in these 3 arrays correspond to each other, so that
  // indices i collectively represent the i-th lowest range
  // will store the unsigned 32-bit IPs as signed ints to save space
  private int[] mins;
  private int[] maxs;
  private String[] countries;  // NOTE: tried storing countries as a byte array flyweight, but that (surprisingly) used even more memory
  private int n;  // the used size of each array


  public IpRangeDatabase(int initialCapacity) {
    mins = new int[initialCapacity];
    maxs = new int[initialCapacity];
    countries = new String[initialCapacity];
  }

  /**
   * This method should be invoked in ascending order of min and max.
   * The passed-in longs, which represent 32-bit unsigned ints are internally
   * converted to ints using {@link MathUtils#packUnsignedInt(long)}
   */
  public void addRange(long min, long max, String country) {
    assert n == 0 || (min > mins[n-1] && max > maxs[n-1]);  // make sure the ranges will be sorted in ascending order
    ensureCapacity(n+1);
    mins[n] = MathUtils.packUnsignedInt(min);
    maxs[n] = MathUtils.packUnsignedInt(max);
    countries[n] = country.intern(); // intern the short country codes to save memory
    n++;
  }


  /**
   * Increases the capacity of the underlying arrays, if
   * necessary, to ensure that it can hold at least the number of elements
   * specified by the minimum capacity argument.
   *
   * (Code borrowed from ArrayList.ensureCapacity, but modified to work
   * with 3 arrays instead of 1)
   *
   * @param   minCapacity   the desired minimum capacity
   */
  private void ensureCapacity(int minCapacity) {
    int oldCapacity = mins.length;
    if (minCapacity > oldCapacity) {
      int newCapacity = (oldCapacity * 3)/2 + 1;
      if (newCapacity < minCapacity)
        newCapacity = minCapacity;
      // minCapacity is usually close to size, so this is a win:
      mins = Arrays.copyOf(mins, newCapacity);
      maxs = Arrays.copyOf(maxs, newCapacity);
      countries = Arrays.copyOf(countries, newCapacity);
    }
  }

  /**
   * @param ip value obtained using
   * {@link IpAddressUtils#ip4StringToInt(String)}  }
   * @return the country code associated with the given ip address, or null
   * if no country code is associated with the address.
   */
  public String lookupCountry(int ip) {
    if (n == 0 || ip < mins[0])
      return null;  // special cases: our database is empty or the ip is below the lowest min

    // we manually do a binary search of the mins to find the two bounds
    // between which this ip address falls
    int i = n / 2;
    // define the bounds for i
    int lowerbound = 0;
    int upperbound = n-1;
//    int count = 0; // counter for the number of iterations, for QA purposes
    while (!(mins[i] <= ip && (i == n-1 || ip < mins[i+1]))) {
//      count++;
      // repeat until we have the ip trapped between two consecutive indices
      // each iteration moves the bounds closer together, eventually zeroing in on the target
      if (ip < mins[i]) {
        upperbound = i;
        i -= Math.max((i - lowerbound) / 2, 1);  // Math.max ensures we're always making progress
//        System.out.println("upperbound = " + upperbound);;
      }
      else {
        lowerbound = i;
        i += Math.max((upperbound - i) / 2, 1);
//        System.out.println("lowerbound = " + lowerbound);
      }
    }
//    System.out.println("binary search iterations: " + count);
    // at this point we know our ip falls between mins[i] and mins[i+1]
    if (ip <= maxs[i])
      return countries[i];
    else
      return null; // the IP is not contained by any range: it falls after range i but before range i+1 
  }

  /** @return the number of IP address ranges in this database */
  public int size() {
    return n;
  }
}
