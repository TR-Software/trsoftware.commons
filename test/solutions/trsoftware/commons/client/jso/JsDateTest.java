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

package solutions.trsoftware.commons.client.jso;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.RandomUtilsTest;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.TimeUnit;
import solutions.trsoftware.commons.shared.util.stats.HashCounter;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alex
 * @since 7/15/2018
 */
public class JsDateTest extends CommonsGwtTestCase {

  /**
   * The ISO string for the date used in our test examples.
   * @see #dt
   */
  public static final String ISO_DATE = "2018-07-16T01:47:13.842Z";

  /**
   * The instance representing {@value #ISO_DATE}
   */
  private JsDate dt;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    dt = JsDate.create(ISO_DATE);
  }

  @Override
  protected void gwtTearDown() throws Exception {
    dt = null;
    super.gwtTearDown();
  }

  public void testAdd() throws Exception {
    // the underlying date being tested is "2018-07-16T01:47:13.842Z"
    String baseDateString = "2018-07-16T01:47:13.842Z";
    checkAddResult(TimeUnit.MILLISECONDS, 10_001, // 10s, 1ms
        "2018-07-16T01:47:23.843Z");
    checkAddResult(TimeUnit.SECONDS, 901,  // 15m, 1s
        "2018-07-16T02:02:14.842Z");
    checkAddResult(TimeUnit.MINUTES, 125,  // 2hm, 5m
        "2018-07-16T03:52:13.842Z");
    checkAddResult(TimeUnit.HOURS, 49,  // 2d, 1h
        "2018-07-18T02:47:13.842Z");
    checkAddResult(TimeUnit.DAYS, 6,  // 6d
        "2018-07-22T01:47:13.842Z");
    checkAddResult(TimeUnit.WEEKS, 1,  // 1 week (same as 7d)
        "2018-07-23T01:47:13.842Z");
    checkAddResult(TimeUnit.DAYS, 365,  // 1y
        "2019-07-16T01:47:13.842Z");
    checkAddResult(TimeUnit.MONTHS, 25,  // 2y, 1M
        "2020-08-16T01:47:13.842Z");
    checkAddResult(TimeUnit.MONTHS, -25,  // -2y, 1M
        "2016-06-16T01:47:13.842Z");
    checkAddResult(TimeUnit.YEARS, 2,  // 2y
        "2020-07-16T01:47:13.842Z");
    checkAddResult(TimeUnit.YEARS, -20,  // -20y
        "1998-07-16T01:47:13.842Z");
  }

  /**
   * Invokes {@link JsDate#add(TimeUnit, int)} with the given args on our instance of {@value ISO_DATE} and checks the
   * result.
   */
  private void checkAddResult(TimeUnit unit, int amount, String expected) {
    JsDate result = dt.add(unit, amount);
    getLogger().info(StringUtils.methodCallToStringWithResult("JsDate[\""+ISO_DATE+"\"].add", result.toISOString(), unit, amount));
    // 1) check the return value
    assertEquals(expected, result.toISOString());
    // 2) make sure the original date object was not mutated
    assertEquals(ISO_DATE, dt.toISOString());
  }

  /**
   * @see JsDate#addEqualsAndHashCodeToPrototype()
   */
  public void testEqualsAndHashCode() throws Exception {
    assertEqualsAndHashCode(dt);

    // 1) check our hashCode implementation for a known value:
    assertEquals(549793531, dt.hashCode());

    // 2) now check a mix of sequential and random values
    HashCodeTester hasher = new HashCodeTester();
    for (int i = 1; i < 50; i++) {
      for (TimeUnit unit : TimeUnit.values()) {
        if (unit.isGreaterThanOrEqualTo(TimeUnit.MILLISECONDS)) {
          JsDate d1 = dt.add(unit, i);
          assertEqualsAndHashCode(d1);
          hasher.addHash(d1);
          // also check the negative of this time (i.e. before 1970)
          JsDate d2 = JsDate.create(-d1.getTime());
          assertEqualsAndHashCode(d2);
          hasher.addHash(d2);
        }
      }
    }
    for (int i = 1; i < 100; i++) {
      int rndTime = RandomUtils.rnd().nextInt();
      JsDate dRand = JsDate.create(rndTime);
      assertEqualsAndHashCode(dRand);
      hasher.addHash(dRand);
    }
    // print the hashes
    getLogger().info("Multimap: " + hasher.codes);
    StringBuilder out = new StringBuilder();
    out.append("JsDate hash codes:\n");
    for (Map.Entry<Integer, Integer> entry : hasher.counts.entriesSortedByValueDescending()) {
      hasher.printEntry(entry.getKey(), out);
      out.append('\n');
    }
    getLogger().info(out.toString());
    // assert that all hashes have roughly equal probability
    RandomUtilsTest.assertEqualProbability(hasher.counts);
  }

  private static void assertEqualsAndHashCode(JsDate d) {
    // a new instance representing the same time should be "equals" and have the same hashCode
    AssertUtils.assertEqualsAndHashCode(d, JsDate.create(d.getTime()));
  }

  private class HashCodeTester {
    Multimap<Integer, JsDate> codes = ArrayListMultimap.create();
    HashCounter<Integer> counts = new HashCounter<>();

    void addHash(JsDate d) {
      assertNotNull(d);
          /*double dTime = d.getTime();
          boolean isFinite = Double.isFinite(dTime);
          assertTrue("Invalid time (" + dTime + ") for date " + d, isFinite);*/
      int hc = d.hashCode();
      codes.put(hc, d);
      counts.increment(hc);
    }

    void printEntry(Integer hc, StringBuilder out) {
      SharedNumberFormat numFormat = new SharedNumberFormat(0);
      out.append(hc).append(" (").append(counts.get(hc)).append("): ")
          .append(
              codes.get(hc).stream()
                  .map(d -> d.toISOString() + " [" + numFormat.format(d.getTime()) + "]")
                  .collect(Collectors.joining(", "))
          );
    }
  }

}