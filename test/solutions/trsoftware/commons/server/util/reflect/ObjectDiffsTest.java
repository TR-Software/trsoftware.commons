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

package solutions.trsoftware.commons.server.util.reflect;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.SetUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static solutions.trsoftware.commons.server.util.reflect.ObjectDiffs.*;
import static solutions.trsoftware.commons.shared.util.LogicUtils.eq;

public class ObjectDiffsTest extends TestCase {

  private static class Foo {
    private int i;
    private String s;
    private int[] arrI;
    private String[] arrS;
    private Bar bar;
    private Bar[] arrBar;
    public List<Bar> barList;
    public Map<Integer, Bar> barMap;

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

    public Bar getBar() {
      return bar;
    }

    public void setBar(Bar bar) {
      this.bar = bar;
    }

    public Bar[] getArrBar() {
      return arrBar;
    }
  }

  private static class FooSubA extends Foo {
    public float f;
    public FooSubA(int i, String s) {
      super(i, s, null, null);
    }
  }

  private static class FooSubB extends Foo {
    public float f;
    public FooSubB(int i, String s) {
      super(i, s, null, null);
    }
  }

  private static class Bar {
    private int x;
    private String s;
    private Foo foo;  // tests the handling of circular references

    private Bar(int x, String s) {
      this.x = x;
      this.s = s;
      this.foo = foo;
    }

    public int getX() {
      return x;
    }

    public String getS() {
      return s;
    }

    public Foo getFoo() {
      return foo;
    }

    public void setFoo(Foo foo) {
      this.foo = foo;
    }
  }

  private static class BarSub extends Bar {
    private float f;
    public float[] fArr;

    private BarSub(int x, String s, float f) {
      super(x, s);
      this.f = f;
    }
  }

  private ObjectDiffs evaluator;
  private Foo foo1, foo2;
  private Bar bar1, bar2;


  public void setUp() throws Exception {
    super.setUp();
    evaluator = new ObjectDiffs();
    foo1 = new Foo(1, "a", new int[]{1, 2, 3}, new String[]{"a", "b", "c"});
    foo2 = new Foo(1, "a", new int[]{1, 2, 3}, new String[]{"a", "b", "c"});
    bar1 = new Bar(10, "abc");
    bar2 = new Bar(10, "abc");
  }

  public void tearDown() throws Exception {
    evaluator = null;
    foo1 = null;
    foo2 = null;
    bar1 = null;
    bar2 = null;
    super.tearDown();
  }

  private class ExpectedDiff {
    private Diff expected;
    private List<ObjectDiffs.PathElement> path;

    private ExpectedDiff(Diff expected, PathElement... path) {
      this.expected = expected;
      this.path = Arrays.asList(path);
    }

    private boolean matches(Diff actual) {
      return eq(expected.getClass(), actual.getClass()) &&
          eq(expected.getLeft(), actual.getLeft()) &&
          eq(expected.getRight(), actual.getRight()) &&
          eq(path, actual.getPath());
    }
  }

  private void assertNoDiffs(Object o1, Object o2) throws Exception {
    Result result = getDiffResult(o1, o2);
    assertTrue(result.isEmpty());
    assertNull(result.getDiffs());
  }

  private void assertDiffResult(Object o1, Object o2, ExpectedDiff... expectedDiffs) throws Exception {
    Result result = getDiffResult(o1, o2);
    assertTrue(expectedDiffs.length > 0);  // should use assertNoDiffs if no diffs expected
    List<ObjectDiffs.Diff> actualDiffs = result.getDiffs();
    assertEquals(expectedDiffs.length, actualDiffs.size());
    int nMatches = 0;
    for (ExpectedDiff expectedDiff : expectedDiffs) {
      for (Diff actualDiff : actualDiffs) {
        if (expectedDiff.matches(actualDiff))
          nMatches++;
      }
    }
    assertEquals(expectedDiffs.length, nMatches);
  }

  private Result getDiffResult(Object o1, Object o2) throws InvocationTargetException, IllegalAccessException {
    Result result = evaluator.diffValues(o1, o2);
    System.out.println(result);
    return result;
  }

  /**
   * Checks diffing values without specifying a {@link MemberSet} (i.e. without diffing all the component fields).
   */
  public void testSimpleValues() throws Exception {
    Object a, b;
    // 1) test strings and primitive values
    assertNoDiffs(null, null);
    assertNoDiffs(1, 1);
    assertNoDiffs("abc", "abc");
    assertDiffResult(a = "abc", b = "xyz", new ExpectedDiff(new Diff(a, b)));
    assertDiffResult(a = 1, b = 2, new ExpectedDiff(new Diff(a, b)));
    assertDiffResult(a = 1, b = null, new ExpectedDiff(new Diff(a, b)));
    assertDiffResult(a = null, b = 2, new ExpectedDiff(new Diff(a, b)));
    // 2) test objects with no MemberQuery specified to ObjectDiffs for their class
    assertDiffResult(a = foo1, b = foo2, new ExpectedDiff(new Diff(a, b))); // not equal because Foo doesn't override equals
    assertDiffResult(a = foo1, b = null, new ExpectedDiff(new Diff(a, b)));
    assertDiffResult(a = null, b = foo2, new ExpectedDiff(new Diff(a, b)));
  }

  public void testArrays() throws Exception {
    assertNoDiffs(new int[0], new int[0]);
    assertNoDiffs(new int[1], new int[1]);
    assertNoDiffs(new int[]{1}, new int[]{1});
    assertNoDiffs(new int[]{1, 2}, new int[]{1, 2});
    assertNoDiffs(new String[]{"abc", null}, new String[]{"abc", null});
    assertDiffResult(new int[]{1, 2}, new int[]{1, 3},
        new ExpectedDiff(new Diff(2, 3), new Element(1)));
    assertDiffResult(new int[]{1, 2}, new int[]{1, 2, 3},
        new ExpectedDiff(new Added(3), new Element(2)));
    assertDiffResult(new int[0], new int[]{1, 2},
        new ExpectedDiff(new Added(1), new Element(0)),
        new ExpectedDiff(new Added(2), new Element(1)));
    assertDiffResult(new String[]{"abc", "xyz"}, new String[]{"abc", null},
        new ExpectedDiff(new Diff("xyz", null), new Element(1)));
    assertDiffResult(new String[]{"abc", "xyz", "foo"}, new String[]{"abc", null, "foo", "bar"},
        new ExpectedDiff(new Diff("xyz", null), new Element(1)),
        new ExpectedDiff(new Added("bar"), new Element(3)));
    // now try comparing an array to an object that's not an array
    Object a, b;
    assertDiffResult(a = new String[]{"abc", "xyz", "foo"}, b = "abc", new ExpectedDiff(new Diff(a, b)));
    assertDiffResult(b, a, new ExpectedDiff(new Diff(b, a)));
    assertDiffResult(a, null, new ExpectedDiff(new Diff(a, null)));
    assertDiffResult(null, a, new ExpectedDiff(new Diff(null, a)));
  }

  public void testCollections() throws Exception {
    assertNoDiffs(arrayList(), arrayList());
    assertNoDiffs(arrayList(), linkedList());
    assertNoDiffs(arrayList(1, 2, 3), arrayList(1, 2, 3));
    assertNoDiffs(arrayList(1, 2, 3), linkedList(1, 2, 3));
    assertNoDiffs(arrayList("abc"), linkedList("abc"));
    assertDiffResult(arrayList("abc"), linkedList("xyz"),
        new ExpectedDiff(new Diff("abc", "xyz"), new Element(0)));
    assertNoDiffs(arrayList("abc"), SetUtils.newSet("abc"));
    assertDiffResult(arrayList("abc"), SetUtils.newSet("xyz"),
        new ExpectedDiff(new Diff("abc", "xyz"), new Element(0)));
    assertDiffResult(arrayList("abc", "xyz", "foo"), linkedList("abc", null, "foo", "bar"),
        new ExpectedDiff(new Diff("xyz", null), new Element(1)),
        new ExpectedDiff(new Added("bar"), new Element(3)));
    assertDiffResult(arrayList("abc", "xyz", "foo"), linkedList("abc", null),
        new ExpectedDiff(new Diff("xyz", null), new Element(1)),
        new ExpectedDiff(new Removed("foo"), new Element(2)));
    // now try comparing a Collection to an object that's not a Collection
    Object a, b;
    assertDiffResult(a = Arrays.asList("abc", "xyz", "foo"), b = "abc", new ExpectedDiff(new Diff(a, b)));
    assertDiffResult(b, a, new ExpectedDiff(new Diff(b, a)));
    assertDiffResult(a, null, new ExpectedDiff(new Diff(a, null)));
    assertDiffResult(null, a, new ExpectedDiff(new Diff(null, a)));
  }

  public void testWithCustomEqualsFunctions() throws Exception {
    // 1) test passing a custom equals function for a simple type
    evaluator.addEqualsFunction(String.class, new EqualsFunction<String>() {
      @Override
      public boolean eq(String a, String b) {
        return a.length() == b.length();  // consider the strings equal if they're the same length
      }
    });
    Object a, b;
    assertNoDiffs("abc", "xyz");
    assertDiffResult(a = "abc", b = "xy", new ExpectedDiff(new Diff(a, b)));

    // 2) now test with polymorphic types

    evaluator.addReflectionSpec(new MemberSet<Foo>(Foo.class).exclude(new MemberPattern.InheritedFrom(Object.class)));
    assertDiffResult(a = new FooSubA(1, "abc"), b = new FooSubB(2, "xyz"),
        new ExpectedDiff(new Diff(1, 2), new ClassMember(FooSubA.class.getMethod("getI"))));  // the only diff should be the value of I

    evaluator.addReflectionSpec(new MemberSet<Bar>(Bar.class).exclude(new MemberPattern.InheritedFrom(Object.class)));
    bar1.setFoo(new FooSubA(1, "abc"));
    bar2.setFoo(new FooSubB(1, "xyz"));
    assertNoDiffs(bar1, bar2);
    bar2.setFoo(new FooSubB(2, "xyz"));
    assertDiffResult(bar1, bar2,
        new ExpectedDiff(new Diff(1, 2), new ClassMember(Bar.class.getMethod("getFoo")), new ClassMember(FooSubA.class.getMethod("getI"))));  // the only diff should be the value of I
    bar2.setFoo(new FooSubB(2, "x"));
    assertDiffResult(bar1, bar2,
        new ExpectedDiff(new Diff(1, 2), new ClassMember(Bar.class.getMethod("getFoo")), new ClassMember(FooSubA.class.getMethod("getI"))),
        new ExpectedDiff(new Diff("abc", "x"), new ClassMember(Bar.class.getMethod("getFoo")), new ClassMember(FooSubA.class.getMethod("getS"))));

    // now add a custom EqualsFunction for FooSubA
    evaluator.addEqualsFunction(FooSubA.class, new EqualsFunction<FooSubA>() {
      @Override
      public boolean eq(FooSubA a, FooSubA b) {
        return a.getI() == b.getI();
      }
    });
    // repeat the prior diff and expect to see the same result (because bar2.getFoo() returns an instance of FooSubB
    assertDiffResult(bar1, bar2,
        new ExpectedDiff(new Diff(1, 2), new ClassMember(Bar.class.getMethod("getFoo")), new ClassMember(FooSubA.class.getMethod("getI"))),
        new ExpectedDiff(new Diff("abc", "x"), new ClassMember(Bar.class.getMethod("getFoo")), new ClassMember(FooSubA.class.getMethod("getS"))));
    // even if we set it to be an instance of FooSubA, nothing will change, because the MemberQuery we added earlier for Foo.class trumps the equals function we've just added
    bar2.setFoo(new FooSubA(2, "x"));
    assertDiffResult(bar1, bar2,
            new ExpectedDiff(new Diff(1, 2), new ClassMember(Bar.class.getMethod("getFoo")), new ClassMember(FooSubA.class.getMethod("getI"))),
            new ExpectedDiff(new Diff("abc", "x"), new ClassMember(Bar.class.getMethod("getFoo")), new ClassMember(FooSubA.class.getMethod("getS"))));
    // however, if we remove the member query for Foo.class, we should expect our custom equals function to be used
    evaluator.removeMemberQuery(Foo.class);
    bar2.setFoo(new FooSubA(2, "x"));
    assertDiffResult(bar1, bar2,
        new ExpectedDiff(new Diff(bar1.getFoo(), bar2.getFoo()), new ClassMember(Bar.class.getMethod("getFoo"))));
    bar2.setFoo(new FooSubA(1, "x"));
    assertNoDiffs(bar1, bar2);

    // now if we add a member query for FooSubA.class, then instances of FooSubA should be compared member-by-member
    evaluator.addReflectionSpec(new MemberSet<FooSubA>(FooSubA.class).exclude(new MemberPattern.InheritedFrom(Object.class)));
    assertDiffResult(bar1, bar2,
        new ExpectedDiff(new Diff("abc", "x"), new ClassMember(Bar.class.getMethod("getFoo")), new ClassMember(FooSubA.class.getMethod("getS"))));

    // now if we try to compare an instance of FooSubA vs. FooSubB, it should default to using Object.equals
    bar2.setFoo(new FooSubB(1, "x"));
    assertDiffResult(bar1, bar2,
        new ExpectedDiff(new Diff(bar1.getFoo(), bar2.getFoo()), new ClassMember(Bar.class.getMethod("getFoo"))));
  }

  private static <T> ArrayList<T> arrayList(T... elements) {
    return addAll(new ArrayList<T>(), elements);
  }

  private static <T> LinkedList<T> linkedList(T... elements) {
    return addAll(new LinkedList<T>(), elements);
  }

  private static <L extends List<T>, T> L addAll(L ret, T... elements) {
    ret.addAll(Arrays.asList(elements));
    return ret;
  }

  public void testMaps() throws Exception {
    assertNoDiffs(new HashMap(), new TreeMap());
    assertNoDiffs(MapUtils.hashMap(1, "abc"), MapUtils.hashMap(1, "abc"));
    assertNoDiffs(MapUtils.hashMap(1, "abc"), MapUtils.sortedMap(1, "abc"));
    assertNoDiffs(MapUtils.hashMap("xyz", 2), MapUtils.sortedMap("xyz", 2));
    assertNoDiffs(MapUtils.hashMap("xyz", 2, 1, "abc"), MapUtils.hashMap("xyz", 2, 1, "abc"));
    assertNoDiffs(MapUtils.hashMap("abc", 1, "xyz", 2), MapUtils.sortedMap("abc", 1, "xyz", 2));
    assertNoDiffs(MapUtils.hashMap("abc", 1, "xyz", null), MapUtils.sortedMap("abc", 1, "xyz", null));
    assertNoDiffs(MapUtils.hashMap(null, 1, "xyz", null), MapUtils.hashMap(null, 1, "xyz", null));
    assertDiffResult(MapUtils.hashMap(1, "abc"), MapUtils.hashMap(),
        new ExpectedDiff(new Removed("abc"), new MapEntry(1)));
    assertDiffResult(MapUtils.hashMap(1, "abc"), MapUtils.sortedMap(1, "xyz"),
        new ExpectedDiff(new Diff("abc", "xyz"), new MapEntry(1)));
    assertDiffResult(MapUtils.hashMap(1, "abc"), MapUtils.sortedMap(2, "abc", 1, "xyz"),
        new ExpectedDiff(new Diff("abc", "xyz"), new MapEntry(1)),
        new ExpectedDiff(new Added("abc"), new MapEntry(2)));
    assertDiffResult(MapUtils.hashMap(1, "abc"), MapUtils.sortedMap(2, "abc"),
        new ExpectedDiff(new Removed("abc"), new MapEntry(1)),
        new ExpectedDiff(new Added("abc"), new MapEntry(2)));
  }

  public void testClassMembers() throws Exception {
    // 0) without adding a MemberQuery, Foo instances will be compared using Object.equals
    assertDiffResult(foo1, foo2, new ExpectedDiff(new Diff(foo1, foo2)));

    // 1) if we add a MemberQuery for class Foo, without specifying any include/exclude filters, two instances
    // will differ in the results of their Object.toString and Object.hashCode methods
    evaluator.addReflectionSpec(new MemberSet<Foo>(Foo.class));
    assertDiffResult(foo1, foo2,
        new ExpectedDiff(new Diff(foo1.toString(), foo2.toString()), new ClassMember(Foo.class.getMethod("toString"))),
        new ExpectedDiff(new Diff(foo1.hashCode(), foo2.hashCode()), new ClassMember(Foo.class.getMethod("hashCode"))));

    // 2) if we exclude Foo's methods inherited from Object, there will be no diffs between foo1 and foo2
    evaluator.addReflectionSpec(new MemberSet<Foo>(Foo.class).excludeMethodsInheritedFromObject());
    assertNoDiffs(foo1, foo2);

    // 3) if we set Foo.bar, the results for getBar should no longer be equal (because Bar doesn't override equals)
    foo1.setBar(bar1);
    foo2.setBar(bar2);
    assertDiffResult(foo1, foo2,
        new ExpectedDiff(new Diff(bar1, bar2), new ClassMember(Foo.class.getMethod("getBar"))));

    // 4) if we include a MemberQuery for Bar, then Bar instances will be compared member-by-member
    evaluator.addReflectionSpec(new MemberSet<Bar>(Bar.class));
    assertDiffResult(foo1, foo2,
        new ExpectedDiff(new Diff(bar1.toString(), bar2.toString()), new ClassMember(Foo.class.getMethod("getBar")), new ClassMember(Bar.class.getMethod("toString"))),
        new ExpectedDiff(new Diff(bar1.hashCode(), bar2.hashCode()), new ClassMember(Foo.class.getMethod("getBar")), new ClassMember(Bar.class.getMethod("hashCode"))));

    // 5) if we exclude Bar's methods inherited from Object, there will be no diffs once again
    evaluator.addReflectionSpec(new MemberSet<Bar>(Bar.class).excludeMethodsInheritedFromObject());
    assertNoDiffs(foo1, foo2);

    // 6) test with circular references
    bar1.setFoo(foo1);
    bar2.setFoo(foo2);
    assertNoDiffs(foo1, foo2);

    bar1.setFoo(foo1);
    bar2.setFoo(foo1);
    assertNoDiffs(foo1, foo2);

    bar1.setFoo(foo2);
    bar2.setFoo(foo2);
    assertNoDiffs(foo1, foo2);

    bar1.setFoo(foo2);
    bar2.setFoo(foo1);
    assertNoDiffs(foo1, foo2);

    // 7) test some diffs between foo1 and foo2
    foo1.i++;
    assertDiffResult(foo1, foo2,
        new ExpectedDiff(new Diff(foo1.getI(), foo2.getI()), new ClassMember(Foo.class.getMethod("getI"))));

    // 8) test an array whose component type is compared member-by-member
    foo1.arrBar = new Bar[]{
        new Bar(1, "a"),
        new Bar(2, "b"),
    };
    foo2.arrBar = new Bar[]{
        new Bar(3, "c"),
        new Bar(2, "b"),
        new Bar(1, "a"),
    };

    assertDiffResult(foo1, foo2,
        new ExpectedDiff(new Diff(foo1.getI(), foo2.getI()), new ClassMember(Foo.class.getMethod("getI"))),
        new ExpectedDiff(new Diff("a", "c"), new ClassMember(Foo.class.getMethod("getArrBar")), new Element(0), new ClassMember(Bar.class.getMethod("getS"))),
        new ExpectedDiff(new Diff(1, 3), new ClassMember(Foo.class.getMethod("getArrBar")), new Element(0), new ClassMember(Bar.class.getMethod("getX"))),
        new ExpectedDiff(new Added(foo2.arrBar[2]), new ClassMember(Foo.class.getMethod("getArrBar")), new Element(2)));
  }

}