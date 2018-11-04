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

package solutions.trsoftware.commons.shared.text.markovchain.dict;

import junit.framework.TestCase;

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public class IntHashArrayCodingDictionaryTest extends TestCase {
  IntHashArrayCodingDictionary codingDictionary;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    codingDictionary = new IntHashArrayCodingDictionary();
  }

  public void testCoding() throws Exception {
    // encode a few strings
    assertEquals((Integer)0, codingDictionary.encode("foo"));
    assertEquals((Integer)1, codingDictionary.encode("bar"));
    assertEquals((Integer)0, codingDictionary.encode("foo"));  // no duplicates allowed
    assertEquals(2, codingDictionary.size());
    assertEquals((Integer)2, codingDictionary.encode("baz"));
    assertEquals(3, codingDictionary.size());

    // now decode the same strings
    assertEquals("foo", codingDictionary.decode(0));
    assertEquals("bar", codingDictionary.decode(1));
    assertEquals("baz", codingDictionary.decode(2));
  }

}