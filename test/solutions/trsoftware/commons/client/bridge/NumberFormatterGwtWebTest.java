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

package solutions.trsoftware.commons.client.bridge;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.bridge.rpc.NumberFormatTestService;
import solutions.trsoftware.commons.client.bridge.rpc.NumberFormatTestServiceAsync;
import solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter;
import solutions.trsoftware.commons.client.bridge.text.NumberFormatter;
import solutions.trsoftware.commons.client.bridge.text.impl.NumberFormatterGwtImpl;
import solutions.trsoftware.commons.client.util.IncrementalForLoop;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.mutable.MutableInteger;

import java.util.Random;

import static solutions.trsoftware.commons.shared.util.StringUtils.template;

/**
 * This class uses NumberFormatterTestBridge as a delegate (which provides a way
 * to call the same test methods from both Java and GWT test contexts).
 *
 * This test needs to be run in web mode (not hosted mode) so that the emulated
 * version of BridgeTypeFactory will be used.
 *
 * @author Alex
 */
public class NumberFormatterGwtWebTest extends CommonsGwtTestCase {
  NumberFormatterTestBridge delegate;
  NumberFormatTestServiceAsync serversideJavaFormatter;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    delegate = new NumberFormatterTestBridge() {
    };
    delegate.setUp();
    serversideJavaFormatter = GWT.create(NumberFormatTestService.class);
    ((ServiceDefTarget)serversideJavaFormatter).setServiceEntryPoint(GWT.getModuleBaseURL() + "numberFormatterServlet");
  }


  public void testCorrectInstanceUsed() throws Exception {
    delegate.testCorrectInstanceUsed(NumberFormatterGwtImpl.class);
  }

  public void testFormattingDeterministically() {
    delegate.testFormattingDeterministically();
  }

  /**
   * Unlike the deterministic test, which tests both Java and GWT
   * implementation, this test compares the output of the GWT formatter to the
   * Java formatter on a broad range of random input values. The test will pass
   * if the outputs match
   */
  public void testOutputMatchesJavaImpl() {
    delayTestFinish(120000);  // go into async mode

    int iterations = 10000;
    final Random rnd = new Random();

    // 1) pre-compute the test values and save them into these arrays, so that
    // they can be fed to the servlet as a batch, because doing 10K servlet calls
    // will be slow and will also eventually crash the system (for unknown reasons,
    // probably because the GWT embedded Tomcat can't handle this much load)
    final int[] minIntegerDigits = new int[iterations];
    final int[] minFractionalDigits = new int[iterations];
    final int[] maxFractionalDigits = new int[iterations];
    final boolean[] digitGrouping = new boolean[iterations];
    final double[] numbers = new double[iterations];
    final String[] numbersFormattedByGwt = new String[iterations];
    final String[] patternsUsedByGwtNumberFormat = new String[iterations];

    // run the following loop incrementally to avoid getting "unresponsive script" errors in web mode
    Scheduler.get().scheduleIncremental(new IncrementalForLoop(iterations) {
      protected void loopBody(int i) {
        minIntegerDigits[i] = rnd.nextInt(5);
        minFractionalDigits[i] = rnd.nextInt(5);
//        maxFractionalDigits[i] = rnd.nextIntInRange(minFractionalDigits[i], 10);
        maxFractionalDigits[i] = RandomUtils.nextIntInRange(rnd, minFractionalDigits[i], 10);
        digitGrouping[i] = rnd.nextBoolean();

        // we want to test 3 kinds of numbers - 0..1, single digits, and many digits
        double number = rnd.nextDouble();
        int coin = rnd.nextInt(3);
        if (coin == 0)
          number *= Integer.MAX_VALUE;
        else if (coin == 1)
          number += rnd.nextInt(9);
        // otherwise, if coin == 3, leave the number as is
        // vary the sign half the time
        if (rnd.nextBoolean())
          number *= -1;

        numbers[i] = number;

        NumberFormatter formatter = AbstractNumberFormatter.getInstance(minIntegerDigits[i], minFractionalDigits[i], maxFractionalDigits[i], digitGrouping[i], false);
        numbersFormattedByGwt[i] = formatter.format(number);
        patternsUsedByGwtNumberFormat[i] = ((NumberFormatterGwtImpl)formatter).getPattern();
      }

      protected void loopFinished() {
        // 2) now that we have all the test data pre-computed, feed it to the server
        // in a batch to get the java.text.NumberFormat results for this test data,
        // and compare the outputs pairwise
        serversideJavaFormatter.formatNumber(numbers, minIntegerDigits, minFractionalDigits, maxFractionalDigits, digitGrouping,
            new AsyncCallback<String[]>() {
              public void onFailure(Throwable caught) {
                throw new RuntimeException(caught);
              }

              public void onSuccess(final String[] numbersFormattedByJava) {
                final MutableInteger perfectMatchCount = new MutableInteger();
                final MutableInteger errorCount = new MutableInteger();
                final MutableInteger warningCount = new MutableInteger();
                final int nStrings = numbersFormattedByJava.length;
                assertEquals(numbers.length, nStrings);

                // run the following loop incrementally to avoid getting "unresponsive script" errors in web mode
                Scheduler.get().scheduleIncremental(new IncrementalForLoop(nStrings) {
                  protected void loopBody(int i) {
                    String javaResult = numbersFormattedByJava[i];
                    String gwtResult = numbersFormattedByGwt[i];

                    System.out.println(template("$1: checkFormatting($2, $3, $4, $5, $6): expected=$7, actual=$8; pattern: $9",
                        i, numbers[i], minIntegerDigits[i], minFractionalDigits[i], maxFractionalDigits[i], digitGrouping[i],
                        javaResult, gwtResult, patternsUsedByGwtNumberFormat[i]));
                    if (javaResult.equals(gwtResult)) {
                      perfectMatchCount.incrementAndGet();
                    }
                    // known discrepancy: java's NumberFormatter only prints as many digits as you get in the Java engineering notation representation of the number, even if maxFractionalDigits is greater
                    // this is defined as the min number of digits needed to differentiate the number from all other representable double values;
                    // GWT prints up to maxFractionalDigits here, which is ok too
                    // just make sure that the java string is almost proper prefix of gwt string in this case (the last digit of the java string may be rounded up)
                    // this most frequently happens with large numbers
                    else if (gwtResult.length() > 10 && javaResult.length() > 10
                        && maxFractionalDigits[i] >= 4 && gwtResult.startsWith(javaResult.substring(0, javaResult.length() - 1))) {
                      System.out.println("WARNING: some digits at the end don't match (probably ok)");
                      warningCount.incrementAndGet();
                    }
                    else {
                      System.out.println("ERROR: the above strings do not match.");
                      errorCount.incrementAndGet();
                    }
                  }

                  protected void loopFinished() {
                    double errorRate = (double)errorCount.get() / nStrings;
                    assertEquals(nStrings, errorCount.get() + warningCount.get() + perfectMatchCount.get());
                    System.out.println("nStrings = " + nStrings);
                    System.out.println("perfectMatchCount = " + perfectMatchCount);
                    System.out.println("warningCount = " + warningCount);
                    System.out.println("errorCount = " + errorCount);
                    System.out.println("errorRate = " + errorRate * 100 + "%");
                    assertTrue("The error rate was too high (> 0.1%)", errorRate < .001);  // assert that the two implementations match at least 99.9% of the time
                    finishTest();
                  }
                });
              }
            });
      }
    });
  }

}