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

package solutions.trsoftware.commons.client.bridge;

import com.google.gwt.core.client.Scheduler;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.bridge.util.RandomGen;
import solutions.trsoftware.commons.client.bridge.util.impl.RandomGenGwtImpl;
import solutions.trsoftware.commons.client.util.IncrementalForLoop;
import solutions.trsoftware.commons.shared.util.callables.Function0;

/**
 * This class uses RandomGenTestBridge as a delegate (which provides a way to
 * call the same test methods from both a Java test and a GWT test context).
 *
 * @author Alex
 */
public class RandomGenGwtTest extends CommonsGwtTestCase {
  RandomGenTestCase delegate = new RandomGenTestCase() {
    @Override
    protected void checkForCollisions(final Function0<? extends Number> generator) {
      final RandomGenTestCase.CollisionsTester ct = delegate.new CollisionsTester();
      delayTestFinish(120000);
      Scheduler.get().scheduleIncremental(new IncrementalForLoop(ITERATIONS_TO_RUN) {
        protected void loopBody(int i) {
          ct.addNext(generator.call());
        }
        protected void loopFinished() {
          ct.assertNotTooManyCollisions();
          finishTest();
        }
      });
    }
  };

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    delegate.setUp();
  }

  public void testCorrectInstanceUsed() throws Exception {
    // this test is running in GWT, not java
    assertTrue(RandomGen.getInstance() instanceof RandomGenGwtImpl);
  }

  public void testNextInt() throws Exception {
    delegate.testNextInt();
  }

  public void testNextDouble() throws Exception {
    delegate.testNextDouble();
  }

  public void testNextIntInRange() throws Exception {
    delegate.testNextIntInRange();
  }

  public void testNextIntWithUpperBound() throws Exception {
    delegate.testNextIntWithUpperBound();
  }

}