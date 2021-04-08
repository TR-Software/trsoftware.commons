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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
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
   * @return a predicate that evaluates to {@code true} iff its argument declares an annotation of the given type.
   */
  public static Predicate<AnnotatedElement> mustHaveDeclaredAnnotation(Class<? extends Annotation> ann) {
    return annotatedElement -> annotatedElement.getDeclaredAnnotation(ann) != null;
  }

  /**
   * @return a predicate that evaluates to {@code true} iff its argument is or extends {@code superClass}
   * (this is similar to the generic type expression {@code <T extends superClass>})
   */
  public static Predicate<Class<?>> mustBeSubclassOf(Class<?> superClass) {
    return superClass::isAssignableFrom;
  }
}
