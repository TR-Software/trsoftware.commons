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

import java.util.ArrayList;

import static solutions.trsoftware.commons.shared.util.LogicUtils.*;

/**
 * Dec 10, 2008
 *
 * @author Alex
 */
public class LogicUtilsTest extends TestCase {

  public void test_bothNotNullAndEqual() throws Exception {
    assertTrue(bothNotNullAndEqual("foo", "foo"));
    assertFalse(bothNotNullAndEqual("foo", "bar"));
    assertFalse(bothNotNullAndEqual("foo", null));
    assertFalse(bothNotNullAndEqual(null, "foo"));
  }

  public void test_bothNotNullAndNotEqual() throws Exception {
    assertTrue(bothNotNullAndNotEqual("foo", "bar"));
    assertFalse(bothNotNullAndNotEqual("foo", "foo"));
    assertFalse(bothNotNullAndNotEqual("foo", null));
    assertFalse(bothNotNullAndNotEqual(null, "foo"));
  }

  public void test_bothNull() throws Exception {
    assertTrue(bothNull(null, null));
    assertFalse(bothNull("foo", "bar"));
    assertFalse(bothNull("foo", null));
    assertFalse(bothNull(null, "foo"));
  }

  public void test_bothNotNull() throws Exception {
    assertTrue(bothNotNull("foo", "foo"));
    assertTrue(bothNotNull("foo", "bar"));
    assertFalse(bothNotNull("foo", null));
    assertFalse(bothNotNull(null, "foo"));
  }

  public void test_eq() throws Exception {
    assertTrue(eq(null, null));
    assertTrue(eq("foo", "foo"));
    assertFalse(eq("foo", "bar"));
    assertFalse(eq("foo", null));
    assertFalse(eq(null, "foo"));
  }

  public void testFirstNonNull() throws Exception {
    assertNull(firstNonNull(null, null));
    assertEquals("", firstNonNull(null, ""));
    assertEquals("", firstNonNull("", null));
    assertEquals("", firstNonNull("", "foo"));
    assertEquals("foo", firstNonNull("foo", ""));
    ArrayList<String> args = ListUtils.fill(new ArrayList<String>(), 5, null);
    args.add("foo");
    for (int i = 0; i < 100; i++) {
      RandomUtils.shuffle(args);
      assertEquals("foo", firstNonNull(args.toArray()));
    }
  }

}