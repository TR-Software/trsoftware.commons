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

import java.util.Objects;

/**
 * IP address conversions.
 * 
 * @author Alex
 * @since Jun 21, 2013
 */
public class IpAddressUtils {

  /**
   * Packs the 4 unsigned bytes of the given IP address string into a {@code long} integer.
   *
   * For example, {@code "127.255.127.255"} will be converted to {@code 0x7fff7fffL}
   *
   * @param ip an IPv4 address string in dot-decimal notation (e.g. "203.0.113.1")
   * @return the bytes of the given address packed into a {@code long} integer
   * @throws IllegalArgumentException if the given string is not a valid IPv4 address in dot-decimal notation
   * @throws NullPointerException if the argument is null
   * @see #ip4StringToInt(String)
   */
  public static long ip4StringToLong(String ip) {
    Objects.requireNonNull(ip, "Argument is null");
    String[] parts = ip.split("\\.");
    if (parts.length != 4)
      throw new IllegalArgumentException(formatErrorMessage(ip));
    long ipLong = 0;
    for (int i = 0; i < 4; i++) {
      try {
        long part = Long.parseLong(parts[i]);
        if (part < 0 || part > 255)
          throw new IllegalArgumentException(formatErrorMessage(ip));
        ipLong |= (part << ((3-i)*8));
      }
      catch (NumberFormatException e) {
        throw new IllegalArgumentException(formatErrorMessage(ip));
      }
    }
    return ipLong;
  }

  private static String formatErrorMessage(String ip) {
    return new StringBuilder().append('"').append(ip).append('"').append(" is not a valid IPv4 address").toString();
  }

  /**
   * Inverse of {@link #ip4StringToLong(String)}
   */
  public static String ip4LongToString(long ip) {
    StringBuilder str = new StringBuilder();
    for (int i = 3; i >= 0; i--) {
      str.append(ip >> 8 * i & 0xff);
      if (i > 0)
        str.append(".");
    }
    return str.toString();
  }

  /**
   * Packs the 4 unsigned bytes of the given IP address string into a signed {@code int} using
   * {@link MathUtils#packUnsignedInt(long)}.
   *
   * For example, {@code "127.255.127.255"} will be converted to {@code -32769} ({@code 0xFFFF7FFF}).
   *
   * @param ip an IPv4 address string in dot-decimal notation (e.g. "203.0.113.1")
   * @return the bytes of the given address packed into a signed {@code int}
   * @throws IllegalArgumentException if the given string is not a valid IPv4 address in dot-decimal notation
   * @throws NullPointerException if the argument is null
   * @see #ip4StringToLong(String)
   */
  public static int ip4StringToInt(String ip) {
    return ip4LongToInt(ip4StringToLong(ip));
  }

  /**
   * Inverse of {@link #ip4StringToInt(String)}.
   */
  public static String ip4IntToString(int ip) {
    return ip4LongToString(ip4IntToLong(ip));
  }

  public static int ip4LongToInt(long ip) {
    return MathUtils.packUnsignedInt(ip);
  }

  public static long ip4IntToLong(int ip) {
    return MathUtils.unsignedInt(ip);
  }

  public static IpAddress randomIpAddress() {
    return new IpAddress(RandomUtils.rnd.nextInt());
  }
}
