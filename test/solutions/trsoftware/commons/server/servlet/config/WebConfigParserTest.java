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

package solutions.trsoftware.commons.server.servlet.config;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.servlet.testutil.DummyFilterConfig;
import solutions.trsoftware.commons.server.servlet.testutil.DummyServletConfig;
import solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext;
import solutions.trsoftware.commons.server.servlet.testutil.DummyWebConfigObject;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.NumberRange;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;

import java.lang.reflect.Field;
import java.util.*;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThat;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 1/2/2018
 */
public class WebConfigParserTest extends TestCase {

  private Map<String, String> paramMap;
  private List<DummyWebConfigObject> configObjects;

  public void setUp() throws Exception {
    super.setUp();
    paramMap = Collections.unmodifiableMap(MapUtils.stringLinkedHashMap(
        "foo", "123",
        "bar", "true",
        "str", "something"
    ));
    configObjects = Arrays.asList(
        new DummyFilterConfig(paramMap),
        new DummyServletConfig(paramMap),
        new DummyServletContext(paramMap)
    );
  }

  /**
   * Invokes {@link DummyWebConfigObject#setInitParameterMap(Map)} with a new copy of {@link #paramMap},
   * so that parameter values can be modified without affecting other objects in {@link #configObjects}.
   *
   * @param configObject the object to modify
   * @return the new instance of the param mapping.
   */
  private Map<String, String> replaceParams(DummyWebConfigObject configObject) {
    Map<String, String> newParamMap = new LinkedHashMap<>(paramMap);
    configObject.setInitParameterMap(newParamMap);
    return newParamMap;
  }

  public void testParserWithBasicFieldsOnly() throws Exception {
    for (DummyWebConfigObject config : configObjects) {
      {
        BasicInitParams params = WebConfigParser.parse(config, new BasicInitParams());
        System.out.println("Parse result: " + params);
        assertEquals(123L, params.foo);
        assertEquals(true, params.bar);
        assertEquals("something", params.str);
      }
      // now try it with some missing params
      Map<String, String> newParamMap = replaceParams(config);
      {
        newParamMap.remove("foo");
        BasicInitParams params = WebConfigParser.parse(config, new BasicInitParams());
        System.out.println("Parse result: " + params);
        assertEquals(0L, params.foo);
        assertEquals(true, params.bar);
        assertEquals("something", params.str);
      }
      {
        newParamMap.remove("bar");
        BasicInitParams params = WebConfigParser.parse(config, new BasicInitParams());
        System.out.println("Parse result: " + params);
        assertEquals(0L, params.foo);
        assertEquals(false, params.bar);
        assertEquals("something", params.str);
      }
      newParamMap.remove("str");  // this is the only required parameter
      assertThrows(new RequiredInitParameterMissing("str", String.class, config),
          (Function0_t<Throwable>)() -> WebConfigParser.parse(config, new BasicInitParams()));
    }
  }

  public void testParserWithUserDefinedFields() throws Exception {
    for (DummyWebConfigObject config : configObjects) {
      // 1) check that parsing a config with a required parameter missing throws an exception
      assertThrows(new RequiredInitParameterMissing("numRange", NumberRange.class, config),
          (Function0_t<Throwable>)() -> WebConfigParser.parse(config, new ComplexInitParams()));
      // 2) now provide a value for that missing param and check that it parses correctly
      Map<String, String> newParamMap = replaceParams(config);
      newParamMap.put("numRange", "5..10");
      ComplexInitParams params = WebConfigParser.parse(config, new ComplexInitParams());
      System.out.println("Parse result: " + params);
      assertEquals(123L, params.foo);
      assertEquals(true, params.bar);
      assertEquals("something", params.str);
      assertEquals(new NumberRange<>(5, 10), params.numberRange);
    }
  }

  public void testParserWithMissingParamAnnotation() throws Exception {
    for (DummyWebConfigObject config : configObjects) {
      Map<String, String> newParamMap = replaceParams(config);
      newParamMap.put("numberRange", "5..10");
      InitParameterParserNotAvailable ex = assertThrows(
          InitParameterParserNotAvailable.class,
          (Function0_t<WebConfigException>)() -> WebConfigParser.parse(config, new InitParamsWithMissingAnnotation()));
      assertThat(ex.getMessage()).startsWith(
          InitParamsWithMissingAnnotation.class.getSimpleName() + ".numberRange must contain a @Param annotation specifying a ParameterParser<NumberRange> subclass");

    }
  }

  public void testParserWithInadequateParamAnnotation() throws Exception {
    for (DummyWebConfigObject config : configObjects) {
      Map<String, String> newParamMap = replaceParams(config);
      newParamMap.put("numRange", "5..10");
      InitParameterParserNotAvailable ex = assertThrows(
          InitParameterParserNotAvailable.class,
          (Function0_t<WebConfigException>)() -> WebConfigParser.parse(config, new InitParamsWithIncompleteAnnotation()));
      assertThat(ex.getMessage()).startsWith(
          InitParamsWithIncompleteAnnotation.class.getSimpleName() + ".numberRange must contain a @Param annotation specifying a ParameterParser<NumberRange> subclass");
    }
  }

  public void testParserWithClassInstance() throws Exception {
    for (DummyWebConfigObject config : configObjects) {
      Map<String, String> newParamMap = replaceParams(config);
      // 1) try naming a subclass of Set (as expected)
      {
        newParamMap.put("setImpl", HashSet.class.getName());
        // parser should set this field to a new instance of HashSet (based on the given class name)
        InitParamsWithClassInstance params = WebConfigParser.parse(config, new InitParamsWithClassInstance());
        assertNotNull(params.setImpl);
        assertTrue(params.setImpl instanceof HashSet);
      }
      // 2) now try it with a name representing a class of the wrong type
      {
        newParamMap.put("setImpl", getClass().getName());  // this is not a subclass of Set
        InitParameterParseException ex = assertThrows(InitParameterParseException.class,
            (Function0_t<WebConfigException>)() -> WebConfigParser.parse(config, new InitParamsWithClassInstance()));
        Throwable cause = ex.getCause();
        // the cause should be an IllegalArgumentException thrown by Field.set
        // recreate the same exception and check that its message equals what we have
        Field field = InitParamsWithClassInstance.class.getDeclaredField("setImpl");
        field.setAccessible(true);
        try {
          field.set(new InitParamsWithClassInstance(), new WebConfigParserTest());
        }
        catch (IllegalArgumentException e) {
          assertEquals(e.getClass(), cause.getClass());
          assertEquals(e.getMessage(), cause.getMessage());
        }
      }

//      InitParameterParserNotAvailable ex = assertThrows(
//          InitParameterParserNotAvailable.class,
//          (Function0_t<WebConfigException>)() -> parser.parse(config, new InitParamsWithIncompleteAnnotation()));
//      assertThat(ex.getMessage()).startsWith(
//          InitParamsWithIncompleteAnnotation.class.getSimpleName() + ".numberRange must contain a @Param annotation specifying a ParameterParser<NumberRange> subclass");
    }
  }

  private static class BasicInitParams implements InitParameters {
    private long foo;
    private boolean bar;
    @Param(required = true)
    private String str;

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('{');
      sb.append("foo=").append(foo);
      sb.append(", bar=").append(bar);
      sb.append(", str='").append(str).append('\'');
      sb.append('}');
      return sb.toString();
    }
  }

  private class InitParamsWithMissingAnnotation implements InitParameters {
    private long foo;
    private boolean bar;
    private String str;
    private NumberRange<Integer> numberRange;

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('{');
      sb.append("foo=").append(foo);
      sb.append(", bar=").append(bar);
      sb.append(", str='").append(str).append('\'');
      sb.append(", numberRange=").append(numberRange);
      sb.append('}');
      return sb.toString();
    }
  }

  private class InitParamsWithIncompleteAnnotation implements InitParameters {
    private long foo;
    private boolean bar;
    private String str;
    @Param(name = "numRange") // try a custom name, but no parser
    private NumberRange<Integer> numberRange;

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('{');
      sb.append("foo=").append(foo);
      sb.append(", bar=").append(bar);
      sb.append(", str='").append(str).append('\'');
      sb.append(", numberRange=").append(numberRange);
      sb.append('}');
      return sb.toString();
    }
  }

  private class ComplexInitParams implements InitParameters {
    private long foo;
    private boolean bar;
    private String str;
    @Param(name = "numRange", required = true, parser = NumberRangeParser.class) // annotation which specifies all its members
    private NumberRange<Integer> numberRange;

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('{');
      sb.append("foo=").append(foo);
      sb.append(", bar=").append(bar);
      sb.append(", str='").append(str).append('\'');
      sb.append(", numberRange=").append(numberRange);
      sb.append('}');
      return sb.toString();
    }
  }

  static class NumberRangeParser implements InitParameters.ParameterParser<NumberRange<Integer>> {
    @Override
    public NumberRange<Integer> parse(String value) throws Exception {
      return NumberRange.fromStringIntRange(value);
    }
  }

  private class InitParamsWithClassInstance implements InitParameters {
    /** Should be initialized from a class name */
    @Param(parser = ClassNameParameterParser.class)
    private Set setImpl;

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("InitParamsWithClassInstance{");
      sb.append("setImpl=").append(setImpl);
      sb.append('}');
      return sb.toString();
    }
  }
}