package solutions.trsoftware.commons.server.servlet.filters.config;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.servlet.testutil.DummyFilterConfig;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.NumberRange;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;

import java.util.LinkedHashMap;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertThat;
import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 1/2/2018
 */
public class FilterConfigParserTest extends TestCase {

  private LinkedHashMap<String, String> paramMap;
  private DummyFilterConfig filterConfig;
  private FilterConfigParser parser;

  public void setUp() throws Exception {
    super.setUp();
    paramMap = MapUtils.stringLinkedHashMap(
        "foo", "123",
        "bar", "true",
        "str", "something"
    );
    filterConfig = new DummyFilterConfig(paramMap);
    parser = new FilterConfigParser();
  }

  public void testParserWithBasicFieldsOnly() throws Exception {
    {
      BasicInitParams params = parser.parse(filterConfig, new BasicInitParams());
      System.out.println("Parse result: " + parser);
      assertEquals(123L, params.foo);
      assertEquals(true, params.bar);
      assertEquals("something", params.str);
    }
    // now try it with some missing params
    {
      paramMap.remove("foo");
      BasicInitParams params = parser.parse(filterConfig, new BasicInitParams());
      System.out.println("Parse result: " + parser);
      assertEquals(0L, params.foo);
      assertEquals(true, params.bar);
      assertEquals("something", params.str);
    }
    {
      paramMap.remove("bar");
      BasicInitParams params = parser.parse(filterConfig, new BasicInitParams());
      System.out.println("Parse result: " + parser);
      assertEquals(0L, params.foo);
      assertEquals(false, params.bar);
      assertEquals("something", params.str);
    }
    paramMap.remove("str");  // this is the only required parameter
    assertThrows(new RequiredParameterMissing("str", String.class, filterConfig),
        (Function0_t<Throwable>)() -> parser.parse(filterConfig, new BasicInitParams()));
  }

  public void testParserWithUserDefinedFields() throws Exception {
    // 1) check that parsing a config with a required parameter missing throws an exception
    assertThrows(new RequiredParameterMissing("numRange", NumberRange.class, filterConfig),
        (Function0_t<Throwable>)() -> parser.parse(filterConfig, new ComplexInitParams()));
    // 2) now provide a value for that missing param and check that it parses correctly
    paramMap.put("numRange", "5..10");
    ComplexInitParams params = parser.parse(filterConfig, new ComplexInitParams());
    System.out.println("Parse result: " + parser);
    assertEquals(123L, params.foo);
    assertEquals(true, params.bar);
    assertEquals("something", params.str);
    assertEquals(new NumberRange<>(5, 10), params.numberRange);
  }

  public void testParserWithMissingParamAnnotation() throws Exception {
    paramMap.put("numberRange", "5..10");
    ParameterParserNotAvailable ex = assertThrows(
        ParameterParserNotAvailable.class,
        (Function0_t<ParseException>)() -> parser.parse(filterConfig, new InitParamsWithMissingAnnotation()));
    assertThat(ex.getMessage()).startsWith(
        InitParamsWithMissingAnnotation.class.getSimpleName() + ".numberRange must contain a @Param annotation specifying a ParameterParser<NumberRange> subclass");
  }

  public void testParserWithInadequateParamAnnotation() throws Exception {
    paramMap.put("numRange", "5..10");
    ParameterParserNotAvailable ex = assertThrows(
        ParameterParserNotAvailable.class,
        (Function0_t<ParseException>)() -> parser.parse(filterConfig, new InitParamsWithIncompleteAnnotation()));
    assertThat(ex.getMessage()).startsWith(
        InitParamsWithIncompleteAnnotation.class.getSimpleName() + ".numberRange must contain a @Param annotation specifying a ParameterParser<NumberRange> subclass");
  }

  private static class BasicInitParams implements FilterParameters {
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

  private class InitParamsWithMissingAnnotation implements FilterParameters {
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

  private class InitParamsWithIncompleteAnnotation implements FilterParameters {
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

  private class ComplexInitParams implements FilterParameters {
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

  static class NumberRangeParser implements FilterParameters.ParameterParser<NumberRange<Integer>> {
    @Override
    public NumberRange<Integer> parse(String value) {
      return NumberRange.fromStringIntRange(value);
    }
  }
}