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

package solutions.trsoftware.commons.server.util.reflect;

import com.google.common.collect.Collections2;
import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static solutions.trsoftware.commons.client.util.SetUtils.newSet;
import static solutions.trsoftware.commons.server.util.reflect.MemberPatternTest.*;

public class MemberSetTest extends TestCase {

  private final java.util.function.Predicate<Member> isNonStaticFieldOrGetter = member -> {
    if (Modifier.isStatic(member.getModifiers()))
      return false;
    if (member instanceof Field)
      return true;
    else {
      assertTrue(member instanceof Method);
      Method method = (Method)member;
      // we consider any non-void 0-arg method as a getter
      return method.getReturnType() != void.class && method.getParameterTypes().length == 0;
    }
  };

  private static void assertQueryResults(MemberSet<?> query, Collection<Member> expected) {
    assertEquals(newSet(expected), newSet(query.iterator()));
  }

  public void testDefaultFilters() throws Exception {
    // the default MemberQuery.Builder should match any instance (non-static) member that's either a field or a getter (a non-void 0-arg method)
    for (Class<?> cls : classes) {
      assertQueryResults(new MemberSet(cls), Collections2.filter(ReflectionUtils.listMembersAccessibleFrom(cls), isNonStaticFieldOrGetter::test));
    }
  }


  public void testWithNoFilters() throws Exception {
    // an empty query for class X should match all public members accessible from an instance of X
    for (Class<?> cls : classes) {
      assertQueryResults(new MemberSet(cls, false, false), ReflectionUtils.listMembersAccessibleFrom(cls));
    }
    // TODO: test with no value specified for MemberQuery.type (if we choose to allow such queries)
  }

  public void testWithAdditionalFilters() throws Exception {
    // the default MemberQuery.Builder should match any instance (non-static) member that's either a field or a getter (a non-void 0-arg method)
    for (Class<?> cls : classes) {
      MemberSet query = new MemberSet(cls, false, false);
      query.addFilter(MemberPattern.isField());
      assertQueryResults(query, Collections2.filter(ReflectionUtils.listMembersAccessibleFrom(cls), input -> input instanceof Field));
      query.addFilter(MemberPattern.valueTypeIs(String.class));
      assertQueryResults(query, Collections2.filter(ReflectionUtils.listMembersAccessibleFrom(cls), input -> input instanceof Field && ((Field)input).getType() == String.class));
    }
  }

  public void testFiltersCommutativeAndEquals() throws Exception {
    MemberSet<Foo> fooA = new MemberSet<>(Foo.class, false, false);
    MemberSet<Foo> fooB = new MemberSet<>(Foo.class, false, false);
    assertMemberSetsEqual(fooA, fooB);  // the sets should contain the same elements
    MemberSet<Bar> barA = new MemberSet<>(Bar.class, false, false);
    MemberSet<Bar> barB = new MemberSet<>(Bar.class, false, false);
    assertMemberSetsEqual(barA, barB);
    assertMemberSetsNotEqual(fooA, barB);

    MemberPattern[] filters = new MemberPattern[]{MemberPattern.isField(), MemberPattern.valueTypeIs(String.class)};
    for (MemberPattern filter : filters) {
      assertMemberSetsEqual(fooA, fooB);
      fooA.addFilter(filter);
      assertMemberSetsNotEqual(fooA, fooB);
      fooB.addFilter(filter);
      assertMemberSetsEqual(fooA, fooB);
      // adding the same filter twice shouldn't make any difference
      fooA.addFilter(filter);
      assertMemberSetsEqual(fooA, fooB);
    }
  }

  private static void assertMemberSetsEqual(MemberSet a, MemberSet b) {
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(newSet(a.iterator()), newSet(b.iterator()));
  }

  private static void assertMemberSetsNotEqual(MemberSet a, MemberSet b) {
    AssertUtils.assertNotEqual(a, b);
    AssertUtils.assertNotEqual(newSet(a.iterator()), newSet(b.iterator()));
  }

}