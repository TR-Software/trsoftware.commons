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
import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.server.io.StringPrintStream;
import solutions.trsoftware.commons.server.io.file.FileSet;
import solutions.trsoftware.commons.shared.util.SetUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.VersionNumber;
import solutions.trsoftware.commons.shared.util.reflect.ClassNameParser;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static javax.lang.model.element.Modifier.*;
import static solutions.trsoftware.commons.server.util.reflect.ReflectionUtils.*;

/**
 * @author Alex, 1/9/14
 */
public class ReflectionUtilsTest extends TestCase {

  public void testIsPrimitiveWrapper() throws Exception {
    assertTrue(isPrimitiveWrapper(Boolean.class));
    assertTrue(isPrimitiveWrapper(Integer.class));
    assertTrue(isPrimitiveWrapper(Double.class));
    assertFalse(isPrimitiveWrapper(getClass()));
    assertFalse(isPrimitiveWrapper(null));
  }

  public void testPrimitiveTypeFor() throws Exception {
    assertEquals(char.class, primitiveTypeFor(Character.class));
    assertEquals(short.class, primitiveTypeFor(Short.class));
    assertEquals(float.class, primitiveTypeFor(Float.class));
    assertNull(primitiveTypeFor(getClass()));
    assertNull(primitiveTypeFor(null));
  }

  public void testUnwrap() throws Exception {
    assertEquals(char.class, unwrap(Character.class));
    assertEquals(short.class, unwrap(Short.class));
    assertEquals(float.class, unwrap(Float.class));
    // if the arg is not a wrapper, should just return the arg
    assertSame(char.class, unwrap(char.class));
    assertSame(short.class, unwrap(short.class));
    assertSame(float.class, unwrap(float.class));
    assertSame(Foo.class, unwrap(Foo.class));
  }

  public void testWrapperTypeFor() throws Exception {
    assertEquals(Byte.class, wrapperTypeFor(byte.class));
    assertEquals(Long.class, wrapperTypeFor(long.class));
    assertEquals(Void.class, wrapperTypeFor(void.class));
    assertNull(wrapperTypeFor(getClass()));
    assertNull(wrapperTypeFor(null));
  }

  public void testGetActualTypeArguments() throws Exception {
    assertEquals(Arrays.<Type>asList(Integer.class, String.class),
        Arrays.asList(getGenericTypeArgumentsForInterface(FooIntString.class, IFoo.class)));
    assertNull(getGenericTypeArgumentsForInterface(Foo.class, IFoo.class));
    assertNull(getGenericTypeArgumentsForInterface(FooIntString.class, Object.class));
  }


  public void testGetAllTypesAssignableFrom() throws Exception {
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

  public void testGetAllTypesAssignableFromAll() throws Exception {
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

  public void testParseModifiers() throws Exception {
    assertEquals(Arrays.asList(PUBLIC), parseModifiers("public"));
    assertEquals(Arrays.asList(PUBLIC, STATIC), parseModifiers("  public\n static\n"));
    assertEquals(Arrays.asList(DEFAULT, STRICTFP), parseModifiers("default strictfp void"));  // void is not a modifier
  }

  public void testGetCompilerOutputDir() throws Exception {
    Class<? extends ReflectionUtilsTest> thisClass = getClass();
    File result = getCompilerOutputDir(thisClass);
    System.out.println("result = " + result);
    assertTrue(result.exists());
    assertTrue(result.isDirectory());
    // confirm that the result indeed contains this class
    FileSet allClassFiles = new FileSet(result);
    assertTrue(allClassFiles.contains(ReflectionUtils.getClassFile(thisClass).toFile()));
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

  public void testIsJavaKeyword() throws Exception {
    /*
      This list of keywords obtained from https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.9
      NOTE: it applies only to Java 1.8, and may not work for other Java versions
        (for example Java 9 also reserves "_" as a keyword)
    */
    String[] keywordsFromJLS = {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"};

    for (String k : keywordsFromJLS) {
      // NOTE: this test could fail if running on a JRE older than 1.8
      assertTrue(k, isJavaKeyword(k));
      assertFalse(k, isJavaKeyword(k + "_"));
    }
  }

  public void testGetClassFile() throws Exception {
    class LocalClass {}
    assertTrue(getClassFile(getClass()).exists());
    assertTrue(getClassFile(ReflectionUtils.class).exists());
    assertTrue(getClassFile(NestedClass.class).exists());
    assertTrue(getClassFile(InnerClass.class).exists());
    assertTrue(getClassFile(LocalClass.class).exists());
    // now try some anonymous classes
    assertTrue(getClassFile(new NestedClass(){}.getClass()).exists());
    assertTrue(getClassFile(new NestedClass(){}.getClass()).exists());
    // now try some array classes
    assertTrue(getClassFile(ReflectionUtils[].class).exists());
    assertTrue(getClassFile(ReflectionUtils[][].class).exists());
    assertTrue(getClassFile(NestedClass[].class).exists());
    assertTrue(getClassFile(InnerClass[].class).exists());
    assertTrue(getClassFile(LocalClass[].class).exists());
    // now try some classes from the Java runtime
    assertNotNull(getClassFile(Map.class));
    assertNotNull(getClassFile(Map.Entry.class));
    // now try some primitives
    assertNull(getClassFile(int.class));
    assertNull(getClassFile(void.class));
    assertNull(getClassFile(byte[].class));
  }

  private File getClassFile(Class cls) {
    StringPrintStream msg = new StringPrintStream();
    msg.printf("getClassFile(%s)", cls);
    ResourceLocator classFileResource = ReflectionUtils.getClassFile(cls);
    File file = null;
    if (classFileResource == null) {
      msg.printf("%n -> null");
    }
    else {
      msg.printf("%n -> (resource) %s", classFileResource);
      file = classFileResource.toFile();
      if (file != null) {
        msg.printf("%n -> (file) %s", file);
        // check that the filename corresponds to the binary name of the class
        ClassNameParser classNameParser = new ClassNameParser(getRootComponentTypeOfArray(cls));
        assertEquals(classNameParser.getComplexName() + ".class", file.getName());
      }
    }
    System.out.println(msg);
    return file;
  }

  public void testGetSourceDir() throws Exception {
    // should work for this test class
    Path srcPath = getSrcPath(getClass());
    assertTrue(Files.exists(srcPath));
    String srcFileName = getClass().getSimpleName() + ".java";
    assertTrue(Files.exists(srcPath.resolve(srcFileName)));

    // now try some classes for which it might not work
    class LocalClass {}
    // should work for a local class
    assertEquals(srcPath, getSrcPath(LocalClass.class));
    // but shouldn't work for system classes or classes that come from a JAR
    // (we won't even bother checking any assertions for these, because the results are hard to predict)
    getSrcPath(String.class);
    getSrcPath(int.class);
  }

  private Path getSrcPath(Class cls) {
    System.out.printf("getSourceDir(%s)", cls);
    ResourceLocator resourceLocator = ReflectionUtils.getSourceDir(cls);
    System.out.printf("%n -> (resource) %s", resourceLocator);
    System.out.printf("%n -> (resource URL) %s%n", resourceLocator.getURL());
    try {
      Path path = resourceLocator.toPath();
      System.out.printf(" -> (path) %s%n", path);
      return path;
    }
    catch (FileSystemNotFoundException e) {
      return null;
    }
  }

  public void testGetSourceRoot() throws Exception {
    Class<? extends ReflectionUtilsTest> thisClass = getClass();
    Path sourceRoot = getSourceRoot(thisClass);
    System.out.println("sourceRoot = " + sourceRoot);
    Path thisFile = sourceRoot.resolve(
        thisClass.getName().replace('.', File.separatorChar) + ".java");
    System.out.println("thisFile = " + thisFile);
    assertTrue(Files.exists(thisFile));
  }

  public void testGetSourceFile() throws Exception {
    Class<? extends ReflectionUtilsTest> thisClass = getClass();
    Path sourceRoot = getSourceRoot(thisClass);
    System.out.println("sourceRoot = " + sourceRoot);
    Path expected = Paths.get(sourceRoot.toString()
        + File.separator
        + thisClass.getName().replace('.', '/')
        + ".java");
    Path actual = getSourceFile(thisClass);
    System.out.println("result = " + actual);
    assertEquals(expected, actual);
    assertTrue(Files.exists(actual));

    // we also test that this method works for classes with any level of nesting, as well as for local and anonymous classes
    getInnerLocalAndAnonClasses().forEach(arg -> {
      assertEquals(expected, getSourceFile(arg));
    });
  }

  public void testGetEnclosingClass() throws Exception {
    Class thisClass = getClass();
    // we test that this method works for classes with any level of nesting, as well as for local and anonymous classes
    getInnerLocalAndAnonClasses().forEach(arg -> {
      assertEquals(thisClass, doGetEnclosingClass(arg));
    });
  }

  /**
   * Generates some test data.
   * @return an assortment of inner, local, and anonymous classes, with various levels of nesting
   */
  private List<Class> getInnerLocalAndAnonClasses() throws Exception {
    Class thisClass = getClass();
    EnclosingClassTester tester = new EnclosingClassTester();
    return Arrays.asList(thisClass,
        EnclosingClassTester.class,
        EnclosingClassTester.Inner.class,
        EnclosingClassTester.Inner.InnerInner.class,
        tester.anonClassFromConstructor,
        tester.anonClassFromFieldDecl,
        tester.localClassFromConstructor,
        EnclosingClassTester.getLocalClassFromMethod(),
        EnclosingClassTester.getLocalInnerClassFromMethod(),
        EnclosingClassTester.localClassFromStaticInit,
        EnclosingClassTester.localClassFromStaticInitAnon,
        EnclosingClassTester.localInnerClassFromStaticInit,
        EnclosingClassTester.localInnerClassFromStaticInitAnon);
  }

  /**
   * Invokes {@link ReflectionUtils#getEnclosingClass(Class)} with the given arg, and prints its return value.
   * @return the result of {@link ReflectionUtils#getEnclosingClass(Class)}
   */
  private Class doGetEnclosingClass(Class cls) {
    Class ret = getEnclosingClass(cls);
    System.out.println(StringUtils.methodCallToStringWithResult("getEnclosingClass", ret, cls));
    return ret;
  }

  public void testGetJavaSpecVersion() throws Exception {
    // print out all system properties (just to see which ones might contain the Java version
    SortedMap<String, String> sysProps = new TreeMap<>();
    for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
      Object key = entry.getKey();
      Object value = entry.getValue();
      if (key instanceof String && (value instanceof String || value == null)) {
        sysProps.put((String)key, (String)value);
      }
    }
    for (Map.Entry<String, String> entry : sysProps.entrySet()) {
      if (!entry.getKey().equals("line.separator")) {  // printing the line separator is pointless (it will just be an empty line)
        System.out.println(entry);
      }
    }

    VersionNumber javaSpecVersion = getJavaSpecVersion();
    assertNotNull(javaSpecVersion);
    System.out.println("\nparsed javaSpecVersion = " + javaSpecVersion);
    assertEquals(VersionNumber.parse(System.getProperty("java.specification.version")), javaSpecVersion);
  }

  public void testIsJunitTestCase() throws Exception {
    assertTrue(isJunit3TestCase(getClass()));  // this class is a unit test
    assertFalse(isJunit3TestCase(ReflectionUtils.class));  // but ReflectionUtils is not
  }

  public void testGetAllDeclaredFields() throws Exception {
    // 1) test some edge cases
    assertEquals(Collections.emptySet(), getAllDeclaredFields(int.class));  // primitive
    assertEquals(Collections.emptySet(), getAllDeclaredFields(Object.class));  // Object has no fields
    assertEquals(Collections.emptySet(), getAllDeclaredFields(Sub[].class));  // array

    // 2) test with the classes in our dummy hierarchy
    LinkedHashSet<Field> baseFields = SetUtils.newSet(
        Base.class.getDeclaredField("a"),
        Base.class.getDeclaredField("b")
    );
    LinkedHashSet<Field> subFields = SetUtils.newSet(
        Sub.class.getDeclaredField("c"),
        Sub.class.getDeclaredField("d")
    );
    Set<Field> allFields = SetUtils.union(baseFields, subFields);
    assertEquals(baseFields, getAllDeclaredFields(Base.class));
    assertEquals(allFields, getAllDeclaredFields(Sub.class));

    // Note: inner classes have an additional synthetic field "this$0"
    class InnerSub extends Sub {
      private Object e;
    }
    LinkedHashSet<Field> innerSubFields = SetUtils.newSet(
        InnerSub.class.getDeclaredField("e"),
        InnerSub.class.getDeclaredField("this$0"));
    assertEquals(SetUtils.union(allFields, innerSubFields),
        getAllDeclaredFields(InnerSub.class));
  }

  private static class Base {
    public int a;
    private int b;

    public int sum() {
      return a + b;
    }
  }

  private static class Sub extends Base {
    private int c, d;
  }

  private static class NestedClass {}
  private class InnerClass {}

  public void testGetRootComponentTypeOfArray() throws Exception {
    assertEquals(Foo.class, getRootComponentTypeOfArray(Foo.class));
    assertEquals(Foo.class, getRootComponentTypeOfArray(Foo[].class));
    assertEquals(Foo.class, getRootComponentTypeOfArray(Foo[][].class));
    assertEquals(Foo.class, getRootComponentTypeOfArray(Foo[][][].class));
  }

  public void testListPublicGetters() throws Exception {
    Class<SimpleBean> cls = SimpleBean.class;
    List<Method> expected = Arrays.asList(
        cls.getMethod("getX"),
        cls.getMethod("getY"),
        cls.getMethod("getName"),
        cls.getMethod("isFoo"),
        cls.getMethod("getClass")
    );
    assertEquals(new HashSet<>(expected), new HashSet<>(listPublicGetters(cls)));
  }

  private static class SimpleBean {

    private int x, y;
    private String name;
    private boolean foo;

    public SimpleBean() {
    }

    public SimpleBean(int x, int y, String name, boolean foo) {
      this.x = x;
      this.y = y;
      this.name = name;
      this.foo = foo;
    }

    public int getX() {
      return x;
    }

    public void setX(int x) {
      this.x = x;
    }

    public int getY() {
      return y;
    }

    public void setY(int y) {
      this.y = y;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public boolean isFoo() {
      return foo;
    }

    public void setFoo(boolean foo) {
      this.foo = foo;
    }
  }


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

  public static class EnclosingClassTester {
    static class Inner {
      class InnerInner {

      }
    }
    private final static Class localClassFromStaticInit;
    private final static Class localClassFromStaticInitAnon;
    private final static Class localInnerClassFromStaticInit;
    private final static Class localInnerClassFromStaticInitAnon;
    static {
      class Local {
        class Inner {
          Class anon = new IBar(){}.getClass();
        }
        Class anon = new IBar(){}.getClass();
        Inner innerInstance = new Inner();
      }
      Local localInstance = new Local();
      localClassFromStaticInit = Local.class;
      localClassFromStaticInitAnon = localInstance.anon;
      localInnerClassFromStaticInit = Local.Inner.class;
      localInnerClassFromStaticInitAnon = localInstance.innerInstance.anon;
    }

    private Class anonClassFromConstructor;
    private Class anonClassFromFieldDecl = new IBar(){}.getClass();

    private Class localClassFromConstructor;

    public EnclosingClassTester() {
      anonClassFromConstructor = new IFoo(){}.getClass();
      class Local {}
      localClassFromConstructor = Local.class;
    }

    static Class getLocalClassFromMethod() {
      class Local {}
      return Local.class;
    }

    static Class getLocalInnerClassFromMethod() {
      class Local {
        class Inner {}
      }
      return Local.Inner.class;
    }


  }

}
