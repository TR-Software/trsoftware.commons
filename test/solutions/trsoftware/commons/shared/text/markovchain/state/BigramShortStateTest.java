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

package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.shared.testutil.AssertUtils;

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public class BigramShortStateTest extends ShortStateTest {

  public void testBigramShortState() throws Exception {
    final BigramShortState state = new BigramShortState("foo", "bar", dict);
    assertEquals("foo", state.getWord(0, dict));
    assertEquals("bar", state.getWord(1, dict));
    assertEquals(2, state.wordCount());
    AssertUtils.assertThrows(IndexOutOfBoundsException.class,
        new Runnable() {
          public void run() {
            state.getWord(2, dict);
          }
        });

    // check for proper implementation of equals and hashCode
    assertTrue(state.equals(new BigramShortState("foo", "bar", dict)));
    assertTrue(state.hashCode() == new BigramShortState("foo", "bar", dict).hashCode());

    assertFalse(state.equals(new BigramShortState("foo", "b", dict)));
    assertFalse(state.hashCode() == new BigramShortState("foo", "b", dict).hashCode());
  }
}