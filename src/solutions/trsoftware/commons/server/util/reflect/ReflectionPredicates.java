/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.server.util.reflect;

import com.google.common.base.MoreObjects;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

/**
 * Utility methods for generating reflection-based predicates.
 * @see Predicate
 *
 * @author Alex
 * @since 5/3/2018
 */
public class ReflectionPredicates {

  /**
   * @return a predicate that evaluates to {@code true} iff its argument has a
   * {@linkplain AnnotatedElement#getDeclaredAnnotation(Class) declared} annotation of the given type.
   *
   * @see ReflectionUtils#hasDeclaredAnnotation(Class, AnnotatedElement)
   */
  public static Predicate<AnnotatedElement> hasDeclaredAnnotation(Class<? extends Annotation> ann) {
    return annotatedElement -> annotatedElement.getDeclaredAnnotation(ann) != null;
  }

  /**
   * @param superClass a class or interface that the object being evaluated should extend to satisfy the returned
   * predicate
   * @return a predicate that evaluates to {@code true} iff its argument is or extends {@code superClass}
   * (this is similar to the generic type expression {@code <T extends superClass>})
   * @see Class#isAssignableFrom(Class)
   */
  public static Predicate<Class<?>> isSubclassOf(Class<?> superClass) {
    return superClass::isAssignableFrom;
  }

  /**
   * Predicates testing the modifiers of a {@link Class} or {@link Member}.
   *
   * @see Class#getModifiers()
   * @see Member#getModifiers()
   */
  public abstract static class Modifiers {

    /*
    ================================================================================
    Class modifiers (see java.lang.reflect.Modifier.CLASS_MODIFIERS):
    ================================================================================
    */

    /**
     * @return {@code true} if {@code arg} has the {@code public} modifier.
     */
    public static boolean isPublic(Class<?> arg) { return Modifier.isPublic(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code private} modifier.
     */
    public static boolean isPrivate(Class<?> arg) { return Modifier.isPrivate(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code protected} modifier.
     */
    public static boolean isProtected(Class<?> arg) { return Modifier.isProtected(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code static} modifier.
     */
    public static boolean isStatic(Class<?> arg) { return Modifier.isStatic(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code final} modifier.
     */
    public static boolean isFinal(Class<?> arg) { return Modifier.isFinal(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code interface} modifier.
     */
    public static boolean isInterface(Class<?> arg) { return Modifier.isInterface(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code abstract} modifier.
     */
    public static boolean isAbstract(Class<?> arg) { return Modifier.isAbstract(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code strictfp} modifier.
     */
    public static boolean isStrict(Class<?> arg) { return Modifier.isStrict(arg.getModifiers()); }


    /*
    =======================================================================================
    Member modifiers (see java.lang.reflect.Modifier.[FIELD|METHOD|CONSTRUCTOR]_MODIFIERS):
    =======================================================================================
    */

    /**
     * @return {@code true} if {@code arg} has the {@code public} modifier.
     */
    public static boolean isPublic(Member arg) { return Modifier.isPublic(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code private} modifier.
     */
    public static boolean isPrivate(Member arg) { return Modifier.isPrivate(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code protected} modifier.
     */
    public static boolean isProtected(Member arg) { return Modifier.isProtected(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code static} modifier.
     */
    public static boolean isStatic(Member arg) { return Modifier.isStatic(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code final} modifier.
     */
    public static boolean isFinal(Member arg) { return Modifier.isFinal(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code synchronized} modifier.
     */
    public static boolean isSynchronized(Member arg) { return Modifier.isSynchronized(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code volatile} modifier.
     */
    public static boolean isVolatile(Member arg) { return Modifier.isVolatile(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code transient} modifier.
     */
    public static boolean isTransient(Member arg) { return Modifier.isTransient(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code native} modifier.
     */
    public static boolean isNative(Member arg) { return Modifier.isNative(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code abstract} modifier.
     */
    public static boolean isAbstract(Member arg) { return Modifier.isAbstract(arg.getModifiers()); }

    /**
     * @return {@code true} if {@code arg} has the {@code strictfp} modifier.
     */
    public static boolean isStrict(Member arg) { return Modifier.isStrict(arg.getModifiers()); }

  }

}
