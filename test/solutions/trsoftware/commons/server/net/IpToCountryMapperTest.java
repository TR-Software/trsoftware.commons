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
import solutions.trsoftware.commons.Slow;
import solutions.trsoftware.commons.client.data.CountryCodes;
import solutions.trsoftware.commons.client.util.Box;
import solutions.trsoftware.commons.client.util.IpAddress;
import solutions.trsoftware.commons.client.util.callables.Function0;
import solutions.trsoftware.commons.server.testutil.TestUtils;

import java.util.HashSet;
import java.util.Set;

import static solutions.trsoftware.commons.client.util.IpAddressUtils.randomIpAddress;

/**
 * Nov 6, 2009
 *
 * @author Alex
 */
public class IpToCountryMapperTest extends TestCase {

  /**
   * This test just makes sure there are no exceptions while reading
   * the database, and that
   */
  public void testLoadingFromCsv() throws Exception {
    IpToCountryMapper db = IpToCountryMapper.getInstance();
    // sanity check: there should be over 100K entries in the database
    assertTrue(db.getDatabaseSize() > 100000);
    // check a few values from the CSV file
    assertEquals("cn", db.ipToCountry(new IpAddress(3719036928L)));
    assertEquals("jp", db.ipToCountry(new IpAddress(3719823360L)));
    assertEquals("es", db.ipToCountry(new IpAddress(89337856L)));
  }


  /**
   * This test checks 100K random IP Addresses, and makes sure that over half of them
   * can be resolved to a country and that at least 50 different countries
   * have been resolved (these are very conservative minimums just for sanity)
   */
  @Slow
  public void testRandomAddresses() throws Exception {
    IpToCountryMapper db = IpToCountryMapper.getInstance();
    int n = 100000;
    int hits = 0;
    Set<String> uniqueCountries = new HashSet<String>();
    for (int i = 0; i < n; i++) {
      IpAddress ipAddress = randomIpAddress();
      String countryCode = db.ipToCountry(ipAddress);
      if (countryCode != null) {
        uniqueCountries.add(countryCode);
        assertNotNull(CountryCodes.getCountryName(countryCode)); // make sure the result can be identified (e.g. that we have a flag image for it)
        hits++;
      }
    }
    double hitRate = (double)hits / n;
    System.out.printf("The database was able to identify %d out of %d (hitRate=%.2f%%)%nrandom IP addresses in %d different countries:%n%s%n",
        hits, n, hitRate*100, uniqueCountries.size(), uniqueCountries);
    assertTrue(hitRate > .5);
    assertTrue(uniqueCountries.size() > 50);
  }

  @Slow
  public void testPerformance() throws Exception {
    final Box<IpToCountryMapper> dbCapsule = new Box<IpToCountryMapper>();
    TestUtils.printMemoryDelta("Loading " + IpToCountryMapper.DATA_FILENAME + " into IpToCountryMapper",
        new Function0() {
          public Object call() {
            IpToCountryMapper db = new IpToCountryMapper();
            dbCapsule.setValue(db);
            return db;
          }
        });
    final IpToCountryMapper db = dbCapsule.getValue();
    // see how long a thousand lookup take on average
    TestUtils.printMemoryAndTimeUsage("1000 lookups with IpToCountryMapper",
        new Function0() {
          public Object call() {
            String last = null;
            for (int i = 0; i < 1000; i++) {
              last = db.ipToCountry(randomIpAddress());
            }
            return last;
          }
        });
  }

}