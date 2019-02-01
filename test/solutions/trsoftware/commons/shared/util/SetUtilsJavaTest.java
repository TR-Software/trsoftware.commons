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

import com.google.common.collect.ImmutableSet;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.Set;

import static solutions.trsoftware.commons.shared.util.SetUtils.newSet;
import static solutions.trsoftware.commons.shared.util.SetUtils.parse;

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

  public void testToString() throws Exception {
    assertEquals("a,b,c", SetUtils.toString(newSet("a", "b", "c")));
    assertEquals("a", SetUtils.toString(newSet("a")));
    assertEquals("", SetUtils.toString(newSet()));
  }

  public void testPowerset() throws Exception {
    // example from Wikipedia: http://en.wikipedia.org/wiki/Powerset
    int x = 5;
    int y = 6;
    int z = 7;
    Set<Set<Integer>> powerset = SetUtils.powerset(ImmutableSet.of(x, y, z));
    assertEquals(8, powerset.size());
    assertTrue(powerset.contains(ImmutableSet.<Integer>of()));
    assertTrue(powerset.contains(ImmutableSet.of(x)));
    assertTrue(powerset.contains(ImmutableSet.of(y)));
    assertTrue(powerset.contains(ImmutableSet.of(z)));
    assertTrue(powerset.contains(ImmutableSet.of(x, y)));
    assertTrue(powerset.contains(ImmutableSet.of(x, z)));
    assertTrue(powerset.contains(ImmutableSet.of(y, z)));
    assertTrue(powerset.contains(ImmutableSet.of(x, y, z)));
  }

}