package solutions.trsoftware.commons.server.servlet.config;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.servlet.testutil.DummyFilterConfig;
import solutions.trsoftware.commons.server.servlet.testutil.DummyServletConfig;
import solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext;
import solutions.trsoftware.commons.server.servlet.testutil.DummyWebConfigObject;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.NumberRange;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;

import java.util.*;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertThat;
import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 1/2/2018
 */
public class WebConfigParserTest extends TestCase {

  private WebConfigParser parser;
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
    parser = new WebConfigParser();
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
        BasicInitParams params = parser.parse(config, new BasicInitParams());
        System.out.println("Parse result: " + params);
        assertEquals(123L, params.foo);
        assertEquals(true, params.bar);
        assertEquals("something", params.str);
      }
      // now try it with some missing params
      Map<String, String> newParamMap = replaceParams(config);
      {
        newParamMap.remove("foo");
        BasicInitParams params = parser.parse(config, new BasicInitParams());
        System.out.println("Parse result: " + params);
        assertEquals(0L, params.foo);
        assertEquals(true, params.bar);
        assertEquals("something", params.str);
      }
      {
        newParamMap.remove("bar");
        BasicInitParams params = parser.parse(config, new BasicInitParams());
        System.out.println("Parse result: " + params);
        assertEquals(0L, params.foo);
        assertEquals(false, params.bar);
        assertEquals("something", params.str);
      }
      newParamMap.remove("str");  // this is the only required parameter
      assertThrows(new RequiredInitParameterMissing("str", String.class, config),
          (Function0_t<Throwable>)() -> parser.parse(config, new BasicInitParams()));
    }
  }

  public void testParserWithUserDefinedFields() throws Exception {
    for (DummyWebConfigObject config : configObjects) {
      // 1) check that parsing a config with a required parameter missing throws an exception
      assertThrows(new RequiredInitParameterMissing("numRange", NumberRange.class, config),
          (Function0_t<Throwable>)() -> parser.parse(config, new ComplexInitParams()));
      // 2) now provide a value for that missing param and check that it parses correctly
      Map<String, String> newParamMap = replaceParams(config);
      newParamMap.put("numRange", "5..10");
      ComplexInitParams params = parser.parse(config, new ComplexInitParams());
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
          (Function0_t<WebConfigException>)() -> parser.parse(config, new InitParamsWithMissingAnnotation()));
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
          (Function0_t<WebConfigException>)() -> parser.parse(config, new InitParamsWithIncompleteAnnotation()));
      assertThat(ex.getMessage()).startsWith(
          InitParamsWithIncompleteAnnotation.class.getSimpleName() + ".numberRange must contain a @Param annotation specifying a ParameterParser<NumberRange> subclass");
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
    public NumberRange<Integer> parse(String value) {
      return NumberRange.fromStringIntRange(value);
    }
  }
}