package solutions.trsoftware.commons.shared.util.text;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.text.DurationFormat.Component;

import static solutions.trsoftware.commons.shared.util.TimeUnit.*;

/**
 * @author Alex
 * @since 1/20/2019
 */
public class DurationFormatTest extends TestCase {

  public void testFormat() throws Exception {
    {
      DurationFormat fmt = new DurationFormat(Component.MINUTES, 0, 3);
      System.out.println("================================================================================");
      System.out.println(fmt);
      System.out.println("================================================================================");
      checkResult(fmt, 0, "00:00");
      checkResult(fmt, 1, "00:00.001");
      checkResult(fmt, 10, "00:00.01");
      checkResult(fmt, 500, "00:00.5");
      checkResult(fmt, MINUTES.toMillis(5.5), "05:30");
      checkResult(fmt, HOURS.toMillis(5.5), "05:30:00");
      checkResult(fmt, HOURS.toMillis(5.5) + 2_123, "05:30:02.123");
      checkResult(fmt, YEARS.toMillis(1) + HOURS.toMillis(25.5) + 2_123,
          "1 year, 1 day, 01:30:02.123");
      checkResult(fmt, YEARS.toMillis(2) + HOURS.toMillis(25.5) + 2_123,
          "2 years, 1 day, 01:30:02.123");
      checkResult(fmt, YEARS.toMillis(2) + DAYS.toMillis(3) + HOURS.toMillis(1.5) + 2_123,
          "2 years, 3 days, 01:30:02.123");
      checkResult(fmt, YEARS.toMillis(2) + DAYS.toMillis(3) + HOURS.toMillis(1) + 2_123,
          "2 years, 3 days, 01:00:02.123");
      checkResult(fmt, YEARS.toMillis(2) + DAYS.toMillis(3) + HOURS.toMillis(1) + 2_123,
          "2 years, 3 days, 01:00:02.123");
    }

    {
      DurationFormat fmt = new DurationFormat(Component.SECONDS, 0, 3);
      System.out.println("================================================================================");
      System.out.println(fmt);
      System.out.println("================================================================================");
      checkResult(fmt, 0, "00");
      checkResult(fmt, 1, "00.001");
      checkResult(fmt, 500, "00.5");
      checkResult(fmt, MINUTES.toMillis(5.5), "05:30");
      checkResult(fmt, HOURS.toMillis(5.5), "05:30:00");
      checkResult(fmt, HOURS.toMillis(5.5) + 2_123, "05:30:02.123");
      checkResult(fmt, HOURS.toMillis(25.5) + 2_123, "1 day, 01:30:02.123");
    }

    {
      for (DurationFormat fmt : new DurationFormat[]{
          new DurationFormat(0),
          new DurationFormat(Component.MINUTES, 0)}) {
        System.out.println("================================================================================");
        System.out.println(fmt);
        System.out.println("================================================================================");
        checkResult(fmt, 0, "00:00");
        checkResult(fmt, 1, "00:00");
        checkResult(fmt, 10, "00:00");
        checkResult(fmt, 501, "00:01");
        checkResult(fmt, MINUTES.toMillis(5.5), "05:30");
        checkResult(fmt, HOURS.toMillis(5.5), "05:30:00");
        checkResult(fmt, HOURS.toMillis(5.5) + 2_123, "05:30:02");
        // 59.999 seconds should be rounded up to nearest minute
        checkResult(fmt, 59_999, "01:00");
        // and so on...
        checkResult(fmt, MINUTES.toMillis(59) + 59_999, "01:00:00");
        checkResult(fmt, HOURS.toMillis(23) + MINUTES.toMillis(59) + 59_999 + .99, "1 day, 00:00");

      }
    }

    {
      DurationFormat fmt = new DurationFormat(Component.YEARS, 0, 3);
      System.out.println("================================================================================");
      System.out.println(fmt);
      System.out.println("================================================================================");
      checkResult(fmt, 0, "0 years, 0 days, 00:00:00");
      checkResult(fmt, HOURS.toMillis(5.5) + 2_123, "0 years, 0 days, 05:30:02.123");
    }

    {
      // check that seconds are rounded using the "half-up" rounding mode
      checkResult(new DurationFormat(), 1500, "00:01.500");
      checkResult(new DurationFormat(2), 1250, "00:01.25");
      checkResult(new DurationFormat(1), 1250, "00:01.3");
      checkResult(new DurationFormat(0), 1500, "00:02");
    }

  }


  private void checkResult(DurationFormat formatter, double millis, String expected) {
    doCheckResult(formatter, millis, expected);
    doCheckResult(formatter, -millis, expected);  // should return the same string for a negative mills value
  }

  private void doCheckResult(DurationFormat formatter, double millis, String expected) {
    String result = formatter.format(millis);
    System.out.println(String.valueOf(millis) + " -> " + result);
    assertEquals(expected, result);
  }

}