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

package solutions.trsoftware.commons.shared.testutil;

import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilterTest;
import solutions.trsoftware.commons.shared.util.LazyReference;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.readLines;

/**
 * @author Alex
 * @since 11/4/2017
 */
public class TestData {

  public static final ResourceLocator ALICE_TEXT_RESOURCE = new ResourceLocator("aliceInWonderlandCorpus.txt", TestData.class);
  private static final Random rnd = new Random();

  // TODO: use private static "holder" classes instead of LazyReference (to avoid need for double-checked locking)

  private static LazyReference<String> aliceInWonderlandText = new LazyReference<String>() {
    @Override
    protected String create() {
      try {
        return ALICE_TEXT_RESOURCE.getContentAsString();
      }
      catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  };

  private static LazyReference<List<String>> aliceInWonderlandTextLines = new LazyReference<List<String>>() {
    @Override
    protected List<String> create() {
      return readLines(ALICE_TEXT_RESOURCE.getReaderUTF8(), true);
    }
  };

  public static String getAliceInWonderlandText() throws IOException {
    return aliceInWonderlandText.get();
  }

  public static List<String> getAliceInWonderlandTextLines() {
    return aliceInWonderlandTextLines.get();
  }

  /**
   * @return {@code n} random URI values for testing a servlet or filter
   * @see HttpServletRequest#getRequestURI()
   * @see CachePolicyFilterTest
   */
  public static List<String> randomURIs(int n) {
    ArrayList<String> ret = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      List<String> path = randomPath(RandomUtils.nextIntInRange(1, 10));
      int outcome = rnd.nextInt(4);
      switch (outcome) {
        case 1:
          // insert a .cache. string at the end
          path.add(randFileName(".cache."));
          break;
        case 2:
          // insert a .nocache. string at the end
          path.add(randFileName(".nocache."));
          break;
      }
      outcome = rnd.nextInt(5);
      switch (outcome) {
        case 1:
          // insert a .cache. string somewhere in the middle
          path.add(rnd.nextInt(path.size()), randFileName(".cache."));
          break;
        case 2:
          // insert a .nocache. string somewhere in the middle
          path.add(rnd.nextInt(path.size()), randFileName(".nocache."));
          break;
        case 3:
          // insert both somewhere in the middle
          path.add(rnd.nextInt(path.size()), randFileName(".cache."));
          path.add(rnd.nextInt(path.size()), randFileName(".nocache."));
          break;
      }
      ret.add("/" + String.join("/", path));
    }
    return ret;
  }

  /**
   * @return the given string surrounded on both sides with random alphanumeric strings between 1 and 10 chars long.
   * @see #randName()
   */
  public static String randFileName(String innerString) {
    return randName() + innerString + randName();
  }

  /**
   * @return a random alphanumeric string (suitable for a filename) between 1 and 10 chars long.
   */
  public static String randName() {
    return RandomUtils.randString(StringUtils.ASCII_LETTERS_AND_NUMBERS, 1, 10);
  }

  /**
   * Generates a random URI ("path" segment of a URL) for testing a servlet or filter.
   *
   * @param length the number of path segments to include
   * @return a random path with the desired number of segments (e.g. {@code 3} &rarr; {@code ["asdf/qwer/zxcv"})
   * @see HttpServletRequest#getRequestURI()
   * @see CachePolicyFilterTest
   */
  public static String randomURI(int length) {
    StringBuilder ret = new StringBuilder(length * 8);
    for (int i = 0; i < length; i++) {
      String pathSegment = RandomUtils.randString(StringUtils.ASCII_LETTERS_AND_NUMBERS, 1, 15);
      ret.append('/').append(pathSegment);
    }
    return ret.toString();
  }

  /**
   * Generates a list of random path elements, useful when need to test a random URI or filesystem path.
   *
   * @param length the number of path segments to include
   * @return a random path with the desired number of segments (e.g. {@code 3} &rarr; {@code ["asdf", "qwer", "zxcv"]})
   * (it's up to the caller to construct the actual path string by joining the elements of this list)
   * @see HttpServletRequest#getRequestURI()
   * @see CachePolicyFilterTest
   */
  public static List<String> randomPath(int length) {
    ArrayList<String> ret = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      String pathSegment = RandomUtils.randString(StringUtils.ASCII_LETTERS_AND_NUMBERS, 1, 15);
      ret.add(pathSegment);
    }
    return ret;
  }

  public static String randomURL(String protocol, int nSubdomains, boolean includePort, int nPathSegments, int segmentLength) {
    StringBuilder ret = new StringBuilder(segmentLength * 2 * (nSubdomains + nPathSegments));
    ret.append(protocol).append("://");
    for (int i = 0; i < nSubdomains; i++) {
      ret.append(RandomUtils.randString(segmentLength, StringUtils.ASCII_LETTERS_AND_NUMBERS));
      if (i < nSubdomains-1)
        ret.append('.');
    }
    if (includePort)
      ret.append(':').append(rnd.nextInt(49152));

    for (int i = 0; i < nPathSegments; i++) {
      ret.append('/').append(RandomUtils.randString(segmentLength, StringUtils.ASCII_LETTERS_AND_NUMBERS));
    }
    return ret.toString();
  }

  /**
   * Generates some {@code int}s useful for unit tests.
   *
   * @param n The number of random {@code int}s to include in the result.
   * @return an array consisting of 9 interesting edge cases in the 32-bit integer space,
   * plus {@code n} random {@code int}s.
   */
  public static int[] randomInts(int n) {
    int[] ret = new int[9+n];
    ret[0] = Integer.MIN_VALUE;
    ret[1] = Integer.MIN_VALUE+1;
    ret[2] = Integer.MAX_VALUE-1;
    ret[3] = Integer.MAX_VALUE;
    ret[4] = -2;
    ret[5] = -1;
    ret[6] = 0;
    ret[7] = 1;
    ret[8] = 2;
    for (int i = 9; i < ret.length; i++) {
      ret[i] = rnd.nextInt();
    }
    return ret;
  }
}
