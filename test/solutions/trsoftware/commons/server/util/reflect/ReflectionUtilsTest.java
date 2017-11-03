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

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.util.SetUtils;
import solutions.trsoftware.commons.client.util.StringUtils;
import solutions.trsoftware.commons.server.io.FileSet;
import solutions.trsoftware.commons.server.io.ServerIOUtils;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

import static solutions.trsoftware.commons.server.util.reflect.ReflectionUtils.*;

/**
 * @author Alex, 1/9/14
 */
public class ReflectionUtilsTest extends TestCase {

  public interface IFoo<A,B> {}
  public interface IBar {}
  public static class Foo implements IFoo {}
  public static class FooIntString implements IFoo<Integer, String> {}

  public interface IFooSubA extends IFoo {}
  public interface IFooSubB extends IFoo {}
  public static class FooSubA implements IFooSubA {}
  public static class FooSubB implements IFooSubB {}
  public static class FooSubAB implements IFooSubA, IFooSubB {}
  public static class FooSubABSub extends FooSubAB implements IBar {}


  public void testGetActualTypeArguments() throws Exception {
    assertEquals(Arrays.<Type>asList(Integer.class, String.class),
        Arrays.asList(getGenericTypeArgumentsForInterface(FooIntString.class, IFoo.class)));
    assertNull(getGenericTypeArgumentsForInterface(Foo.class, IFoo.class));
    assertNull(getGenericTypeArgumentsForInterface(FooIntString.class, Object.class));
  }


  public void test_getAllTypesAssignableFrom() throws Exception {
    assertAllAssignableFrom(null,
        expectedSet());

    assertAllAssignableFrom(Object.class,
        expectedSet(Object.class));

    assertAllAssignableFrom(int.class,
        expectedSet(int.class));  // primitives aren't assignable to Object

    assertAllAssignableFrom(int[].class,
        expectedSet(int[].class, Object.class, Cloneable.class, Serializable.class)); // all array classes implement Cloneable and Serializable

    assertAllAssignableFrom(Foo.class,
        expectedSet(Foo.class, IFoo.class, Object.class));

    assertAllAssignableFrom(IFoo.class,
        expectedSet(IFoo.class, Object.class));

    assertAllAssignableFrom(IFooSubA.class,
        expectedSet(IFooSubA.class, IFoo.class, Object.class));

    assertAllAssignableFrom(FooSubAB.class,
        expectedSet(FooSubAB.class, IFooSubA.class, IFoo.class, IFooSubB.class, Object.class));

    assertAllAssignableFrom(FooSubABSub.class,
        expectedSet(FooSubABSub.class, IBar.class, FooSubAB.class, IFooSubA.class, IFoo.class, IFooSubB.class, Object.class));
  }

  public void test_getAllTypesAssignableFromAll() throws Exception {
    assertAllAssignableFromAll(
        new Class[]{FooSubAB.class},
        expectedSet(FooSubAB.class, IFooSubA.class, IFoo.class, IFooSubB.class, Object.class));

    assertAllAssignableFromAll(
        new Class[]{FooSubAB.class, FooSubABSub.class},
        expectedSet(FooSubAB.class, IFooSubA.class, IFoo.class, IFooSubB.class, Object.class));

    assertAllAssignableFromAll(
        new Class[]{FooSubAB.class, FooSubABSub.class, IFooSubA.class},
        expectedSet(IFooSubA.class, IFoo.class, Object.class));

    assertAllAssignableFromAll(
         new Class[]{FooSubAB.class, FooSubABSub.class, IFooSubA.class, Object.class},
        expectedSet(Object.class));

    assertAllAssignableFromAll(
         new Class[]{FooSubAB.class, FooSubABSub.class, IFooSubA.class, int.class},
        expectedSet());  // no classes have anything in common with the primitive type int

    assertAllAssignableFromAll(
        new Class[]{LinkedList.class, ArrayList.class},
        expectedSet(List.class, Collection.class, Iterable.class, Cloneable.class, Serializable.class, AbstractList.class, AbstractCollection.class, Object.class));

    assertAllAssignableFromAll(
        new Class[]{LinkedList.class, ArrayList.class, int[].class, Integer[].class},
        expectedSet(Cloneable.class, Serializable.class, Object.class));

    assertAllAssignableFromAll(
        new Class[]{LinkedList.class, ArrayList.class, int[].class, Integer[].class, null},
        expectedSet());  // no classes have anything in common with a null arg
  }

  public void test_getCompilerOutputDir() throws Exception {
    Class<? extends ReflectionUtilsTest> thisClass = getClass();
    File result = getCompilerOutputDir(thisClass);
    System.out.println("result = " + result);
    assertTrue(result.exists());
    assertTrue(result.isDirectory());
    // confirm that the result indeed contains this class
    FileSet allClassFiles = new FileSet(result);
    assertTrue(allClassFiles.contains(ServerIOUtils.getClassFile(thisClass)));
  }

  private static void assertAllAssignableFrom(Class<?> arg, Set<Class<?>> expected) {
    Set<Class<?>> actual = getAllTypesAssignableFrom(arg);
    System.out.println(StringUtils.methodCallToStringWithResult("getAllTypesAssignableFrom", actual, arg));
    assertEquals(expected, actual);
    for (Class<?> x : actual) {
      assertTrue(x.isAssignableFrom(arg));
    }
  }

  private static void assertAllAssignableFromAll(Class<?>[] args, Set<Class<?>> expected) {
    Set<Class<?>> actual = getAllTypesAssignableFromAll(args);
    System.out.println(StringUtils.methodCallToStringWithResult("getAllTypesAssignableFromAll", actual, args));
    assertEquals(expected, actual);
    for (Class<?> x : actual) {
      for (Class arg : args) {
        assertTrue(x.isAssignableFrom(arg));
      }
    }
  }

  private static Set<Class<?>> expectedSet(Class<?>... args) {
    return SetUtils.newSet(args);
  }


}
