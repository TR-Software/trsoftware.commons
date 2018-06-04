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
public class NgramShortStateTest extends ShortStateTest {

  public void testNgramShortState() throws Exception {
    final NgramShortState state = new NgramShortState(dict, "foo", "bar", "baz");
    assertEquals("foo", state.getWord(0, dict));
    assertEquals("bar", state.getWord(1, dict));
    assertEquals("baz", state.getWord(2, dict));
    assertEquals(3, state.wordCount());
    AssertUtils.assertThrows(IndexOutOfBoundsException.class,
        new Runnable() {
          public void run() {
            state.getWord(3, dict);
          }
        });

    // check for proper implementation of equals and hashCode
    assertTrue(state.equals(new NgramShortState(dict, "foo", "bar", "baz")));
    assertTrue(state.hashCode() == new NgramShortState(dict, "foo", "bar", "baz").hashCode());

    assertFalse(state.equals(new NgramShortState(dict, "foo", "b", "baz")));
    assertFalse(state.hashCode() == new NgramShortState(dict, "foo", "b", "baz").hashCode());
  }
}