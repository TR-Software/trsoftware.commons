package solutions.trsoftware.commons.shared.text;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.text.Language;
import solutions.trsoftware.commons.shared.text.TypingSpeed;

import java.util.Random;

import static solutions.trsoftware.commons.shared.text.TypingSpeed.*;
import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertEqualsAndHashCode;
import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertNotEqualsAndHashCode;

/**
 * @author Alex, 10/30/2017
 */
public class TypingSpeedTest extends TestCase {

  /** Precision to use for comparing {@code double} values */
  private static final double DELTA = .00001;

  public void testCalcWpm() throws Exception {
    assertEquals(20d, calcWpm(100, 60000, Language.ENGLISH));
  }

  public void testCalcCpm() throws Exception {
    assertEquals(100d, calcCpm(100, 60000));
  }

  public void testWpmToCpm() throws Exception {
    assertEquals(100d, wpmToCpm(20d, Language.ENGLISH));
  }

  public void testCpmToWpm() throws Exception {
    assertEquals(20d, cpmToWpm(100d, Language.ENGLISH));
  }

  public void testCpmToTime() throws Exception {
    assertEquals(60000d, cpmToTime(100, 100d));
    assertEquals(120000d, cpmToTime(100, 50d));
    assertEquals(30000d, cpmToTime(50, 100d));
  }

  public void testWpmToTime() throws Exception {
    assertEquals(60000d, wpmToTime(100, 20d, Language.ENGLISH));
    assertEquals(120000d, wpmToTime(100, 10d, Language.ENGLISH));
    assertEquals(30000d, wpmToTime(50, 20d, Language.ENGLISH));
  }

  public void testGetSpeed() throws Exception {
    Random rnd = new Random();
    for (int i = 0; i < 100; i++) {
      double x = rnd.nextDouble();
      for (Language lang : Language.values()) {
        for (Unit unit1 : Unit.values()) {
          TypingSpeed speed = new TypingSpeed(x, unit1, lang);
          assertEquals(x, speed.getSpeed(unit1), DELTA);
          for (Unit unit2 : Unit.values()) {
            assertEquals(unit1.to(unit2, x, lang), speed.getSpeed(unit2), DELTA);
          }
        }
      }
    }
  }

  /**
   * Tests that {@link TypingSpeed} properly implements the {@code abstract} methods defined in {@link Number}
   */
  public void testNumberImplementation() throws Exception {
    {
      TypingSpeed speed = new TypingSpeed(10, Unit.CPM, Language.ENGLISH);
      assertEquals(10d, speed.doubleValue());
      assertEquals(10f, speed.floatValue());
      assertEquals(10, speed.intValue());
      assertEquals(10L, speed.longValue());
    }
    {
      TypingSpeed speed = new TypingSpeed(10.45, Unit.CPM, Language.ENGLISH);
      assertEquals(10.45d, speed.doubleValue());
      assertEquals(10.45f, speed.floatValue());
      assertEquals(10, speed.intValue());  // should be rounded down
      assertEquals(10L, speed.longValue()); // should be rounded down
    }
    {
      TypingSpeed speed = new TypingSpeed(10.5, Unit.CPM, Language.ENGLISH);
      assertEquals(10.5d, speed.doubleValue());
      assertEquals(10.5f, speed.floatValue());
      assertEquals(11, speed.intValue());  // should be rounded up
      assertEquals(11L, speed.longValue()); // should be rounded up
    }
  }

  public void testUnitTo() throws Exception {
    Random rnd = new Random();
    for (int i = 0; i < 100; i++) {
      double x = rnd.nextDouble();
      for (Language lang : Language.values()) {
        assertEquals(x, Unit.WPM.to(Unit.WPM, x, lang), DELTA);
        assertEquals(x, Unit.CPM.to(Unit.CPM, x, lang));
        assertEquals(wpmToCpm(x, lang), Unit.WPM.to(Unit.CPM, x, lang), DELTA);
        assertEquals(cpmToWpm(x, lang), Unit.CPM.to(Unit.WPM, x, lang), DELTA);
      }
    }
  }

  public void testUnitTimeMillis() throws Exception {
    Random rnd = new Random();
    for (int i = 0; i < 100; i++) {
      for (int charsTyped = 0; charsTyped < 100; charsTyped++) {
        int time = rnd.nextInt(100_000);
        double cpm = calcCpm(charsTyped, time);
        for (Language lang : Language.values()) {
          assertEquals(cpmToTime(charsTyped, cpm), Unit.CPM.timeMillis(charsTyped, cpm, lang), DELTA);
          double wpm = cpmToWpm(cpm, lang);
          assertEquals(wpmToTime(charsTyped, wpm, lang), Unit.CPM.timeMillis(charsTyped, cpm, lang), DELTA);
        }
      }
    }
  }

  public void testUnitCalcSpeed() throws Exception {
    Random rnd = new Random();
    for (int i = 0; i < 100; i++) {
      for (int charsTyped = 0; charsTyped < 100; charsTyped++) {
        int time = rnd.nextInt(100_000);
        double cpm = calcCpm(charsTyped, time);
        for (Language lang : Language.values()) {
          double wpm = cpmToWpm(cpm, lang);
          assertEquals(cpm, Unit.CPM.calcSpeed(charsTyped, time, lang), DELTA);
          assertEquals(wpm, Unit.WPM.calcSpeed(charsTyped, time, lang), DELTA);
          assertEquals(cpm, new TypingSpeed(charsTyped, time, lang).doubleValue(), DELTA);
        }
      }
    }
  }

  public void testEqualsAndHashCode() throws Exception {
    Random rnd = new Random();
    for (int i = 0; i < 100; i++) {
      for (Language lang : Language.values()) {
        double value = rnd.nextInt(100_000);
        for (Unit unit : Unit.values()) {
          AssertUtils.assertEqualsAndHashCode(new TypingSpeed(value, unit, lang), new TypingSpeed(value, unit, lang));
          AssertUtils.assertNotEqualsAndHashCode(new TypingSpeed(value, unit, lang), new TypingSpeed(value + DELTA, unit, lang));
        }
        AssertUtils.assertEqualsAndHashCode(new TypingSpeed(value, Unit.CPM, lang), new TypingSpeed(cpmToWpm(value, lang), Unit.WPM, lang));
        if (lang.charsPerWord() > 1)
          AssertUtils.assertNotEqualsAndHashCode(new TypingSpeed(value, Unit.CPM, lang), new TypingSpeed(value, Unit.WPM, lang));
      }
    }
  }

  public void testToString() throws Exception {
    double wpm = 25.123;
    double cpm = 125.615;  // as calculated by Unit.WPM.to(Unit.CPM, wpm, Language.ENGLISH)
    TypingSpeed speedWpm = new TypingSpeed(wpm, Unit.WPM, Language.ENGLISH);
    TypingSpeed speedCpm = new TypingSpeed(cpm, Unit.CPM, Language.ENGLISH);
    assertEquals("25.123 WPM", speedWpm.toString());
    assertEquals("25.123 WPM", speedCpm.toString());
  }
}