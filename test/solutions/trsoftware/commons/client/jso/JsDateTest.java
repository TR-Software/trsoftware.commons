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
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.RandomUtilsTest;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.TimeUnit;
import solutions.trsoftware.commons.shared.util.stats.HashCounter;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Strings.lenientFormat;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertWithRetry;
import static solutions.trsoftware.commons.shared.util.RandomUtils.randInt;

/**
 * @author Alex
 * @since 7/15/2018
 */
public class JsDateTest extends CommonsGwtTestCase {

  /**
   * The ISO string for the date used in our test examples.
   * <p>
   * <b>Note:</b> the time (10:47 AM UTC) was chosen to give the highest probability of local components
   * being the same as their UTC counterparts (e.g. <code>{@link JsDate#getDay()} == {@link JsDate#getUTCDay()}</code>).
   * According to a reddit post:
   * <blockquote cite="https://www.reddit.com/r/NoStupidQuestions/comments/zud9in/what_time_would_it_be_a_time_where_for_every/#:~:text=When%20it's%2010%2D11%3A00,the%20Line%20islands%20in%20Kiribati.">
   *   When it's 10-11:00 in the morning at GMT0, it is the same calendar day everywhere except UTC +14, which is only used by the Line islands in Kiribati
   * </blockquote>
   * @see #dt
   */
  public static final String ISO_DATE = "2018-07-16T10:47:13.842Z";

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
    // the underlying date being tested is "2018-07-16T10:47:13.842Z"
    checkAddResult(TimeUnit.MILLISECONDS, 10_001, // 10s, 1ms
        "2018-07-16T10:47:23.843Z");
    checkAddResult(TimeUnit.SECONDS, 901,  // 15m, 1s
        "2018-07-16T11:02:14.842Z");
    checkAddResult(TimeUnit.MINUTES, 125,  // 2hr, 5m
        "2018-07-16T12:52:13.842Z");
    checkAddResult(TimeUnit.HOURS, 49,  // 2d, 1h
        "2018-07-18T11:47:13.842Z");
    checkAddResult(TimeUnit.DAYS, 6,  // 6d
        "2018-07-22T10:47:13.842Z");
    checkAddResult(TimeUnit.WEEKS, 1,  // 1 week (same as 7d)
        "2018-07-23T10:47:13.842Z");
    checkAddResult(TimeUnit.DAYS, 365,  // 1y
        "2019-07-16T10:47:13.842Z");
    checkAddResult(TimeUnit.MONTHS, 25,  // 2y, 1M
        "2020-08-16T10:47:13.842Z");
    checkAddResult(TimeUnit.MONTHS, -25,  // -2y, 1M
        "2016-06-16T10:47:13.842Z");
    checkAddResult(TimeUnit.YEARS, 2,  // 2y
        "2020-07-16T10:47:13.842Z");
    checkAddResult(TimeUnit.YEARS, -20,  // -20y
        "1998-07-16T10:47:13.842Z");
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
    assertEquals(-1565290830, dt.hashCode());

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

  public void testIsToday() throws Exception {
    // TODO: code dup in testIsYesterday
    // NOTE: using retry blocks b/c these assertions could fail if it runs just before midnight and doesn't finish before the day rolls over (i.e. now var becomes yesterday)
    assertWithRetry(() -> {
      JsDate now = now();
      assertTrue(now.isToday());  // always true for same instance
      assertTrue(now.copy().isToday());  // or a different instance having the same value
    });
    // test a range of instances around now (+/- 24 hrs)
    int maxMinutes = (int)TimeUnit.DAYS.to(TimeUnit.MINUTES, 1);
    dateRange(now(), TimeUnit.MINUTES, -maxMinutes, maxMinutes).forEach(assertWithRetry(date -> {
      // NOTE: comparing against the latest instance of now() to avoid failing if this runs just before midnight
      assertEquals(date.isSameDay(now()), date.isToday());
    }));
  }

  public void testIsYesterday() throws Exception {
    // TODO: code dup in testIsToday, testIsThisWeek
    // NOTE: using retry blocks b/c these assertions could fail if it runs just before midnight and doesn't finish before the day rolls over (i.e. now var becomes yesterday)
    assertWithRetry(() -> {
      assertFalse(now().isYesterday());
    });
    HashCounter<Boolean> resultCount = new HashCounter<>();  // TODO: temp
    // test a range of instances within 3 days from now
    dateRange(now(), TimeUnit.HOURS, -72, 72).forEach(assertWithRetry(date -> {
      int today = now().getDay();
      int otherDay = date.getDay();
      boolean expected = (today - otherDay == 1) || (otherDay == 6 && today == 0);
      assertEquals(expected, date.isYesterday());
      resultCount.increment(expected);  // TODO: temp
    }));
    System.out.println("isYesterday results: " + resultCount);  // TODO: temp

  }

  public void testIsThisWeek() throws Exception {
    // TODO: code dup in testIsYesterday
    // NOTE: using retry blocks b/c these assertions could fail if it runs just before midnight and doesn't finish before the day rolls over (i.e. now var becomes yesterday)
    assertWithRetry(() -> {
      JsDate now = now();
      assertTrue(now.isThisWeek());  // always true for same instance
      assertTrue(now.copy().isThisWeek());  // or a different instance having the same value
    });
    // test a range of instances around now (+/- 7 days)
    dateRange(now(), TimeUnit.DAYS, -7, 7).forEach(assertWithRetry(date -> {
      // NOTE: comparing against the latest instance of now() to avoid failing if this runs just before midnight
      assertEquals(date.isSameWeek(now()), date.isThisWeek());
    }));

  }

  private static JsDate now() {
    return JsDate.create();
  }

  @Slow
  public void testIsSameDay() throws Exception {
    // TODO: code dup in testIsToday
    assertTrue(dt.isSameDay(dt));  // always true for same instance
    assertTrue(dt.isSameDay(dt.copy()));  // or a different instance having the same value
    // test a range of instances around now (+/- 24 hrs)
    int maxMinutes = (int)TimeUnit.DAYS.to(TimeUnit.MINUTES, 1);
    dateRange(dt, TimeUnit.MINUTES, -maxMinutes, maxMinutes)
        .forEach(date -> checkSameDay(dt, date));
  }

  public static Stream<JsDate> dateRange(JsDate date, TimeUnit unit, int start, int end) {
    return IntStream.rangeClosed(start, end).mapToObj(i -> date.add(unit, i));
  }

  private void checkSameDay(JsDate d1, JsDate d2) {
    boolean expected = d1.getFullYear() == d2.getFullYear()
        && d1.getMonth() == d2.getMonth()
        && d1.getDate() == d2.getDate();
    assertEquals(expected, d1.isSameDay(d2));  // instance method
    assertEquals(expected, JsDate.isSameDay(d1, d2));  // static method
    // should be reflexive
    assertEquals(expected, d2.isSameDay(d1));
    assertEquals(expected, JsDate.isSameDay(d2, d1));
  }

  public void testIsSameWeek() throws Exception {
    // testing week of "Sun, 15 Jul 2018" to "Sat, 21 Jul 2018"
    List<JsDate> datesInWeek = new ArrayList<>();
    List<JsDate> datesNotInWeek = new ArrayList<>();
    int year = 2018;
    int month = 6 /* July (month arg is 0-indexed) */;
    for (int day = 1; day <= 31; day++) {
      // NOTE: using UTC time between 10 and 11 AM gives maximal compatibility across time zones (see doc of ISO_DATE field)
      JsDate date = JsDate.create(JsDate.UTC(year, month, day, 10,
          // random minutes/seconds/millis
          randInt(60), randInt(60), randInt(1000)));
      if (day >= 15 && day <= 21)
        datesInWeek.add(date);
      else
        datesNotInWeek.add(date);
    }
    // test all possible date combinations that are in the same week
    for (JsDate d1 : datesInWeek) {
      for (JsDate d2 : datesInWeek) {
        checkSameWeek(true, d1, d2);
      }
    }
    // test all dates that are not in the same week
    for (JsDate d1 : datesNotInWeek) {
      for (JsDate d2 : datesInWeek) {
        checkSameWeek(false, d1, d2);
      }
    }
  }

  private void checkSameWeek(boolean expected, JsDate d1, JsDate d2) {
    assertEquals(lenientFormat("isSameWeek(%s, %s)", d1, d2),
        expected, JsDate.isSameWeek(d1, d2));
    // should be the same result with args reversed
    assertEquals(lenientFormat("isSameWeek(%s, %s)", d2, d1),
        expected, JsDate.isSameWeek(d2, d1));
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

  public static class Builder {
    int year, month, day = 1, hours, minutes, seconds, milliseconds;
    boolean utc;

    public Builder year(int year) {
      this.year = year;
      return this;
    }

    public Builder month(int month) {
      this.month = month;
      return this;
    }

    public Builder day(int day) {
      this.day = day;
      return this;
    }

    public Builder hours(int hours) {
      this.hours = hours;
      return this;
    }

    public Builder minutes(int minutes) {
      this.minutes = minutes;
      return this;
    }

    public Builder seconds(int seconds) {
      this.seconds = seconds;
      return this;
    }

    public Builder milliseconds(int milliseconds) {
      this.milliseconds = milliseconds;
      return this;
    }

    public Builder utc(boolean utc) {
      this.utc = utc;
      return this;
    }

    public JsDate build() {
      return JsDate.create(year, month);
    }
  }

}