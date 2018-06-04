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

package solutions.trsoftware.commons.shared.util;

/**
 * IP address conversions.
 * 
 * @author Alex
 * @since Jun 21, 2013
 */
public class IpAddressUtils {

  public static long ip4StringToLong(String ip) {
    if (ip == null)
      return 0L;
    String[] parts = ip.split("\\.");
    assert parts.length == 4;
    long ipLong = 0;
    for (int i = 0; i < 4; i++) {
      ipLong |= (Long.parseLong(parts[i]) << ((3-i)*8));
    }
    return ipLong;
  }

  public static String ip4LongToString(long ip) {
    StringBuilder str = new StringBuilder();
    for (int i = 3; i >= 0; i--) {
      str.append(Long.toString( (ip >> (8*i)) & 0xff) );
      if (i > 0)
        str.append(".");
    }
    return str.toString();
  }

  public static String ip4LongToString(Long ip) {
    // this overloaded method avoids NPE that would arise when calling ip4LongToString(long) with a Long value that's null
    if (ip == null)
      return null;
    return ip4LongToString(ip.longValue());
  }

  public static int ip4StringToInt(String ip) {
    return ip4LongToInt(ip4StringToLong(ip));
  }

  public static String ip4IntToString(int ip) {
    return ip4LongToString(ip4IntToLong(ip));
  }

  public static String ip4IntToString(Integer ip) {
    // this overloaded method avoids NPE that would arise when calling ip4IntegerToString(integer) with a Integer value that's null
    if (ip == null)
      return null;
    return ip4IntToString(ip.intValue());
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
