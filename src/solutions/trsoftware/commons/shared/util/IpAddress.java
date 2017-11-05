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

package solutions.trsoftware.commons.shared.util;

import java.io.Serializable;

import static solutions.trsoftware.commons.shared.util.IpAddressUtils.*;

/**
 * Wrapper class representing an IP address.  For now, only supports IPv4 addresses.
 * Internally stores them as 32-bit signed integers.
 * 
 * @author Alex
 * @since Jun 29, 2013
 */
public class IpAddress implements Serializable {
  private int packedInt;

  public IpAddress(int packedInt) {
    this.packedInt = packedInt;
  }

  public IpAddress(String ipStr) {
    this(ip4StringToInt(ipStr));
  }

  public IpAddress(long ipStr) {
    this(ip4LongToInt(ipStr));
  }

  // default constructor only to support serialization
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

  public int toInt() {
    return packedInt;
  }

  public long toLong() {
    return ip4IntToLong(packedInt);
  }
}
