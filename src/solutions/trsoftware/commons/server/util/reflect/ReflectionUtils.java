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

import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.callables.Function0;
import solutions.trsoftware.commons.shared.util.callables.FunctionN;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author Alex, 1/5/14
 */
public abstract class ReflectionUtils {

  // NOTE: the following 2 methods borrows from java.beans.ReflectionUtils, which unfortunately is not public

  /**
   * @return true iff the given type is one of the wrapper classes for a primitive.
   */
  public static boolean isPrimitiveWrapper(Class type) {
    return primitiveTypeFor(type) != null;
  }

  /**
   * @return the primitive type corresponding to the given wrapper class.
   */
  public static Class primitiveTypeFor(Class wrapper) {
    if (wrapper == Boolean.class) return Boolean.TYPE;
    if (wrapper == Byte.class) return Byte.TYPE;
    if (wrapper == Character.class) return Character.TYPE;
    if (wrapper == Short.class) return Short.TYPE;
    if (wrapper == Integer.class) return Integer.TYPE;
    if (wrapper == Long.class) return Long.TYPE;
    if (wrapper == Float.class) return Float.TYPE;
    if (wrapper == Double.class) return Double.TYPE;
    if (wrapper == Void.class) return Void.TYPE;
    return null;
  }

  /**
   * Creates a factory for the given class using the given constructor args, which wraps all checked reflection
   * exceptions with an IllegalArgumentException.
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
   * Creates a factory for the given class using its default (0-arg) constructor, which wraps all checked reflection
   * exceptions with an IllegalArgumentException.
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
  public static File getCompilerOutputDir(Class refClass) {
    // TODO: unit test this
    // get the directory containing this .class file
    final File thisClassFile = ServerIOUtils.getClassFile(refClass);
    final File thisClassDir = thisClassFile.getParentFile();
    // go up to the root dir of the compiler output
    // (by going up the directory tree the same number of steps as the number of packages above this class)
    // we determine the number of steps to go up by counting the number of dots in the package name
    // example: package "foo.bar" contains 1 dot, so we want to go up 2 steps
    int nSteps = StringUtils.count(refClass.getPackage().getName(), '.') + 1;
    File rootDir = thisClassDir;
    for (int i = 0; i < nSteps; i++) {
      rootDir = rootDir.getParentFile();
    }
    return rootDir;
  }

}
