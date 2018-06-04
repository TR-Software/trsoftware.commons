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

package solutions.trsoftware.commons.shared.text.markovchain;

import junit.framework.TestCase;

import java.util.Random;

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public class ShortTransitionTableTest extends TestCase {

  public void testTransitionTable() throws Exception {
    ShortTransitionTable table = new ShortTransitionTable((short)1);
    assertEquals("{1=1}", table.asMap().toString());
    assertTransitionProbability(table, (short)1, 1.0);

    table.add((short)1);
    assertEquals("{1=2}", table.asMap().toString());
    assertTransitionProbability(table, (short)1, 1.0);

    table.add((short)2);
    assertEquals("{1=2, 2=1}", table.asMap().toString());
    assertTransitionProbability(table, (short)1, .66);
    assertTransitionProbability(table, (short)2, .33);

    table.add((short)125);
    assertEquals("{1=2, 2=1, 125=1}", table.asMap().toString());
    assertTransitionProbability(table, (short)1, .5);
    assertTransitionProbability(table, (short)2, .25);
    assertTransitionProbability(table, (short)125, .25);
  }

  /** Checks that transitions are chosen with roughly the given probability */
  private void assertTransitionProbability(ShortTransitionTable transitionTable, Short target, double expectedProbability) {
    double hits = 0;
    final int trials = 100000;
    Random rnd = new Random();
    for (int i = 0; i < trials; i++) {
      if (target.equals(transitionTable.chooseTransition(rnd)))
        hits++;
    }
    assertEquals(expectedProbability, hits/trials, .02);  // allow 2% margin of error
  }
}