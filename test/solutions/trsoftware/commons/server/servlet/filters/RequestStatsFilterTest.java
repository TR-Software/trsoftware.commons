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

package solutions.trsoftware.commons.server.servlet.filters;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.servlet.testutil.DummyFilterChain;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletRequest;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse;
import solutions.trsoftware.commons.server.stats.HierarchicalCounter;
import solutions.trsoftware.commons.server.stats.HierarchicalCounterTest;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.stats.HashCounter;
import solutions.trsoftware.commons.shared.util.text.Alphabet;

import static solutions.trsoftware.commons.shared.util.RandomUtils.randString;

/**
 * Unit tests for {@link RequestStatsFilter}
 *
 * @author Alex, 10/31/2017
 */
public class RequestStatsFilterTest extends TestCase {

  private RequestStatsFilter filter;
  private DummyFilterChain filterChain;

  public void setUp() throws Exception {
    super.setUp();
    filter = new RequestStatsFilter();
    filterChain = new DummyFilterChain();
  }

  public void tearDown() throws Exception {
    filter = null;
    filterChain = null;
    super.tearDown();
  }

  public void testDoHttpFilter() throws Exception {
    // generate some random URIs
    String[] uris = new String[10];
    for (int i = 0; i < uris.length; i++) {
      uris[i] = "";
      int nPathFragmets = RandomUtils.nextIntInRange(1, 4);
      for (int j = 0; j < nPathFragmets; j++) {
        String pathFragment = randString(RandomUtils.nextIntInRange(3, 5), Alphabet.LETTERS_AND_NUMBERS.getChars());
        uris[i] += "/" + pathFragment;
      }
    }
    HashCounter<String> ourCounts = new HashCounter<>(); // will be used to verify the counters produced by our RequestStatsFilter
    int n = 1000;
    for (int i = 0; i < n; i++) {
      String uri = RandomUtils.randomElement(uris);
      ourCounts.increment(uri);
      filter.doHttpFilter(new DummyHttpServletRequest(uri), new DummyHttpServletResponse(), filterChain);
    }
    assertEquals(n, filterChain.getInvocationCount());  // the filter chain should have been invoked every time
    HierarchicalCounter requestCounts = filter.getRequestCounts();
    // print the counter hierarchy
    HierarchicalCounterTest.printCounters(requestCounts, null);
    assertEquals(n, requestCounts.getCount());  // the root contain should contain the sum of all the per-URI counts
    assertEquals(uris.length, requestCounts.getNumChildren());
    // now verify the counts for each URI
    for (String uri : uris) {
      assertEquals(ourCounts.get(uri), requestCounts.getChild(uri).getCount());
    }
  }

}