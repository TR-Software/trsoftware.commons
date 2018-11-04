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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static solutions.trsoftware.commons.server.util.reflect.ReflectionPredicates.mustBeSubclassOf;
import static solutions.trsoftware.commons.server.util.reflect.ReflectionPredicates.mustHaveDeclaredAnnotation;

/**
 * @author Alex
 * @since 5/3/2018
 */
public class ReflectionPredicatesTest extends TestCase {

  public void testMustHaveDeclaredAnnotation() throws Exception {
    Predicate<AnnotatedElement> mustHaveAnn1 = mustHaveDeclaredAnnotation(Ann1.class);
    assertTrue(mustHaveAnn1.test(ClassWithAnnotatedMethods.class.getMethod("hasAnn1")));
    assertTrue(mustHaveAnn1.test(ClassWithAnnotatedMethods.class.getMethod("hasAnn1Ann2")));
    assertTrue(mustHaveAnn1.test(ClassHasAnn1.class));
    assertTrue(mustHaveAnn1.test(ClassHasAnn1Ann2.class));
    
    assertFalse(mustHaveAnn1.test(ClassHasAnn2.class));
    assertFalse(mustHaveAnn1.test(ReflectionPredicatesTest.class));
    assertFalse(mustHaveAnn1.test(ClassWithAnnotatedMethods.class.getMethod("hasAnn2")));
    for (Method method : ClassWithoutAnnotatedMethods.class.getDeclaredMethods()) {
      assertFalse(mustHaveAnn1.test(method));
    }
  }

  public void testMustBeSubclassOf() throws Exception {
    assertTrue(mustBeSubclassOf(Map.class).test(Map.class));
    assertTrue(mustBeSubclassOf(HashMap.class).test(HashMap.class));
    assertTrue(mustBeSubclassOf(Map.class).test(HashMap.class));

    assertFalse(mustBeSubclassOf(HashMap.class).test(Map.class));
  }


  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  private @interface Ann1 {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  private @interface Ann2 {}

  private static class ClassWithAnnotatedMethods {
    @Ann1
    public void hasAnn1() {};
    @Ann2
    public void hasAnn2() {};
    @Ann1 @Ann2
    public void hasAnn1Ann2() {};
  }
  
  private static class ClassWithoutAnnotatedMethods extends ClassWithAnnotatedMethods {
    @Override
    public void hasAnn1() {
      super.hasAnn1();
    }

    @Override
    public void hasAnn2() {
      super.hasAnn2();
    }

    @Override
    public void hasAnn1Ann2() {
      super.hasAnn1Ann2();
    }
  }
  
  
  @Ann1
  private static class ClassHasAnn1 {}

  @Ann2
  private static class ClassHasAnn2 {}

  @Ann1 @Ann2
  private static class ClassHasAnn1Ann2 {}


}