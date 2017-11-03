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

package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Mar 15, 2011
 *
 * @author Alex
 */
public class StringTokenizerTest extends TestCase {

  private void assertNextTokenEquals(StringTokenizer tok, String expected) {
    assertTrue(tok.hasNext());
    // check that multiple calls to hasNext don't change anything
    assertTrue(tok.hasNext());
    assertTrue(tok.hasNext());
    assertTrue(tok.hasNext());
    assertEquals(expected, tok.next());
  }

  private void assertNoMoreTokens(final StringTokenizer tok, boolean callHasNextFirst) {
    if (!callHasNextFirst) {
      AssertUtils.assertThrows(NoSuchElementException.class,
          new Runnable() {
            public void run() {
              tok.next();
            }
          });
    }
    assertFalse(tok.hasNext());
    // check that multiple calls to hasNext don't change anything
    assertFalse(tok.hasNext());
    assertFalse(tok.hasNext());
    AssertUtils.assertThrows(NoSuchElementException.class,
        new Runnable() {
          public void run() {
            tok.next();
          }
        });
  }

  public void testEmptyString() throws Exception {
    assertNoMoreTokens(new StringTokenizer(""), true);
    assertNoMoreTokens(new StringTokenizer(""), false);
  }

  public void testStringLength1NoDelimiters() throws Exception {
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("a");
      assertNextTokenEquals(tok, "a");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
  }

  public void testStringLength2NoDelimiters() throws Exception {
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("ab");
      assertNextTokenEquals(tok, "ab");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
  }

  public void testStringLength1WithDelimiters() throws Exception {
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer(" ");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
  }


  public void testStringLength2AllDelimiters() throws Exception {
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("\n ");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("\r\f");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
  }

  public void testStringLength2With1Delimiter() throws Exception {
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("a ");
      assertNextTokenEquals(tok, "a");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("\na");
      assertNextTokenEquals(tok, "a");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
  }

  public void testStringLength3With1Delimiter() throws Exception {
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("a b");
      assertNextTokenEquals(tok, "a");
      assertNextTokenEquals(tok, "b");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("\nab");
      assertNextTokenEquals(tok, "ab");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("ab\n");
      assertNextTokenEquals(tok, "ab");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
  }

  public void testStringLength3With2Delimiters() throws Exception {
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer(" a\n");
      assertNextTokenEquals(tok, "a");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("\n a");
      assertNextTokenEquals(tok, "a");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
    for (boolean callHasNextFirst : new boolean[]{true, false}) {
      StringTokenizer tok = new StringTokenizer("a\r\n");
      assertNextTokenEquals(tok, "a");
      assertNoMoreTokens(tok, callHasNextFirst);
    }
  }

  /** The above tests check the base cases; this one checks the general case. */
  public void testTokenize() throws Exception {
    String[] inputs = new String[]{
        "A long time ago, in a galaxy far, far away.",
        "\nA long time ago, in a galaxy far, far away.",
        "A long time      ago, in a galaxy far, far away.\n",
        "A long time ago, \rin a galaxy far, far away.\n",
        "A long time ago, \f\fin a galaxy far, far away.",
        "A long  \t \n \r \f  time ago, in a galaxy far, far away.",
        "A long  \t\n\r\f  time ago, in a galaxy far, far away.",
        "A long \t\n\r\ftime ago, in a galaxy \nfar, far away.",
        "A long time \n ago, in a   galaxy far, far away.",
        "A\nlong\ttime\rago,  \n in a galaxy\ffar,\f far\naway.",
    };
    List<String> tokens = Arrays.asList("A", "long", "time", "ago,", "in", "a", "galaxy", "far,", "far", "away.");
    for (String input : inputs) {
      System.out.println("Tokenizing \"" + input + "\"");
      AssertUtils.assertSameSequence(tokens.iterator(), new StringTokenizer(input));
    }
  }

}