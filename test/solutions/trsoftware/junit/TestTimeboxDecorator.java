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

package solutions.trsoftware.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junitx.extensions.TestSetup;
import solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter;
import solutions.trsoftware.commons.client.bridge.text.NumberFormatter;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.TimeUnit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static solutions.trsoftware.commons.shared.util.StringUtils.quantity;
import static solutions.trsoftware.commons.shared.util.TimeUnit.SECONDS;

/**
 * Checks that a Test terminates within a short time limit,
 * otherwise prints a warning message.
 *
 * @author Alex
 */
public class TestTimeboxDecorator extends TestSetup {

  /**
   * Test classes that might require a costly 1-time set up operation can define a {@code static} field of this name
   * to keep their tests from being failed for taking too long.  Example:
   * <pre>
   *   public double _TestTimeboxDecoratorDiscount = 4.5;
   * </pre>
   *
   */
  public static final String DISCOUNT_FIELD_NAME = "_TestTimeboxDecoratorDiscount";
  /**
   * Default settings for a {@link TestTimeboxDecorator} that never fails a test for taking too long; only prints warnings
   */
  public static final TimeBoxSettings LENIENT_TIMEBOX = new TimeBoxSettings(Integer.MAX_VALUE, 20);
  /**
   * Default settings for a {@link TestTimeboxDecorator} that fails a test for taking too long
   */
  public static final TimeBoxSettings STRICT_TIMEBOX = new TimeBoxSettings(2, 1);

  private static NumberFormatter timeFormatter = AbstractNumberFormatter.getInstance(0, 0, 2, true, false);
//  private static SharedNumberFormat timeFormatter = new SharedNumberFormat(0, 0, 2, true, false);
  private final boolean hasSlowAnnotation;

  public static class TimeBoxSettings {
    /*
      TODO: cont here:
      * these time limits should only apply to tests not annotated with @Slow
      * should allow fractional seconds for finer precision (either change the type to double or use millis)
    */

    private final int maxSeconds;
    private final int warningSeconds;

    /**
     * @param maxSeconds maximum number of seconds the test is allowed
     *     to finish executing. If it takes longer it will fail.
     * @param warningSeconds If it takes longer than this number of seconds,
     *     a warning will be printed but the test will not fail.
     */
    public TimeBoxSettings(int maxSeconds, int warningSeconds) {
      assert maxSeconds >= warningSeconds;
      this.maxSeconds = maxSeconds;
      this.warningSeconds = warningSeconds;
    }

    public int getMaxSeconds() {
      return maxSeconds;
    }

    public int getWarningSeconds() {
      return warningSeconds;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("TimeBoxSettings{");
      sb.append("maxSeconds=").append(maxSeconds);
      sb.append(", warningSeconds=").append(warningSeconds);
      sb.append('}');
      return sb.toString();
    }
  }

  private final Test test;
  private final String name;
  /**
   * The original method corresponding to the test (e.g. {@code FooTest.testFoo()})
   */
  private final Method testMethod;
  private TimeBoxSettings settings;
  private Duration duration;

  /**
   * @param test The test to be wrapped by this decorator.
   * @param testMethod The original method corresponding to the test (e.g. {@code FooTest.testFoo()})
   */
  public TestTimeboxDecorator(Test test, Method testMethod, TimeBoxSettings timeBoxSettings) {
    super(test);
    this.test = test;
    this.testMethod = testMethod;
    this.settings = timeBoxSettings;
    if (test instanceof TestCase)
      name = ((TestCase)test).getName();
    else if (test instanceof TestSuite)
      name = ((TestSuite)test).getName() + " (suite)";
    else
      name = "<unknown test class: " + test.getClass().getName() + ">";
    hasSlowAnnotation = testMethod != null && testMethod.getAnnotation(Slow.class) != null;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // debug tracing
//    System.out.println("entering TestTimeboxDecorator.setUp() for " + name);
    duration = new Duration(name);
  }

  @Override
  protected void tearDown() throws Exception {
    // NOTE: verifying assertions in this method will not work (they won't cause the test to actually fail)
    super.tearDown();
//    System.out.println(TestUtils.printMemoryUsage(2) + " after " +  name);

    double elapsedSeconds = duration.elapsed(SECONDS);

    // if the test contains a field named _TestTimeboxDecoratorDiscount, its value will be subtracted from elapsedSeconds
    double discountSeconds;
    try {
      Field discountField = test.getClass().getField(DISCOUNT_FIELD_NAME);
      discountSeconds = TimeUnit.MILLISECONDS.to(TimeUnit.SECONDS, discountField.getDouble(test));
    }
    catch (NoSuchFieldException e) {
      discountSeconds = 0;
    }

    if (discountSeconds > 0) {
      elapsedSeconds -= discountSeconds;
      System.out.println(getClass().getName() + ".tearDown() applying a time discount of " + discountSeconds +
          " seconds because the class being tested contains a " + DISCOUNT_FIELD_NAME + " field");
    }

    if (elapsedSeconds > settings.warningSeconds) {
      if (elapsedSeconds > settings.maxSeconds && !hasSlowAnnotation) {
        fail(getDetailedName() + " took " + timeFormatter.format(elapsedSeconds) +
            " seconds, which is longer than the allowed " + quantity(settings.maxSeconds, "second") +
            ".  It should probably be annotated with @" + Slow.class.getSimpleName());
      }
      else if (elapsedSeconds > settings.warningSeconds) {
        StringBuilder warnMsg = new StringBuilder("WARNING: ").append(getDetailedName()).append(" took ")
            .append(timeFormatter.format(elapsedSeconds)).append(" seconds; try to keep it under ")
            .append(quantity(settings.warningSeconds, "second"));
        if (testMethod != null && testMethod.getAnnotation(Slow.class) == null)
          warnMsg.append(" (or annotate with with @").append(Slow.class.getSimpleName()).append(")");
        System.err.println(warnMsg.append("."));
      }
    }
    else {

      if (elapsedSeconds < settings.warningSeconds && hasSlowAnnotation) {
        System.err.println("WARNING: " + getDetailedName() + " took " + timeFormatter.format(elapsedSeconds)
            + " seconds, so there is no need for it to be annotated with @" + Slow.class.getSimpleName() + ".");
      }
    }
  }

  private String getDetailedName() {
    String ret = this.name;
    if (testMethod != null)
      ret += " (in " + testMethod.getDeclaringClass().getName() + ")";
    return ret;
  }

}
