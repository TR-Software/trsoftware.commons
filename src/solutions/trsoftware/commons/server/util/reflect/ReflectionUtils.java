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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.shared.util.StringTokenizer;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.VersionNumber;
import solutions.trsoftware.commons.shared.util.callables.Function0;
import solutions.trsoftware.commons.shared.util.callables.FunctionN;
import solutions.trsoftware.commons.shared.util.reflect.ClassNameParser;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

/**
 * @author Alex, 1/5/14
 */
public abstract class ReflectionUtils {


  // NOTE: the following 2 methods borrows from java.beans.ReflectionUtils, which unfortunately is not public

  /**
   * Maps the primitive wrapper types to their corresponding primitive types.
   */
  public static final BiMap<Class, Class> WRAPPER_TYPES = ImmutableBiMap.<Class, Class>builder()
      .put(Boolean.class, Boolean.TYPE)
      .put(Byte.class, Byte.TYPE)
      .put(Character.class, Character.TYPE)
      .put(Short.class, Short.TYPE)
      .put(Integer.class, Integer.TYPE)
      .put(Long.class, Long.TYPE)
      .put(Float.class, Float.TYPE)
      .put(Double.class, Double.TYPE)
      .put(Void.class, Void.TYPE)
      .build();

  /**
   * @return true iff the given type is one of the wrapper classes for a primitive.
   */
  public static boolean isPrimitiveWrapper(Class type) {
    return WRAPPER_TYPES.containsKey(type);
  }

  /**
   * @return the primitive type corresponding to the given wrapper class, or {@code null} if the given class is not
   * a wrapper type.
   */
  public static Class primitiveTypeFor(Class wrapper) {
    return WRAPPER_TYPES.get(wrapper);
  }

  /**
   * @return the wrapper class corresponding to the given primitive type, or {@code null} if the given class is not
   * a primitive type.
   */
  public static Class wrapperTypeFor(Class primitive) {
    return WRAPPER_TYPES.inverse().get(primitive);
  }

  /**
   * @return a list of modifier constants parsed from the given string
   * @see Modifier
   */
  public static List<Modifier> parseModifiers(String str) {
    ArrayList<Modifier> modifiers = new ArrayList<>();
    StringTokenizer tok = new StringTokenizer(str);
    while (tok.hasNext()) {
      String next = tok.next();
      for (Modifier modifier : Modifier.values()) {
        // NOTE: there might be a more efficient way of doing this, but this is good enough b/c the number of modifiers should be sufficiently small
        if (modifier.toString().equals(next))
          modifiers.add(modifier);
      }
    }
    return modifiers;
  }

  /**
   * Creates a factory for the given class using the given constructor args. Any checked exception caught
   * while trying to invoke the constructor by reflection will be rethrown as {@link IllegalArgumentException}.
   *
   * @return A factory that will keep creating new instances of cls, based on the given constructor args.
   */
  public static <T> FunctionN<T> newInstanceFactory(Class<T> cls, Class... constructorParamTypes) {
    try {
      final Constructor<T> constructor = cls.getConstructor(constructorParamTypes);
      constructor.setAccessible(true);
      return new FunctionN<T>() {
        @Override
        public T call(Object... args) {
          try {
            return constructor.newInstance(args);
          }
          catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
          }
        }
      };
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Creates a factory for the given class using its default (0-arg) constructor. Any checked exception caught
   * while trying to invoke the constructor by reflection will be rethrown as {@link IllegalArgumentException}.
   *
   * @return A factory that will keep creating new instances of cls, based on the given constructor args.
   */
  public static <T> Function0<T> newInstanceFactory0(final Class<T> cls) {
    return new Function0<T>() {
      @Override
      public T call() {
        return newInstanceUnchecked(cls);
      }
    };
  }

  /**
   * Instantiates the given class using its default (0-arg) constructor. Any checked exception caught
   * while trying to invoke the constructor by reflection will be rethrown as {@link IllegalArgumentException}.
   *
   * @param cls the class to instantiate
   * @param <T> the generic type of the class
   * @return a new instance of the given class
   */
  public static <T> T newInstanceUnchecked(Class<T> cls) {
    try {
      return cls.newInstance();
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * If the given {@code cls} inherits a generic interface of the type specified by the
   * {@code parametrizedInterface} argument, then its actual type arguments are returned, otherwise returns null.
   * <p>
   * For example, given the following declarations:
   * </p>
   * <pre>
   * interface Foo{@literal <A,B>} {}
   * class Bar implements Foo{@literal <Integer, String>} {}
   * </pre>
   * <p>
   * The result of {@code getActualTypeArguments(Bar.class, Foo.class)} will be {@code [Integer.class, String.class] }
   * </p>
   *
   */
  public static Type[] getGenericTypeArgumentsForInterface(Class cls, Class parametrizedInterface) {
    Type[] genericInterfaces = cls.getGenericInterfaces();
    for (Type genericInterface : genericInterfaces) {
      if (genericInterface instanceof ParameterizedType) {
        ParameterizedType parameterizedInterface = (ParameterizedType)genericInterface;
        if (parameterizedInterface.getRawType() == parametrizedInterface)
          return parameterizedInterface.getActualTypeArguments();
      }
    }
    return null;
  }

  public static void assertType(Object input, Class<?> expectedType) {
    Class givenType = input.getClass();
    if (!expectedType.isAssignableFrom(givenType))
      throw new IllegalArgumentException(String.format(
          "The object %s must be of type %s, but is actually of type %s", String.valueOf(input), expectedType, givenType));
  }

  /**
   * @return the names of all fields declared by the given class (excluding any synthetic fields like "this$0").
   */
  public static List<String> getFieldNames(Class cls) {
    Field[] fields = cls.getDeclaredFields();
    ArrayList<String> names = new ArrayList<String>();
    for (Field field : fields) {
      if (!field.isSynthetic())
        names.add(field.getName());
    }
    return names;
  }

  /**
   * @return The set of all types that {@code cls} can be cast to: this includes all superclasses and
   * interfaces implemented by {@code cls}, recursively.  In other words, will return the set
   * <code>
   *   {X &forall;X s.t. X.isAssignableFrom(cls) == true}
   * </code>
   * The iterator of the resulting set will first return {@code cls}, followed by the transitive closure of all the interfaces
   * implemented by {@code cls}, and finally, the transitive closure of {@code cls.getSuperclass()}.
   */
  public static Set<Class<?>> getAllTypesAssignableFrom(Class<?> cls) {
    if (cls == null)
      return Collections.emptySet();
    class AssignableTypesSet extends LinkedHashSet<Class<?>> {
      private AssignableTypesSet(Class<?> cls) {
        expand(cls);
      }
      private AssignableTypesSet expand(Class<?> cls) {
        add(cls);
        // recursively compute the transitive closure
        for (Class iFace : cls.getInterfaces())
          expand(iFace);
        Class<?> superclass = cls.getSuperclass();
        if (superclass != null)
          expand(superclass);
        else if (cls.isInterface())
          add(Object.class);  // we must be sure to include Object explicitly when cls is an interface (doesn't have a superclass), because Object is always assignable from any interface
        return this;
      }
    }
    return new AssignableTypesSet(cls);
  }

  /**
   * @return The set of all types that all of the args can be cast to. In other words, will return the
   * intersection of sets:
   * <code>T<sub>1</sub> &cap; T<sub>2</sub> &hellip; &cap; T<sub>n</sub></code>, where
   * <code>T<sub>i</sub></code> is the set returned by {@link #getAllTypesAssignableFrom(Class)} for <code>args[i]</code>.
   */
  public static Set<Class<?>> getAllTypesAssignableFromAll(Class<?>... args) {
    if (args.length == 0)
      return Collections.emptySet();
    Set<Class<?>> ret = getAllTypesAssignableFrom(args[0]);
    for (int i = 1; i < args.length; i++) {
      ret.retainAll(getAllTypesAssignableFrom(args[i]));
    }
    return ret;
  }

  /**
   * @return A string representation of the given member that's prettier (shorter) than {@link Member#toString()}.
   */
  public static String toString(Member member) {
    if (member == null)
      return String.valueOf(member);
    StringBuilder buf = new StringBuilder(32);
    buf.append(member.getDeclaringClass().getSimpleName()).append('.').append(member.getName());
    if (member instanceof Method) {
      Method method = (Method)member;
      buf.append('(');
      Class<?>[] paramTypes = method.getParameterTypes();
      for (int j = 0; j < paramTypes.length; j++) {
        if (j > 0)
          buf.append(", ");
        buf.append(paramTypes[j].getSimpleName());
      }
      buf.append(')');
    }
    return buf.toString();
  }

  /** @return all members accessible from an instance of the given class */
  public static List<Member> listMembersAccessibleFrom(Class<?> targetClass) {
    // TODO: unit test this method
    List<Member> ret = new ArrayList<Member>();
    Collections.addAll(ret, targetClass.getFields());
    Collections.addAll(ret, targetClass.getMethods());
    return ret;
  }

  /**
   * Returns a list of all the "getter" methods in the given class.  In other words, all the {@code public} instance
   * methods that take no arguments, and have a name that starts with {@code "get"} or {@code "is"}
   * @return the getter methods in the given class
   */
  public static List<Method> listPublicGetters(Class<?> targetClass) {
    // TODO: unit test this method
    List<Method> ret = new ArrayList<>();
    Method[] publicMethods = targetClass.getMethods();
    for (Method method : publicMethods) {
      String name = method.getName();
      if (method.getParameterTypes().length == 0
          && (name.startsWith("get") || name.startsWith("is"))) {
        ret.add(method);
      }
    }
    return ret;
  }

  /** @return all members declared in the given class */
  public static List<Member> listMembersDeclaredIn(Class<?> targetClass) {
    // TODO: unit test this method
    List<Member> ret = new ArrayList<Member>();
    Collections.addAll(ret, targetClass.getDeclaredFields());
    Collections.addAll(ret, targetClass.getDeclaredMethods());
    return ret;
  }

  /**
   * Infers the compiler output path for the project by using the given class as a reference point.
   * @param refClass A reference class to use in performing the classpath lookup.
   * @return the root directory of the subtree from where the given class was loaded.  In most cases this would be
   * the compiler output path for the project (containing the compiled {@code .class} files)
   */
  public static Path getCompilerOutputPath(Class refClass) {
    // get the directory containing this .class file
    final Path refClassFile = Paths.get(getClassFile(refClass).getURI());
    final Path refClassDir = refClassFile.getParent();
    // go up to the root dir of the compiler output
    // (by going up the directory tree the same number of steps as the number of packages above this class)
    // we determine the number of steps to go up by counting the number of dots in the package name
    // example: package "foo.bar" contains 1 dot, so we want to go up 2 steps
    int nSteps = StringUtils.count(refClass.getPackage().getName(), '.') + 1;
    Path rootDir = refClassDir;
    for (int i = 0; i < nSteps; i++) {
      rootDir = rootDir.getParent();
    }
    return rootDir;
  }

  // TODO: extract the methods that attempt discover locations of .class/.java files to a separate util class

  /**
   * Infers the compiler output path for the project by using the given class as a reference point.
   * @param refClass A reference class to use in performing the classpath lookup.
   * @return the root directory of the subtree from where the given class was loaded.  In most cases this would be
   * the compiler output path for the project (containing the compiled {@code .class} files)
   */
  public static File getCompilerOutputDir(Class refClass) {
    return getCompilerOutputPath(refClass).toFile();
  }

  /**
   * Delegates to {@link SourceVersion#isKeyword}, which decides whether the arg is a reserved Java keyword or literal
   * (like {@code true},  {@code false}, or {@code null}) in the latest source (i.e. Java language) version
   * implemented by the current JRE.
   *
   * @return {@code true} iff the argument is a reserved keyword or literal.
   *
   * @see SourceVersion#isKeyword(CharSequence)
   * @see SourceVersion#keywords
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.9">JLS §3.9 (Keywords)</a>
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html">JLS Chapter 3 (Lexical Structure)</a>
   * @see <a href="https://stackoverflow.com/a/54141029/1965404">StackOverflow</a>
   */
  public static boolean isJavaKeyword(String str) {
    return SourceVersion.isKeyword(str);
  }


  /**
   * Every array in Java has its own class (e.g. {@code Foo.class != Foo[].class != Foo[][].class != Foo[][][].class}),
   * and {@link Class#getComponentType()} only goes up 1 level (e.g. {@code Foo[][].class.getComponentType() == Foo[].class})
   * This method unwraps that chain down to the top-most component.
   * <p>
   * <b>Example</b>: will return {@code Foo.class} given any of the following args:
   * <ul>
   *   <li>{@code Foo.class}
   *   <li>{@code Foo[].class}
   *   <li>{@code Foo[][].class}
   *   <li>{@code Foo[][][].class}
   * </li>
   * @param cls an array type
   * @return the basic component type of the given class, or the class itself if it's not an array
   */
  public static Class getRootComponentTypeOfArray(Class cls) {
    while (cls.isArray()) {
      cls = cls.getComponentType();
    }
    return cls;
  }

  /**
   * Attempts to find the location of the compiled {@code .class} file for a given class.
   *
   * @param cls the class to look up (should be a reference type, otherwise will return {@code null})
   * @return a reference to the location of the compiled {@code .class} file for the given class.
   * <b>CAUTION:</b> the resource referenced by the returned {@link ResourceLocator} might not actually exist or might
   * not be compatible with file-system operations (e.g. if it's contained within a JAR).
   * Will return {@code null} if the class is primitive, {@code void}, or its {@code .class} file is not a resource
   * accessible via {@link Class#getResource(String)}
   */
  public static ResourceLocator getClassFile(Class cls) {
    // There are five kinds of classes (or interfaces):
    // a) Top level classes
    // b) Nested classes (static member classes)
    // c) Inner classes (non-static member classes)
    // d) Local classes (named classes declared within a method)
    // e) Anonymous classes

    // the class could also be an array, in which case we're only interested in its top-most component
    cls = getRootComponentTypeOfArray(cls);
    // now we take the binary name of the class (see JLS §13.1: https://docs.oracle.com/javase/specs/jls/se8/html/jls-13.html#jls-13.1)
    // and convert it to path by replacing all the dots with slashes
    String binaryNamePath = cls.getName().replace('.', '/');
    if (!cls.isPrimitive()) {
      ResourceLocator ret = new ResourceLocator("/" + binaryNamePath + ".class", cls);
      if (ret.exists())
        return ret;
    }
    return null;
  }

  /**
   * Attempts to find the directory where the source ({@code .java} file) of the given class is located, using
   * the URL returned by {@code cls.getResource("")}.
   *
   * <p style="font-style: italic;">
   *   NOTE: not sure why this works, but {@link Class#getResource(String)} (with an empty string arg) seems to return
   *   a URL for the directory where the java source code for the given class is located.  However, this might not work
   *   for inner/anonymous classes, and will almost-certainly not work for system classes or classes loaded from JARs,
   *   therefore <strong>use caution</strong> when calling this method.
   * </p>
   *
   * @param cls the class to look up
   * @return a {@link ResourceLocator} <em>potentially</em> pointing to the directory where the source code ({@code .java} file)
   * of the given class is located; <strong>WARNING:</strong> the result could be totally wrong, and calling
   * {@link ResourceLocator#toPath()} on it could throw an exception)
   *
   */
  public static ResourceLocator getSourceDir(Class cls) {
    // the class could also be an array, in which case we're only interested in its top-most component
    cls = getRootComponentTypeOfArray(cls);
    return new ResourceLocator("", cls);
    /*
    TODO: might make more sense to instead return the Path obtained from ResourceLocator#toPath(), or null if it throws an exception
     */
  }

  /**
   * Attempts to find the root of the directory tree where the source code ({@code .java} file) of the given class is
   * located, using the result of {@link #getSourceDir(Class)}
   *
   * <p style="font-style: italic;">
   *   WARNING: should exercise the same precautions described in {@link #getSourceDir(Class)}.
   * </p>
   *
   * @param cls the class to look up
   * @return a {@link ResourceLocator} <em>potentially</em> pointing to the directory where the source code ({@code .java} file)
   * of the given class is located; <strong>WARNING:</strong> the result could be totally wrong,
   * and caller might want to at least check whether the returned path exists before using it
   * @see #getSourceDir(Class)
   * @see Files#exists(Path, LinkOption...)
   */
  public static Path getSourceRoot(Class cls) {
    ResourceLocator sourceDirResourceLocator = getSourceDir(cls);
    Path path = sourceDirResourceLocator.toPath();
    int pkgDepth = StringUtils.count(cls.getPackage().getName(), '.');
    // walk up the file tree by the number of levels equal to the number of dots in the package name
    Path sourceRoot = path;
    for (int i = pkgDepth; i >= 0; i--) {
      sourceRoot = sourceRoot.getParent();
    }
    return sourceRoot;
  }

  /**
   * Attempts to find the {@code .java} file containing the source code of the given class,
   * using the result of {@link #getSourceDir(Class)}
   *
   * <p style="font-style: italic;">
   *   WARNING: should exercise the same precautions described in {@link #getSourceDir(Class)}.
   * </p>
   *
   * @param cls the class to look up
   * @return a {@link ResourceLocator} <em>potentially</em> pointing to the directory where the source code ({@code .java} file)
   * of the given class is located; <strong>WARNING:</strong> the result could be totally wrong,
   * and caller might want to at least check whether the returned path exists before using it.
   * @see #getSourceDir(Class)
   */
  public static Path getSourceFile(Class cls) throws InvalidPathException {
    ResourceLocator sourceDirResourceLocator = getSourceDir(cls);
    ClassNameParser classNameInfo = new ClassNameParser(cls);
    Path srcDir = sourceDirResourceLocator.toPath();
    return srcDir.resolve(classNameInfo.getComplexName() + ".java");
  }

  /**
   * Prints the values of the public fields and getter methods in the given object.
   * @param out will print to this stream
   * @param obj the instance to print
   * @param cls the class or interface to use for accessing the object.
   * This is useful when the class of the object is not public.  For example,
   * {@link java.lang.management.ManagementFactory#getRuntimeMXBean()} returns an instance of
   * {@link sun.management.RuntimeImpl}, which is not public, but implements the public interface
   * {@link java.lang.management.RuntimeMXBean}.
   */
  public static void printBean(PrintStream out, Object obj, Class cls) {
    Field[] publicFields = cls.getFields();
    out.println(obj);
    for (Field field : publicFields) {
      out.printf("  %s: %s%n", toString(field), getFieldOrMethodValue(field, obj));
    }
    List<Method> publicGetters = listPublicGetters(cls);
    for (Method method : publicGetters) {
      out.printf("  %s: %s%n", toString(method), getFieldOrMethodValue(method, obj));
    }
  }

  private static Object getFieldOrMethodValue(Member member, Object obj) {
    try {
      if (member instanceof Field) {
        return ((Field)member).get(obj);
      }
      else {
        assert member instanceof Method;
        return ((Method)member).invoke(obj);
      }
    }
    catch (ReflectiveOperationException e) {
      e.printStackTrace();
      return e;
    }
  }

  /**
   * Determines the Java spec version of the current JVM by parsing the value of the {@code "java.specification.version"}
   * system property.
   * This could be used for feature discovery, for example:
   * <pre>{@code
   *   if (getJavaSpecVersion().greaterThanOrEqualTo(new VersionNumber(1, 7))) {
   *     // can use diamond operator, try-with-resources, etc.
   *   }
   * }</pre>
   * NOTE: Java also provides the {@link SourceVersion} class for this purpose, for example:
   * <pre>{@code
   *   if (SourceVersion.latestSupported().ordinal() >= SourceVersion.RELEASE_7.ordinal()) {
   *    // can use diamond operator, try-with-resources, etc.
   *  }
   * }</pre>
   *
   * @return the JRE Spec Version of the current java process, e.g. {@code "1.8"}
   *
   * @see SourceVersion#getLatestSupported()
   * @see Package#getSpecificationVersion()
   * @see System#getProperties()
   */
  public static VersionNumber getJavaSpecVersion() {
    // NOTE: another way of getting this info is something like String.class.getPackage().getSpecificationVersion()
    //  return VersionNumber.parse(String.class.getPackage().getSpecificationVersion());
    // but we use

    return VersionNumber.parse(System.getProperty("java.specification.version"));
  }
}
