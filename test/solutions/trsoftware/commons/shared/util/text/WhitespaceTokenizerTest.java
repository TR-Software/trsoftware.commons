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

package solutions.trsoftware.commons.shared.util.text;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;

/**
 * Oct 19, 2009
 *
 * @author Alex
 */
public class WhitespaceTokenizerTest extends TestCase {

  private WhitespaceTokenizer tokenizer;
  private String[] inputs;
  private String[] tokens;

  protected void setUp() throws Exception {
    super.setUp();
    tokenizer = new WhitespaceTokenizer();
    // create a bunch of input strings that should all tokenize to the same thing
    // try a few different combinations of delimiters (defined by java.util.StringTokenizer as " \t\n\r\f")
    inputs = new String[]{
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
    tokens = new String[]{"A", "long", "time", "ago,", "in", "a", "galaxy", "far,", "far", "away."};
  }

  public void testTokenize() throws Exception {
    for (String input : inputs) {
      System.out.println("Tokenizing \"" + input + "\"");
      assertArraysEqual(tokens, tokenizer.tokenize(input));
    }
  }

  public void testJoin() throws Exception {
    assertEquals(inputs[0], tokenizer.join(tokens));  // there is only 1 way to join the tokens
  }
}