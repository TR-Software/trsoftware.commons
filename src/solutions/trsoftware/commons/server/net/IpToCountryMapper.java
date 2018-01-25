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

import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.io.csv.CSVReader;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.shared.data.CountryCodes;
import solutions.trsoftware.commons.shared.util.IpAddress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// License for the underlying CSV database: DonationWare (i.e. completely open, see the ZIP file for more info)
// Update Instructions: Download the latest version by running
// wget software77.net/geo-ip/?DL=2 -O /path/IpToCountry.csv.zip
// NOTE: Run IpToCountryMapperTest after updating

// TODO: switch to the gzip format of the database (wget software77.net/geo-ip/?DL=1 -O /path/IpToCountry.csv.gz): this will allow using a GZIPInputStream to read the file, which is probably more efficient than using a ZipFile

/**
 * Maps IPv4 addresses (represented by {@link IpAddress}) to country
 * Uses the CSV data downloaded from <a href="http://software77.net/geo-ip/">http://software77.net/geo-ip/</a> with
 * <pre>wget software77.net/geo-ip/?DL=2 -O /path/IpToCountry.csv.zip</pre>
 *
 * Assumes that the classloader will be able to resolve {@code "/IpToCountry.csv.zip"}
 * to a ZIP file containing a CSV of IP ranges.
 *
 * <p style="font-style: italic;">
 *   The database bundled with this library was last updated on
 *   Sun Nov 19 12:40:01 2017 UTC
 * </P>
 *
 * @author Alex
 * @since Nov 6, 2009
 */
public class IpToCountryMapper {

  // TODO: perhaps load this file using one of the methods described in https://stackoverflow.com/questions/4340653/file-path-to-resource-in-our-war-web-inf-folder
  public static final String DATA_FILENAME = "IpToCountry.csv";

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
        database.addRange(Long.parseLong(csvLine[0]), Long.parseLong(csvLine[1]), csvLine[4].toLowerCase().intern());  // intern the short country codes to save memory
        rangeCount++;
      }
      System.out.println(loadingTime.setName(String.format("Loading %d IP address ranges from %s", rangeCount, DATA_FILENAME)));
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
    final ZipFile zipFile = new ZipFile(ServerIOUtils.resourceNameToFilename("/" + DATA_FILENAME + ".zip"));
    ZipEntry zipEntry = zipFile.getEntry(DATA_FILENAME);
    System.out.printf("Reading %s from %s%n", zipEntry.getName(), zipFile.getName());
    return new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry))) {
      @Override
      public void close() throws IOException {
        super.close();
        zipFile.close();  // give the caller a way to close the ZIP file
      }
    };
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