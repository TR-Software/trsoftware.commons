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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class CalDateTest extends TestCase {

  private CalDate january1_2016;
  private CalDate december1_2016;
  private CalDate december31_2016;

  public void setUp() throws Exception {
    super.setUp();
    january1_2016 = new CalDate(2016, 1, 1);
    december1_2016 = new CalDate(2016, 12, 1);
    december31_2016 = new CalDate(2016, 12, 31);
  }

  public void testGetCal() throws Exception {
    Calendar cal = january1_2016.getCal();
    assertEquals(0, cal.get(Calendar.MONTH));
    assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(1, cal.get(Calendar.DAY_OF_YEAR));
    assertEquals(2016, cal.get(Calendar.YEAR));
  }

  public void testGetDate() throws Exception {
    Date date = january1_2016.getDate();
    assertEquals(0, date.getMonth());
    assertEquals(5, date.getDay());  // getDay returns the day of the week, in this case it's Friday (5)
    assertEquals(2016 - 1900, date.getYear());  // getYear returns the year offset since 1990
  }

  public void testGetYear() throws Exception {
    assertEquals(2016, january1_2016.getYear());
    assertEquals(2016, december1_2016.getYear());
    assertEquals(2016, december31_2016.getYear());
  }

  public void testGetMonth() throws Exception {
    assertEquals(1, january1_2016.getMonth());
    assertEquals(12, december1_2016.getMonth());
    assertEquals(12, december31_2016.getMonth());
  }

  public void testGetDayOfMonth() throws Exception {
    assertEquals(1, january1_2016.getDayOfMonth());
    assertEquals(1, december1_2016.getDayOfMonth());
    assertEquals(31, december31_2016.getDayOfMonth());
  }

  public void testGetDayOfYear() throws Exception {
    assertEquals(1, january1_2016.getDayOfYear());
    assertEquals(336, december1_2016.getDayOfYear());
    assertEquals(366, december31_2016.getDayOfYear());
  }

  public void testEquals() throws Exception {
    assertTrue(january1_2016.equals(new CalDate(2016, 1, 1)));
    assertTrue(december1_2016.equals(new CalDate(2016, 12, 1)));
    assertTrue(december31_2016.equals(new CalDate(2016, 12, 31)));
    assertFalse(december31_2016.equals(january1_2016));
    assertFalse(december31_2016.equals(new Object()));
  }

  public void testPackYearAndMonth() throws Exception {
    assertEquals(201601, january1_2016.packYearAndMonth());
    assertEquals(201612, december31_2016.packYearAndMonth());
  }

  public void testUnpackYearAndMonth() throws Exception {
    assertEquals(january1_2016, CalDate.unpackYearAndMonth(201601));
    assertEquals(december1_2016, CalDate.unpackYearAndMonth(201612));
  }

  public void testPack() throws Exception {
    assertEquals(20160101, january1_2016.pack());
    assertEquals(20161201, december1_2016.pack());
    assertEquals(20161231, december31_2016.pack());
  }

  public void testUnpack() throws Exception {
    assertEquals(january1_2016, CalDate.unpack(20160101));
    assertEquals(december1_2016, CalDate.unpack(20161201));
    assertEquals(december31_2016, CalDate.unpack(20161231));
  }

  public void testToString() throws Exception {
    assertEquals("Jan 1, 2016", january1_2016.toString());
    assertEquals("Dec 1, 2016", december1_2016.toString());
    assertEquals("Dec 31, 2016", december31_2016.toString());
  }

  public void testBefore() throws Exception {
    assertTrue(january1_2016.before(new CalDate(2016, 1, 2)));
    assertTrue(december1_2016.before(new CalDate(2016, 12, 2)));
    assertTrue(december31_2016.before(new CalDate(2017, 1, 1)));
    assertFalse(january1_2016.before(new CalDate(2015, 12, 31)));
    assertFalse(december1_2016.before(new CalDate(2016, 11, 30)));
    assertFalse(december31_2016.before(new CalDate(2016, 12, 30)));
  }

  public void testAfter() throws Exception {
    assertFalse(january1_2016.after(new CalDate(2016, 1, 2)));
    assertFalse(december1_2016.after(new CalDate(2016, 12, 2)));
    assertFalse(december31_2016.after(new CalDate(2017, 1, 1)));
    assertTrue(january1_2016.after(new CalDate(2015, 12, 31)));
    assertTrue(december1_2016.after(new CalDate(2016, 11, 30)));
    assertTrue(december31_2016.after(new CalDate(2016, 12, 30)));
  }

  public void testAdd() throws Exception {
    assertEquals(new CalDate(2015, 12, 31), january1_2016.add(Calendar.DAY_OF_MONTH, -1));
    assertEquals(new CalDate(2015, 12, 31), january1_2016.add(Calendar.DAY_OF_YEAR, -1));
    assertEquals(new CalDate(2015, 1, 1), january1_2016.add(Calendar.YEAR, -1));
    assertEquals(new CalDate(2016, 1, 2), january1_2016.add(Calendar.DAY_OF_MONTH, 1));
    assertEquals(new CalDate(2016, 1, 2), january1_2016.add(Calendar.DAY_OF_YEAR, 1));
    assertEquals(new CalDate(2017, 1, 1), january1_2016.add(Calendar.YEAR, 1));
  }

  public void testIterateDaysUpTo() throws Exception {
    assertEquals(CollectionUtils.asList((java.util.Iterator<CalDate>)january1_2016.iterateDaysUpTo(new CalDate(2016, 1, 4))),
        Arrays.asList(
            new CalDate(2016, 1, 1),
            new CalDate(2016, 1, 2),
            new CalDate(2016, 1, 3)));
  }

  public void testIterateUpTo() throws Exception {
    assertEquals(CollectionUtils.asList((java.util.Iterator<CalDate>)january1_2016.iterateUpTo(new CalDate(2016, 6, 1), Calendar.MONTH, 2)),
        Arrays.asList(
            new CalDate(2016, 1, 1),
            new CalDate(2016, 3, 1),
            new CalDate(2016, 5, 1)));
  }
}