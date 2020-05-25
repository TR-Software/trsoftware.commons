/*
 * Copyright 2020 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util;

import solutions.trsoftware.commons.shared.util.compare.RichComparable;

import java.io.Serializable;

import static solutions.trsoftware.commons.shared.util.IpAddressUtils.*;

/**
 * Wrapper class representing an IP address.  For now, only supports IPv4 addresses, which are
 * internally stored as 32-bit integers.
 * <p>
 * NOTE: when using this class in a webapp, the servlet container (e.g. Tomcat) should be started with the JVM arg
 * {@code -Djava.net.preferIPv4Stack=true}
 *
 * @author Alex
 * @since Jun 29, 2013
 *
 * @see IpAddressUtils#ip4StringToInt(String)
 * @see IpAddressUtils#ip4StringToLong(String)
 */
public class IpAddress implements Serializable, RichComparable<IpAddress> {
  private int packedInt;

  /**
   * @see IpAddressUtils#ip4StringToInt(String)
   */
  public IpAddress(int packedInt) {
    this.packedInt = packedInt;
  }

  /**
   * @param ipStr an IPv4 address string in dot-decimal notation (e.g. "203.0.113.1")
   *
   * @throws IllegalArgumentException if the given string is not a valid IPv4 address in dot-decimal notation
   * @throws NullPointerException if the argument is null
   */
  public IpAddress(String ipStr) {
    this(ip4StringToInt(ipStr));
  }

  /**
   * @see IpAddressUtils#ip4StringToLong(String)
   */
  public IpAddress(long ipStr) {
    this(ip4LongToInt(ipStr));
  }

  // default constructor for serialization
  private IpAddress() {
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IpAddress ipAddress = (IpAddress)o;
    return packedInt == ipAddress.packedInt;
  }

  public int hashCode() {
    return packedInt;
  }

  @Override
  public String toString() {
    return ip4IntToString(packedInt);
  }

  /**
   * @return the {@code int} representation of this IP address
   * @see IpAddressUtils#ip4StringToInt(String) 
   */
  public int toInt() {
    return packedInt;
  }

  /**
   * @return the {@code long} representation of this IP address
   * @see IpAddressUtils#ip4StringToLong(String) 
   */
  public long toLong() {
    return ip4IntToLong(packedInt);
  }

  /**
   * Allows bucketing users into {@code N} categories based on IP address.  This is useful for A/B testing.
   * @return an integer in the range {@code [0, N-1]}, classifying the given address into one of {@code N} buckets.
   */
  public int bucket(int nBuckets) {
    return Math.abs(packedInt) % nBuckets;
  }

  @Override
  public int compareTo(IpAddress o) {
    return Integer.compare(packedInt, o.packedInt);
  }
}
