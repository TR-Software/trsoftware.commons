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
package solutions.trsoftware.commons.server.text.markovchain.dict;

import junit.framework.TestCase;

/**
 * Oct 21, 2009
 *
 * @author Alex
 */
public class ShortArrayCodingDictionaryUtf8Test extends TestCase {
  private ShortArrayCodingDictionaryUtf8 codingDictionary;
  

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    codingDictionary = new ShortArrayCodingDictionaryUtf8();
  }

  public void testCoding() throws Exception {
    // encode a few strings
    assertEquals((Short)(short)0, codingDictionary.encode("foo"));
    assertEquals((Short)(short)1, codingDictionary.encode("bar"));
    assertEquals((Short)(short)0, codingDictionary.encode("foo"));  // no duplicates allowed
    assertEquals(2, codingDictionary.size());
    assertEquals((Short)(short)2, codingDictionary.encode("baz"));
    assertEquals(3, codingDictionary.size());

    // now decode the same strings
    assertEquals("foo", codingDictionary.decode((short)0));
    assertEquals("bar", codingDictionary.decode((short)1));
    assertEquals("baz", codingDictionary.decode((short)2));
  }
}