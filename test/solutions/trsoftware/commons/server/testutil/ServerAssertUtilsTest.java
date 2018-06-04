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

package solutions.trsoftware.commons.server.testutil;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.util.reflect.MemberPattern;
import solutions.trsoftware.commons.server.util.reflect.MemberSet;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;

import java.util.regex.Pattern;

import static solutions.trsoftware.commons.server.testutil.ServerAssertUtils.assertEqualsByReflection;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * Date: Nov 28, 2008 Time: 6:25:03 PM
 *
 * @author Alex
 */
public class ServerAssertUtilsTest extends TestCase {


  public static class Foo {
    private int i;
    private String s;
    private int[] arrI;
    private String[] arrS;

    public Foo(int i, String s, int[] arrI, String[] arrS) {
      this.i = i;
      this.s = s;
      this.arrI = arrI;
      this.arrS = arrS;
    }

    public int getI() {
      return i;
    }

    public String getS() {
      return s;
    }

    public int[] getArrI() {
      return arrI;
    }

    public String[] getArrS() {
      return arrS;
    }
  }

  public void test_assertEqualsByReflection() throws Exception {
    Foo foo1 = new Foo(1, "a", new int[]{1, 2, 3}, new String[]{"a", "b", "c"});
    Foo foo1b = new Foo(1, "a", new int[]{1, 2, 3}, new String[]{"a", "b", "c"});
    Foo foo2 = new Foo(2, "a", new int[]{1, 2, 3}, new String[]{"a", "b", "c"});
    Foo foo3 = new Foo(2, "a", new int[]{1, 2, 3}, new String[]{"a", "b"});
    Foo foo4 = new Foo(2, "a", new int[]{1, 2, 3}, new String[]{"b", "c", "d"});
    assertEqualsByReflection(foo1, foo1b);
    assertEqualsByReflectionFails(foo1, foo2);
    assertEqualsByReflectionFails(foo1, foo2, new MemberSet<Foo>(Foo.class).excludeMethodsInheritedFromObject());
    assertEqualsByReflection(foo1, foo2, new MemberSet<Foo>(Foo.class).excludeMethodsInheritedFromObject()
        .exclude(MemberPattern.nameMatches("getI")));
    assertEqualsByReflection(foo1, foo2, new MemberSet<Foo>(Foo.class).excludeMethodsInheritedFromObject()
        .addFilter(MemberPattern.and(MemberPattern.isMethod(), MemberPattern.nameMatches(Pattern.compile("get(S|ArrS)")))));
    // TODO: cont here: MemberQuery, if it has any inclusion filters, should only include those members... unit test MemberQuery
    foo2.s = "x";
    assertEqualsByReflectionFails(foo1, foo2, new MemberSet<Foo>(Foo.class).excludeMethodsInheritedFromObject()
            .addFilter(MemberPattern.and(MemberPattern.isMethod(), MemberPattern.nameMatches(Pattern.compile("get(S|ArrS)")))));
    assertEqualsByReflectionFails(foo1, foo3);
    assertEqualsByReflectionFails(foo1, foo4);
    assertEqualsByReflectionFails(foo2, foo3);
    assertEqualsByReflectionFails(foo3, foo4);
    assertEqualsByReflectionFails(foo2, foo4);
  }

  public static <T> void assertEqualsByReflectionFails(final T expected, final T actual) throws Exception {
    AssertionFailedError ex = assertThrows(AssertionFailedError.class, new Function0_t<Throwable>() {
      @Override
      public void call() throws Throwable {
        assertEqualsByReflection(expected, actual);
      }
    });
    System.out.println("assertEqualsByReflection failed as expected by throwing " + ex);
  }

  public static <T> void assertEqualsByReflectionFails(final T expected, final T actual, final MemberSet<T> memberSpec) throws Exception {
    AssertionFailedError ex = assertThrows(AssertionFailedError.class, new Function0_t<Throwable>() {
      @Override
      public void call() throws Throwable {
        assertEqualsByReflection(expected, actual, memberSpec);
      }
    });
    System.out.print("assertEqualsByReflection failed as expected by throwing " + ex);
  }
}