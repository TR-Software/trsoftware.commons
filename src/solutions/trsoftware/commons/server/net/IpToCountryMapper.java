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

import solutions.trsoftware.commons.client.data.CountryCodes;
import solutions.trsoftware.commons.client.util.IpAddress;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.io.csv.CSVReader;
import solutions.trsoftware.commons.server.util.Duration;

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

/**
 * Uses the CSV data downloaded from http://software77.net/geo-ip/
 *
 * Assumes that the classloader will be able to resolve "/IpToCountry.csv.zip"
 * to a ZIP file containing a CSV of IP ranges.
 *
 * Nov 6, 2009
 *
 * @author Alex
 */
public class IpToCountryMapper {

  // TODO: perhaps load this file using one of the methods described in https://stackoverflow.com/questions/4340653/file-path-to-resource-in-our-war-web-inf-folder
  public static final String DATA_FILENAME = "IpToCountry.csv";

  private IpRangeDatabase database = new IpRangeDatabase(100000);

  private static IpToCountryMapper instance = new IpToCountryMapper();

  public static IpToCountryMapper getInstance() {
    return instance;
  }

  /**
   * Loads the IP-to-Country data from the CSV zip file.
   *
   * Exposed ith package visibility for unit testing
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