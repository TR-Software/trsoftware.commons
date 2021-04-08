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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.shared.util.NumberRange;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * An immutable version of {@link java.util.Calendar} that represents month ordinals starting with 1 (as opposed to 0, which
 * is the way Calendar represents them).  Also supports iteration by increments.
 *
 * @author Alex, 1/2/14
 */
public class CalDate {

  private Calendar cal;

  public CalDate(Calendar cal) {
    this.cal = copyCal(cal);
  }

  /**
   * Creates a fully zeroed-out instance of Calendar.
   */
  public CalDate() {
    this.cal = Calendar.getInstance();
    cal.clear();  // use a clean copy (clear all attributes except the time zone)
  }

  /**
   * @param year The absolute year (e.g. 2014)
   * @param month The month of the year, starting with 1
   *     (NOTE: this is different from {@link Calendar#set(int, int, int)}, which uses 0 for January)
   * @param day The day of the month (starting with 1)
   */
  public CalDate(int year, int month, int day) {
    this();
    cal.set(year, month - 1, day);
  }

  /**
   * @param date The date to use for {@link #cal}
   */
  public CalDate(Date date) {
    this();
    cal.setTime(date);
  }

  /**
   * This method should be used nearly everywhere a Calendar instance is being modified, because Calendar instances
   * are mutable.  This method is made private to encourage using CalDate instead of Calendar.
   * @return A defensive copy of cal.
   */
  private static Calendar copyCal(Calendar cal) {
    return (Calendar)cal.clone();
  }

  public Calendar getCal() {
    return copyCal(cal);
  }

  public Date getDate() {
    return cal.getTime();
  }

  /**
   * @return the current time as UTC milliseconds from the epoch.
   * @see Calendar#getTimeInMillis()
   * @see Date#getTime()
   */
  public long getTime() {
    return cal.getTimeInMillis();
  }

  public int getYear() {
    return cal.get(Calendar.YEAR);
  }

  /**
   * @return the month of the year, starting with 1 (NOTE: this is different from {@link Calendar#get(int)},
   * which uses 0 for January)
   */
  public int getMonth() {
    return cal.get(Calendar.MONTH) + 1;
  }

  /**
   * @return day of the month.
   */
  public int getDayOfMonth() {
    return cal.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * @return day of the year.
   */
  public int getDayOfYear() {
    return cal.get(Calendar.DAY_OF_YEAR);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    return cal.equals(((CalDate)o).cal);
  }

  @Override
  public int hashCode() {
    return cal.hashCode();
  }

  /**
   * @return {@code getYear() * 100 + getMonth()}
   * <br/>
   * <strong>Example:</strong>
   * <pre>
   * December, 2016 &rarr; 201612
   * </pre>
   */
  public int packYearAndMonth() {
    return getYear() * 100 + getMonth();
  }

  /**
   * Reverses {@link #packYearAndMonth()}
   * <br/>
   * <strong>Example:</strong>
   * <pre>
   * unpackYearAndMonth(201612) &rarr; new {@link CalDate}(2016, 12, 1)
   * </pre>
   */
  public static CalDate unpackYearAndMonth(int packedYearAndMonth) {
    int year = packedYearAndMonth / 100;
    int month = packedYearAndMonth % 100;
    return new CalDate(year, month, 1);
  }

  /**
   * @return <code>{@link #getYear()} * 10000 + {@link #getMonth()} * 100 + {@link #getDayOfMonth()}</code>
   * <br/>
   * <strong>Example:</strong>
   * <pre>
   * December 31, 2016 &rarr; 20161231
   * </pre>
   */
  public int pack() {
    return getYear() * 10000 + getMonth() * 100 + getDayOfMonth();
  }

  /**
   * Reverses {@link #pack()}
   * <br/>
   * <strong>Example:</strong>
   * <pre>
   * {@link #unpack}(201612) &rarr; new {@link CalDate}(2016, 12, 1)
   * </pre>
   */
  public static CalDate unpack(int packed) {
    int year = packed / 10000;
    int monthDay = (packed % 10000);
    int month = monthDay / 100;
    int day = monthDay % 100;
    return new CalDate(year, month, day);
  }

  /**
   * Same as {@link Calendar#before(Object)}
   */
  public boolean before(CalDate other) {
    return cal.before(other.cal);
  }

  /**
   * Same as {@link Calendar#after(Object)}
   */
  public boolean after(CalDate other) {
    return cal.after(other.cal);
  }

  /**
   * Same as {@link Calendar#add(int, int)}, but preserves immutability.
   * <p>
   * NOTE: this method is semi-deprecated, use {@link #add(Field, int)} instead.
   *
   * @param field the unit to add: one of the constants defined in {@link Calendar}
   * @param offset the amount (of the unit) to add
   */
  public CalDate add(int field, int offset) {
    CalDate result = new CalDate(cal);
    result.cal.add(field, offset);
    return result;
  }

  /**
   * Creates a new instance with the given field offset from this one by the given amount.
   *
   * @param field the unit to add: one of the constants defined in {@link Calendar}
   * @param offset the amount (of the unit) to add
   */
  public CalDate add(Field field, int offset) {
    return add(field.getCalendarField(), offset);
  }

  @Override
  public String toString() {
    return DateFormat.getDateInstance().format(cal.getTime());
  }

  /**
   * Iterates over all the possible CalDate instances in the range [this date, limit), where each returned date is
   * incremented by one of the constants defined in {@link Calendar}.
   */
  public RangeIterator iterateUpTo(CalDate limit, int field, int increment) {
    return new RangeIterator(this, limit, field, increment);
  }

  /**
   * Iterates over all the possible CalDate instances in the range [this date, limit), where each returned date is
   * incremented by one of the constants defined in {@link Calendar}.
   */
  public RangeIterator iterateUpTo(CalDate limit, Field field, int increment) {
    return new RangeIterator(this, limit, field.getCalendarField(), increment);
  }

  /**
   * Iterates over all the possible instances in the range [this date, limit), incrementing by a day each time.
   */
  public RangeIterator iterateDaysUpTo(CalDate limit) {
    return iterateUpTo(limit, Calendar.DAY_OF_YEAR, 1);
  }


  /**
   * Iterates over all the possible CalDate instances between the two endpoints, using the given field and increment.
   */
  public static class RangeIterator implements Iterator<CalDate>, Iterable<CalDate> {
    private final int field;
    private final int increment;
    private final CalDate limit;
    private CalDate next;

    RangeIterator(CalDate startCalDate, CalDate endCalDate, int field, int increment) {
      this.field = field;
      this.increment = increment;
      this.next = startCalDate;
      this.limit = endCalDate;
    }

    public RangeIterator(CalDate startCalDate, CalDate endCalDate, Field field, int increment) {
      this(startCalDate, endCalDate, field.getCalendarField(), increment);
    }

    @Override
    public boolean hasNext() {
      return next.before(limit);
    }

    @Override
    public CalDate next() {
      CalDate ret = next;
      next = next.add(field, increment);
      return ret;
    }

    @Override
    public Iterator<CalDate> iterator() {
      return this;
    }
  }

  public enum Field {
    YEAR(Calendar.YEAR),
    MONTH(Calendar.MONTH),
    DAY(Calendar.DAY_OF_MONTH),
    ;

    /**
     * The {@link Calendar} constant corresponding to this field (e.g. {@link Calendar#YEAR}, {@link Calendar#MONTH},
     * or {@link Calendar#DAY_OF_MONTH}
     */
    private final int calField;

    Field(int calField) {
      this.calField = calField;
    }

    /**
     * @return the {@link Calendar} constant corresponding to this field
     *     (e.g. {@link Calendar#YEAR}, {@link Calendar#MONTH}, or {@link Calendar#DAY_OF_MONTH}
     */
    public int getCalendarField() {
      return calField;
    }

    // TODO: unit test these methods

    /**
     * Converts the given value for this {@link CalDate} field to the value for the corresponding field in {@link Calendar}
     * (the only difference is {@code MONTH} (which starts with 0 in {@link Calendar} but 1 in {@link CalDate}).
     */
    public int toCalendarValue(int value) {
      if (this == MONTH)
        return value - 1;
      return value;
    }

    /**
     * The opposite of {@link #toCalendarValue(int)}
     */
    public int fromCalendarValue(int value) {
      if (this == MONTH)
        return value + 1;
      return value;
    }

    /**
     * Calls {@link Calendar#getActualMinimum(int)} and {@link Calendar#getActualMaximum(int)} on the {@link Calendar}
     * instance encapsulated by the given {@link CalDate} to check whether the given int represents a valid value
     * for this {@link CalDate} field.
     *
     * @return {@code true} iff the given value is within the bounds for this fields.
     */
    public boolean isValid(CalDate calDate, int value) {
      int cValue = toCalendarValue(value);
      Calendar cal = calDate.cal;
      return cValue >= cal.getActualMinimum(calField) && cValue <= cal.getActualMaximum(calField);
    }

    /**
     * Calls {@link Calendar#getActualMinimum(int)} and {@link Calendar#getActualMaximum(int)} on the {@link Calendar}
     * instance encapsulated by the given {@link CalDate} to get the range of valid values for this {@link CalDate}
     * field.
     *
     * @return the range of valid values for this field in reference to the {@link Calendar} encapsulated by the given
     *     {@link CalDate}.
     */
    public NumberRange<Integer> getValueRange(CalDate calDate) {
      Calendar cal = calDate.cal;
      return new NumberRange<>(
          fromCalendarValue(cal.getActualMinimum(calField)),
          fromCalendarValue(cal.getActualMaximum(calField))
      );
    }
  }
}
