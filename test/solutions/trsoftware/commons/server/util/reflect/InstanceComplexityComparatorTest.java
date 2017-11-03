package solutions.trsoftware.commons.server.util.reflect;

import junit.framework.TestCase;


import java.io.Serializable;
import java.util.*;

import static solutions.trsoftware.commons.server.util.reflect.InstanceComplexityComparator.*;

public class InstanceComplexityComparatorTest extends TestCase {

  private static interface IFoo {
    int getFoo();
    public static IFoo IMPL = new Foo();  // static fields shouldn't count

    public boolean equals(Object o);  // methods already defined by Object should not be counted twice
  }

  private static class Foo implements IFoo {
    private int x;  // private fields shouldn't count
    private int foo;
    public String bar;  // but public fields should

    private Foo() {}

    private Foo(int foo, String bar) {
      this.foo = foo;
      this.bar = bar;
    }

    @Override
    public int getFoo() {
      return foo;
    }
  }

  private static class Bar {}
  private static class Baz {}
  private static class BarSub extends Bar {}

  public void testComplexityOf() throws Exception {
    assertEquals(1, complexityOf(Object.class)); // the "complexity" of the Object class is defined as 1
    // the following classes don't have any members that are not also present in object
    assertEquals(1, complexityOf(Bar.class));
    assertEquals(1, complexityOf(Baz.class));
    assertEquals(1, complexityOf(BarSub.class));
    // the following classes do have some additional members
    assertEquals(2, complexityOf(IFoo.class));
    assertEquals(3, complexityOf(Foo.class));
  }

  public void testCompare() throws Exception {
    InstanceComplexityComparator cmp = get();

    assertEquals(0, cmp.compare(Object.class, Object.class));
    assertEquals(0, cmp.compare(IFoo.class, IFoo.class));
    assertEquals(0, cmp.compare(Foo.class, Foo.class));

    assertTrue(cmp.compare(Foo.class, Object.class) < 0);
    assertTrue(cmp.compare(IFoo.class, Object.class) < 0);
    assertTrue(cmp.compare(Foo.class, IFoo.class) < 0);

    assertTrue(cmp.compare(Object.class, Foo.class) > 0);
    assertTrue(cmp.compare(Object.class, IFoo.class) > 0);
    assertTrue(cmp.compare(IFoo.class, Foo.class) > 0);

    // any class should come before the Object class
    assertTrue(cmp.compare(Foo.class, Object.class) < 0);
    assertTrue(cmp.compare(Serializable.class, Object.class) < 0);
    assertTrue(cmp.compare(Collection.class, Object.class) < 0);

    // any implementation of an interface should come before the interface, and any subclass should come before its superclass
    assertTrue(cmp.compare(AbstractMap.class, Map.class) < 0);
    assertTrue(cmp.compare(HashMap.class, Map.class) < 0);
    assertTrue(cmp.compare(HashMap.class, AbstractMap.class) < 0);
    assertTrue(cmp.compare(HashMap.class, HashMap.class) == 0);
    assertTrue(cmp.compare(BarSub.class, Bar.class) < 0);
    assertTrue(cmp.compare(Baz.class, Bar.class) == 0);
  }

  public void testSorting() throws Exception {
    assertSortOrder(Foo.class, IFoo.class, Object.class);
    assertSortOrder(HashMap.class, AbstractMap.class, Map.class);
    assertSortOrder(HashMap.class, AbstractMap.class, Map.class, Object.class);
  }

  private void assertSortOrder(Class<?>... expected) {
    // randomize the args and sort them (10 times, to ensure that the sort order is not accidental)
    for (int i = 0; i < 10; i++) {
      List<Class<?>> list = Arrays.asList(expected);
      Collections.shuffle(list);
      Collections.sort(list, get());
      // the comparator should have sorted our collection in order of decreasing complexity of the classes
      assertEquals(Arrays.asList(expected), list);
    }
  }

}