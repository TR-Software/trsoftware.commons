package solutions.trsoftware.commons.client.util;

import java.io.Serializable;

import static solutions.trsoftware.commons.client.util.IpAddressUtils.*;

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
