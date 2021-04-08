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

import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.io.csv.CSVReader;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.shared.data.CountryCodes;
import solutions.trsoftware.commons.shared.util.IpAddress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.zip.GZIPInputStream;

// License for the underlying CSV database: DonationWare (i.e. completely open, see the ZIP file for more info)
// Update Instructions: Download the latest version by running
// wget software77.net/geo-ip/?DL=1 -O IpToCountry.csv.gz
// NOTE: Run IpToCountryMapperTest after updating

/**
 * Maps IPv4 addresses (represented by {@link IpAddress}) to <a href="https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2">
 * ISO 3166-1 alpha-2</a> country codes.
 * <p>
 * Uses the CSV data downloaded from <a href="http://software77.net/geo-ip/">http://software77.net/geo-ip/</a> with
 * <pre>wget software77.net/geo-ip/?DL=1 -O IpToCountry.csv.gz</pre>
 * This license for the underlying CSV database is "DonationWare" (i.e. completely open, see the csv file contained
 * in {@code IpToCountry.csv.gz} for more info, and give them a donation if you're able to!)
 *
 * <p style="font-style: italic;">
 *   The database bundled with this library was last updated on
 *   Tue May 26 00:40:04 2020 UTC
 * </p>
 *
 * @author Alex
 * @since Nov 6, 2009
 */
public class IpToCountryMapper {

  public static final ResourceLocator DATA_RESOURCE =
      new ResourceLocator("IpToCountry.csv.gz", IpToCountryMapper.class);

  private IpRangeDatabase database = new IpRangeDatabase(100000);

  private static IpToCountryMapper instance;

  public static IpToCountryMapper get() {
    if (instance == null)
      instance = new IpToCountryMapper(); // lazy init
    return instance;
  }

  /**
   * Loads the IP-to-Country data from the CSV zip file.
   *
   * Exposed with package visibility for unit testing
   */
  IpToCountryMapper() {
    Duration loadingTime = new Duration();
    BufferedReader br = null;
    try {
      br = readDataFile();
      int rangeCount = 0;
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#") || line.startsWith(" "))
          continue;  // this line is a comment, so skip it
        // parse the line as CSV
        String[] csvLine = new CSVReader(new StringReader(line)).readNext();
        database.addRange(Long.parseLong(csvLine[0]), Long.parseLong(csvLine[1]), csvLine[4].toLowerCase());
        rangeCount++;
      }
      System.out.println(loadingTime.setName(String.format("Loading %d IP address ranges from %s", rangeCount, DATA_RESOURCE)));
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    finally {
      try {
        if (br != null)
          br.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Returns a new BufferedReader that will traverse the data file embedded in
   * the ZIP file
   */
  private static BufferedReader readDataFile() throws IOException {
    System.out.println("Reading IP address ranges from " + DATA_RESOURCE);
    return new BufferedReader(ServerIOUtils.readUTF8(new GZIPInputStream(DATA_RESOURCE.getInputStream())));
  }

  /**
   * @return the 2-char ISO code for the country associated with the given ip address, if a match was found in the database
   * (and the code is present in {@link CountryCodes}), otherwise {@code null}
   * @see <a href="https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2">ISO 3166-1 alpha-2</a>
   */
  public String ipToCountry(IpAddress ip) {
    if (ip == null)
      return null;
    String result = database.lookupCountry(ip.toInt());
    if (CountryCodes.getCountryName(result) == null) {
      // the http://software77.net/geo-ip/ database uses a fake code "zz" to denote a "Reserved" IP address range
      // as well as some newer ISO codes like "SS" = South Sudan, that we don't support (we don't have a flag image for that)
      // so for those values we return null
      return null;
    }
    return result;
  }

  /** @return the number of IP address ranges in the database */
  public int getDatabaseSize() {
    return database.size();
  }
}