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

package solutions.trsoftware.junit;

import com.google.common.collect.Lists;
import com.google.gwt.junit.client.GWTTestCase;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.util.WebUtilsGwtTest;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.util.MapDecorator;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.template.SimpleTemplateParserGwtTest;
import solutions.trsoftware.commons.shared.util.template.SimpleTemplateParserJavaTest;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormatGwtTest;
import solutions.trsoftware.commons.shared.validation.EmailAddressValidatorGwtTest;
import solutions.trsoftware.commons.shared.validation.RegexValidationRuleGwtTest;
import solutions.trsoftware.commons.shared.validation.RegexValidationRuleGwtTestCase;
import solutions.trsoftware.commons.shared.validation.ValidationRuleGwtTestCase;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static solutions.trsoftware.commons.server.testutil.ServerAssertUtils.assertFunctionResults;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.junit.TestSuiteBuilder.FilterMode;
import static solutions.trsoftware.junit.TestSuiteBuilder.iterTests;

/**
 * @author Alex
 * @since 4/30/2018
 */
public class TestSuiteBuilderTest extends TestCase {


  public void testIterTests() throws Exception {
    ArrayList<Test> tests = Lists.newArrayList(iterTests(
        new TestSuite(Test1.class),
        new TestSuite(Test2.class),
        new TestSuite(Test3.class)));
    assertEquals(12, tests.size());
  }

  /**
   * Tests an instance with neither annotation filters nor superclass filters
   */
  public void testBuildSuite() throws Exception {
    TestSuiteBuilder builder = getTestSuiteBuilder();
    TestSuite suite = buildSuite(builder);
    assertTestSuiteEquals(suite
        , new TestSuiteSpec(Test1.class, "testA1", "testB1", "testC1", "testD1")
        , new TestSuiteSpec(Test2.class, "testA2", "testB2", "testC2", "testD2")
        , new TestSuiteSpec(Test3.class, "testA3", "testB3", "testC3", "testD3")
    );
  }

  /**
   * Tests an instance configured using 
   * {@link TestSuiteBuilder#includeOnlyTestsAnnotatedWith} and 
   * {@link TestSuiteBuilder#excludeTestsAnnotatedWith}
   */
  public void testBuildSuiteWithAnnotationFilters() throws Exception {
    {
      TestSuite suite = buildSuite(getTestSuiteBuilder()
          .includeOnlyTestsAnnotatedWith(Ann4.class)
      );
      // there are no tests annotated with @Ann4
      assertEquals(0, suite.testCount());
    }
    {
      TestSuite suite = buildSuite(getTestSuiteBuilder()
          .includeOnlyTestsAnnotatedWith(Ann1.class)
      );
      assertTestSuiteEquals(suite
          // all test methods in Test1 should be included because the class itself has @Ann1
          , new TestSuiteSpec(Test1.class, "testA1", "testB1", "testC1", "testD1")
          // for the rest, only methods that actually have @Ann1 should be included
          , new TestSuiteSpec(Test2.class, "testA2")
          , new TestSuiteSpec(Test3.class, "testA3", "testD3")
      );
    }
    {
      // now add an exclusion
      TestSuite suite = buildSuite(getTestSuiteBuilder()
          .includeOnlyTestsAnnotatedWith(Ann1.class)
          .excludeTestsAnnotatedWith(Ann2.class)
      );
      assertTestSuiteEquals(suite
          // all test methods in Test1 should be included because the class itself has @Ann1
          , new TestSuiteSpec(Test1.class, "testA1", "testC1")
          // Test2 is now excluded completely since the class itself has @Ann2
          // and nothing changed for Test3, because @Ann2 is not present in any of its method
          , new TestSuiteSpec(Test3.class, "testA3", "testD3")
      );
    }
  }

  /**
   * Tests an instance configured using 
   * {@link TestSuiteBuilder#includeOnlySubclassesOf} and
   * {@link TestSuiteBuilder#excludeSubclassesOf}
   */
  public void testBuildSuiteWithSuperclassFilters() throws Exception {
    {
      TestSuite suite = buildSuite(getTestSuiteBuilder()
          .includeOnlySubclassesOf(GWTTestCase.class)
      );
      // there are no subclasses of GWTTestCase here
      assertEquals(0, suite.testCount());
    }
    {
      TestSuite suite = buildSuite(getTestSuiteBuilder()
          .includeOnlySubclassesOf(BaseTestCaseA.class)
      );
      assertTestSuiteEquals(suite
          , new TestSuiteSpec(Test1.class, "testA1", "testB1", "testC1", "testD1")
          , new TestSuiteSpec(Test2.class, "testA2", "testB2", "testC2", "testD2")
      );
    }
    {
      TestSuite suite = buildSuite(getTestSuiteBuilder()
          .includeOnlySubclassesOf(BaseTestCaseA.class)
          .excludeSubclassesOf(Test2.class)
      );
      assertTestSuiteEquals(suite
          , new TestSuiteSpec(Test1.class, "testA1", "testB1", "testC1", "testD1")
      );
    }
  }

  /**
   * Tests an instance configured using
   * {@link TestSuiteBuilder#includeOnlySubclassesOf}, {@link TestSuiteBuilder#excludeSubclassesOf},
   * {@link TestSuiteBuilder#includeOnlyTestsAnnotatedWith}, and {@link TestSuiteBuilder#excludeTestsAnnotatedWith}
   */
  public void testBuildSuiteWithSuperclassFiltersAndAnnotationFilters() throws Exception {
    {
      TestSuite suite = buildSuite(getTestSuiteBuilder()
          .includeOnlySubclassesOf(BaseTestCaseA.class)
          .includeOnlyTestsAnnotatedWith(Ann1.class)
      );
      assertTestSuiteEquals(suite
          // all test methods in Test1 should be included because the class itself has @Ann1
          , new TestSuiteSpec(Test1.class, "testA1", "testB1", "testC1", "testD1")
          // for Test2, only methods that actually have @Ann1 should be included
          , new TestSuiteSpec(Test2.class, "testA2")
      );
    }
    {
      TestSuite suite = buildSuite(getTestSuiteBuilder()
          .includeOnlySubclassesOf(BaseTestCaseA.class)
          .excludeTestsAnnotatedWith(Ann2.class)
      );
      assertTestSuiteEquals(suite
          , new TestSuiteSpec(Test1.class, "testA1", "testC1")
      );
    }
  }
  

  private TestSuite buildSuite(TestSuiteBuilder builder) throws IOException {
    System.out.println("builder = " + builder);
    TestSuite suite = builder.buildSuite();
    TestSuiteBuilder.printTestSuite(suite);
    assertEquals(getClass().getName(), suite.getName());
    return suite;
  }

  public void testFilterMode() throws Exception {
    assertEquals(FilterMode.EXCLUDE, FilterMode.INCLUDE.opposite());
    assertEquals(FilterMode.INCLUDE, FilterMode.EXCLUDE.opposite());
  }

  public void testApplySuperclassFilters() throws Exception {
    TestSuiteBuilder builder = new TestSuiteBuilder()
        .includeOnlySubclassesOf(CommonsGwtTestCase.class);
    MapDecorator<Class<?>, Boolean> expected = MapUtils.<Class<?>, Boolean>linkedHashMapBuilder()
        .put(SimpleTemplateParserGwtTest.class, true)
        .put(SimpleTemplateParserJavaTest.class, false)
        .put(SharedNumberFormatGwtTest.class, true)
        .put(WebUtilsGwtTest.class, true)
        .put(ValidationRuleGwtTestCase.class, true)
        .put(RegexValidationRuleGwtTestCase.class, true)
        .put(RegexValidationRuleGwtTest.class, true)
        .put(EmailAddressValidatorGwtTest.class, true)
        .put(GWTTestCase.class, false)
        .put(TestCase.class, false)
        .put(TestSuiteBuilderTest.class, false)
        ;
    assertFunctionResults("builder.applySuperclassFilters", builder::applySuperclassFilters, expected.getMap());
    // now add some exclusions
    builder.excludeSubclassesOf(RegexValidationRuleGwtTestCase.class, SharedNumberFormatGwtTest.class);
    assertFunctionResults("builder.applySuperclassFilters", builder::applySuperclassFilters,
        expected
            .put(SharedNumberFormatGwtTest.class, false)
            .put(RegexValidationRuleGwtTestCase.class, false)
            .put(RegexValidationRuleGwtTest.class, false)
            .put(EmailAddressValidatorGwtTest.class, false)
            .getMap()
    );
    // now try adding another inclusion for a superclass that was already excluded
    assertThrows(IllegalStateException.class,
        (Runnable)() -> builder.includeOnlySubclassesOf(SharedNumberFormatGwtTest.class));
    // now do the same for the opposite case
    assertThrows(IllegalStateException.class,
        (Runnable)() -> builder.excludeSubclassesOf(CommonsGwtTestCase.class));
  }

  public void testApplyAnnotationFilters() throws Exception {
    TestSuiteBuilder builder = new TestSuiteBuilder()
        .includeOnlyTestsAnnotatedWith(Ann1.class);
    MapDecorator<AnnotatedElement, Boolean> expected = MapUtils.<AnnotatedElement, Boolean>linkedHashMapBuilder()
        .put(Test1.class, true)
        .put(Test1.class.getMethod("testA1"), true)
        .put(Test1.class.getMethod("testB1"), true) // included because containing class has @Ann1 (even though the method itself does not)
        .put(Test1.class.getMethod("testC1"), true) // included because containing class has @Ann1 (even though the method itself does not)
        .put(Test1.class.getMethod("testD1"), true)
        .put(Test2.class, true)  // included because only exclusion filters are checked for classes
        .put(Test2.class.getMethod("testA2"), true)
        .put(Test2.class.getMethod("testB2"), false)
        .put(Test2.class.getMethod("testC2"), false)
        .put(Test2.class.getMethod("testD2"), false)
        .put(Test3.class, true)  // included because only exclusion filters are checked for classes
        .put(Test3.class.getMethod("testA3"), true)
        .put(Test3.class.getMethod("testB3"), false)
        .put(Test3.class.getMethod("testC3"), false)
        .put(Test3.class.getMethod("testD3"), true)
        ;
    assertFunctionResults("builder.applyAnnotationFilters", builder::applyAnnotationFilters, expected.getMap());
    // now add some exclusions
    builder.excludeTestsAnnotatedWith(Ann2.class, Ann3.class);
    assertFunctionResults("builder.applyAnnotationFilters", builder::applyAnnotationFilters,
        expected
            // these are all now excluded
            .put(Test1.class.getMethod("testB1"), false)
            .put(Test1.class.getMethod("testC1"), false)
            .put(Test1.class.getMethod("testD1"), false) // excluded because has @Ann2 (even though also has @Ann1)
            .put(Test2.class, false)
            .put(Test2.class.getMethod("testA2"), false) // excluded because containing class has @Ann2
            .put(Test3.class.getMethod("testD3"), false) // excluded because has @Ann3 (even though also has @Ann1)
            .getMap()
    );
    // now try adding another inclusion for an annotation that was already excluded
    assertThrows(IllegalStateException.class,
        (Runnable)() -> builder.includeOnlyTestsAnnotatedWith(Ann2.class));
    assertThrows(IllegalStateException.class,
        (Runnable)() -> builder.includeOnlyTestsAnnotatedWith(Ann3.class));
    // now do the same for the opposite case
    assertThrows(IllegalStateException.class,
        (Runnable)() -> builder.excludeTestsAnnotatedWith(Ann1.class));
  }

  /**
   * Spec for a suite that would be created via {@link TestSuite#TestSuite(Class)}
   */
  static class TestSuiteSpec {
    private Class<? extends TestCase> testCaseClass;
    private List<String> testMethodNames;

    public TestSuiteSpec(Class<? extends TestCase> testCaseClass, List<String> testMethodNames) {
      this.testCaseClass = testCaseClass;
      this.testMethodNames = testMethodNames;
    }

    public TestSuiteSpec(Class<? extends TestCase> testCaseClass, String... testMethodNames) {
      this(testCaseClass, Arrays.asList(testMethodNames));
    }

    public void assertEqualTo(TestSuite suite) {
      String errMsg = String.format("Suite %s does not match %s", suite, this);
      assertEquals(errMsg, testCaseClass.getName(), suite.getName());
      @SuppressWarnings("unchecked")
      ArrayList<Test> tests = Collections.list(suite.tests());
      assertEquals(errMsg, testMethodNames.size(), tests.size());
      for (int i = 0; i < tests.size(); i++) {
        Test test = tests.get(i);
        assertEquals(errMsg, testCaseClass, test.getClass());
        assertEquals(errMsg, testMethodNames.get(i), testCaseClass.cast(test).getName());
      }
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("TestSuiteSpec{");
      sb.append("class=").append(testCaseClass.getSimpleName());
      sb.append(", methods=").append(testMethodNames);
      sb.append('}');
      return sb.toString();
    }
  }

  /**
   * Asserts thet the given suite contains the test cases that would be generated by the given specs.
   */
  static void assertTestSuiteEquals(TestSuite suite, TestSuiteSpec... expected) {
    @SuppressWarnings("unchecked")
    ArrayList<Test> tests = Collections.list(suite.tests());
    assertEquals(expected.length, tests.size());
    for (int i = 0; i < expected.length; i++) {
      expected[i].assertEqualTo((TestSuite)tests.get(i));
    }
  }


  /**
   * @return a new instance of {@link TestSuiteBuilder} that will build a suite for the nested test cases under
   * this class.
   */
  private static TestSuiteBuilder getTestSuiteBuilder() {
    return new TestSuiteBuilder()
        // pattern matching the filenames of the nested class files
        .setFilenameRegex(".*TestSuiteBuilderTest\\$Test\\d\\.class")
        .addContentRoot(ReflectionUtils.getClassFile(TestSuiteBuilderTest.class).toFile().toPath().getParent());
  }

  /*
  --------------------------------------------------------------------------------
  Dummy test cases:
  --------------------------------------------------------------------------------
  */

  private static abstract class BaseTestCaseA extends TestCase {}
  private static abstract class BaseTestCaseB extends TestCase {}

  // the following test classes must be public and static (so that the TestSuite constructor can recognize them as proper tests)

  @Ann1
  public static class Test1 extends BaseTestCaseA {
    public Test1() {
    }
    @Ann1
    public void testA1() throws Exception {
    }
    @Ann2
    public void testB1() throws Exception {
    }
    @Ann3
    public void testC1() throws Exception {
    }
    @Ann1 @Ann2
    public void testD1() throws Exception {
    }
  }

  @Ann2
  public static class Test2 extends BaseTestCaseA {
    public Test2() {
    }
    @Ann1
    public void testA2() throws Exception {
    }
    @Ann2
    public void testB2() throws Exception {
    }
    public void testC2() throws Exception {
    }
    public void testD2() throws Exception {
    }
  }

  public static class Test3 extends BaseTestCaseB {
    public Test3() {
    }
    @Ann1
    public void testA3() throws Exception {
    }
    public void testB3() throws Exception {
    }
    @Ann3
    public void testC3() throws Exception {
    }
    @Ann1 @Ann3
    public void testD3() throws Exception {
    }
  }

  /*
  --------------------------------------------------------------------------------
  Annotations
  --------------------------------------------------------------------------------
  */

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  private @interface Ann1 {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  private @interface Ann2 {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  private @interface Ann3 {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  private @interface Ann4 {}
}