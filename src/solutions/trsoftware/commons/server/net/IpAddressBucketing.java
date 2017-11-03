package solutions.trsoftware.commons.server.net;

/**
 * Utility class that assists in bucketing users into N categories based
 * on IP address.  This is useful for A/B testing.
 *
 * Nov 6, 2012
 *
 * @author Alex
 */
public class IpAddressBucketing {

  /**
   * @return an integer in the range 0..(nBuckets-1), classifying the
   * given address into one of N buckets.
   */
  public static int bucketIpAddress(String ipAddress, int nBuckets) {
    return Math.abs(ipAddress.hashCode()) % nBuckets;
  }
}
