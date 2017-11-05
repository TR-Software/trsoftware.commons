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

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

import java.util.Collections;

import static solutions.trsoftware.commons.shared.util.SetUtils.*;

public class SetUtilsJavaTest extends TestCase {

  public void testParse() throws Exception {
    assertEquals(newSet("a", "b", "c"), parse("a,b,c"));
    assertEquals(newSet("a", "b", "c"), parse("  a  ,b,   c"));
    assertEquals(newSet("a", "b", "c"), parse("a,  b  ,c,,"));
    assertEquals(newSet("a"), parse("a"));
    assertEquals(newSet("a"), parse("  a  "));
    assertEquals(newSet("a"), parse("  a , , ,,, "));
    assertEquals(Collections.<String>emptySet(), parse(""));
    assertEquals(Collections.<String>emptySet(), parse("  "));
    assertEquals(Collections.<String>emptySet(), parse("  , , ,,, "));
  }

  public void testPrint() throws Exception {
    assertEquals("a,b,c", print(newSet("a", "b", "c")));
    assertEquals("a", print(newSet("a")));
    assertEquals("", print(newSet()));
  }
}