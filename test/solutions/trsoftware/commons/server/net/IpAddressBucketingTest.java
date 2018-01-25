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

package solutions.trsoftware.commons.server.net;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.shared.util.IpAddress;
import solutions.trsoftware.commons.shared.util.stats.Mean;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static solutions.trsoftware.commons.server.net.IpAddressBucketing.bucketIpAddress;

/**
 * Nov 7, 2012
 *
 * @author Alex
 */
public class IpAddressBucketingTest extends TestCase {

  /*
   This list of IP addresses to use as test data was obtained with the following grep command:
   grep -Eo "([0-9]{1,3})(\.[0-9]{1,3}){3}" catalina.out.2012_10_23_rimu2-tomcat | sort | uniq > ipAddrs.txt
   */

  private String ipAddrsFilename = ServerIOUtils.filenameInPackageOf("ipAddrs.txt", getClass());

  /**
   * Tests that the bucketing algorithm divides the test set of IP address into N
   * buckets with equal probability, and deterministically (an IP address always
   * maps to the same bucket).
   */
  public void testBucketIpAddress() throws Exception {
    ArrayList<String> ipAddrs = ServerIOUtils.readLines(new FileReader(ipAddrsFilename), true);
    System.out.printf("Loaded %d IP addresses from file %s%n", ipAddrs.size(), ipAddrsFilename);

    // test splitting the ip addresses into N buckets, for various values of N
    for (int nBuckets = 1; nBuckets < 10; nBuckets++) {
      System.out.printf("%nTesting bucketing into %d buckets:%n", nBuckets);
      List<String>[] buckets = new List[nBuckets];
      for (String ip : ipAddrs) {
        int bucketIndex = bucketIpAddress(ip, nBuckets);
        // check that the algorithm is deterministic (always maps the same IP address to the same bucket):
        assertEquals(bucketIndex, bucketIpAddress(ip, nBuckets));
        assertEquals(bucketIndex, bucketIpAddress(ip, nBuckets));
        
        if (buckets[bucketIndex] == null)
          buckets[bucketIndex] = new ArrayList<String>();
        List<String> bucket = buckets[bucketIndex];
        bucket.add(ip);
      }
      // now print the buckets, and assert that the probability is approximately as expected
      int sizeOfAllBuckets = 0;
      int totalSize = ipAddrs.size();
      Mean<Double> avgPctUSA = new Mean<Double>();
      double[] pctUSA = new double[nBuckets];
      for (int i = 0; i < buckets.length; i++) {
        List<String> bucket = buckets[i];
        int bucketSize = bucket.size();
        sizeOfAllBuckets += bucketSize;
        double pct = (double)bucketSize / totalSize;
        // calculate % of users in the USA to make sure that some buckets aren't seeing more expensive ads than others
        double nUSA = 0;
        for (String ip : bucket) {
          if ("us".equals(IpToCountryMapper.get().ipToCountry(new IpAddress(ip)))) {
            nUSA++;
          }
        }
        pctUSA[i] = nUSA / bucketSize;
        avgPctUSA.update(pctUSA[i]);
        System.out.printf("Bucket %d (size=%d, %.2f%% of total, %.2f%% USA): ", i, bucketSize, pct*100, pctUSA[i]*100);
        // assert that the fraction of IP addresses assigned to this bucket is approximately what we would expect
        assertEquals(((double)totalSize / nBuckets) / totalSize, pct, 0.05);
        // print 10 random elements in this bucket
        Collections.shuffle(bucket);
        for (int j = 0; j < 10; j++) {
          System.out.print(bucket.get(j) + ", ");
        }
        System.out.println(" etc...");
      }
      assertEquals(totalSize, sizeOfAllBuckets);  // assert that nothing got lost
      // check that no bucket deviates from the average % of USA IP address too much
      for (int i = 0; i < buckets.length; i++) {
        List<String> bucket = buckets[i];
        assertEquals(avgPctUSA.getMean(), pctUSA[i], .05);  // allow a 5% margin of error
      }
    }
  }

}