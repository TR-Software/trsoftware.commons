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

package solutions.trsoftware.commons.shared.util;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * Mar 15, 2011
 *
 * @author Alex
 */
public class StringTokenizerGwtTest extends CommonsGwtTestCase {

  private StringTokenizerTest delegate = new StringTokenizerTest();

  public void testEmptyString() throws Exception {
    delegate.testEmptyString();
  }

  public void testStringLength1NoDelimiters() throws Exception {
    delegate.testStringLength1NoDelimiters();
  }

  public void testStringLength1WithDelimiters() throws Exception {
    delegate.testStringLength1WithDelimiters();
  }

  public void testStringLength2AllDelimiters() throws Exception {
    delegate.testStringLength2AllDelimiters();
  }

  public void testStringLength2NoDelimiters() throws Exception {
    delegate.testStringLength2NoDelimiters();
  }

  public void testStringLength2With1Delimiter() throws Exception {
    delegate.testStringLength2With1Delimiter();
  }

  public void testStringLength3With1Delimiter() throws Exception {
    delegate.testStringLength3With1Delimiter();
  }

  public void testStringLength3With2Delimiters() throws Exception {
    delegate.testStringLength3With2Delimiters();
  }

  public void testTokenize() throws Exception {
    delegate.testTokenize();
  }
}