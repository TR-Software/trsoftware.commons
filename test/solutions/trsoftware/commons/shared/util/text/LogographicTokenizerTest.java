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
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

/**
 * Oct 19, 2009
 *
 * @author Alex
 */
public class LogographicTokenizerTest extends TestCase {

  private LogographicTokenizer tokenizer;
  private String input;
  private String[] tokens;

  protected void setUp() throws Exception {
    super.setUp();
    tokenizer = new LogographicTokenizer();
    input = "A long time ago";
    tokens = new String[]{"A", " ", "l", "o", "n", "g", " ", "t", "i", "m", "e", " ", "a", "g", "o"};
  }

  public void testTokenize() throws Exception {
    AssertUtils.assertArraysEqual(tokens, tokenizer.tokenize(input));
  }

  public void testJoin() throws Exception {
    assertEquals(input, tokenizer.join(tokens));
  }
}