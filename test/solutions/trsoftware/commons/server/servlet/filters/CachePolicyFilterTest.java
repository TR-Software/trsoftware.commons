/*
 * Copyright 2020 TR Software Inc.
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

package solutions.trsoftware.commons.server.servlet.filters;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import junit.framework.TestCase;
import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationFilterFactory;
import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.server.servlet.HttpDateParser;
import solutions.trsoftware.commons.server.servlet.config.InitParameterParseException;
import solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilter.CachePolicy;
import solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilter.CachePolicyMatcher;
import solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilter.DfaScannerMatcher;
import solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilter.SimpleStringMatcher;
import solutions.trsoftware.commons.server.servlet.testutil.DummyFilterChain;
import solutions.trsoftware.commons.server.servlet.testutil.DummyFilterConfig;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletRequest;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse;
import solutions.trsoftware.commons.server.testutil.EmbeddedTomcatServer;
import solutions.trsoftware.commons.server.testutil.PerformanceComparison;
import solutions.trsoftware.commons.shared.annotations.ExcludeFromSuite;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.testutil.TestData;
import solutions.trsoftware.commons.shared.util.MultimapDecorator;
import solutions.trsoftware.commons.shared.util.TimeUnit;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;

import javax.annotation.Nonnull;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilter.CACHE_CONTROL_HEADER_VALUE_10YEARS;
import static solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilter.CachePolicy.*;
import static solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilter.TEN_YEARS_FROM_NOW_DATE;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;

/**
 * @author Alex
 * @since 5/21/2018
 */
public class CachePolicyFilterTest extends TestCase {

  private Map<Class<? extends CachePolicyMatcher>, Multimap<CachePolicy, String>> expectedResults;
  private EmbeddedTomcatServer embeddedTomcatServer;

  public void setUp() throws Exception {
    super.setUp();
    expectedResults = new LinkedHashMap<>();
    expectedResults.put(DfaScannerMatcher.class,
        new MultimapDecorator<CachePolicy, String>(LinkedHashMultimap.create())
            .putAll(DEFAULT,
                "",
                "/foo",
                "/foo/bar",
                "/foo/bar.js",
                "/cache",
                "/cache.js",
                "/foo/cache.js",
                "/foo/.cache.js",
                "/foo/.nocache.js",
                "/foo/foo.nocache.js/bar",
                "/foo/foo.cache.js/foo.bar"
            )
            .putAll(CACHE_FOREVER,
                "/foo.cache.js",
                "/foo/bar.cache.png"
            )
            .putAll(NO_CACHE,
                "/foo.nocache.js",
                "/foo/bar.nocache.png"
            )
            .getMultimap()
    );
    expectedResults.put(SimpleStringMatcher.class,
        new MultimapDecorator<CachePolicy, String>(LinkedHashMultimap.create())
            .putAll(DEFAULT,
                "",
                "/foo",
                "/foo/bar",
                "/foo/bar.js",
                "/cache",
                "/cache.js",
                "/foo/cache.js",
                "/nocache.js",
                "/foo/nocache.js"
            )
            .putAll(CACHE_FOREVER,
                "/foo.cache.js",
                "/foo/bar.cache.png",
                "/foo/.cache.js"
            )
            .putAll(NO_CACHE,
                "/foo.nocache.js",
                "/foo/bar.nocache.png",
                "/foo/.nocache.js"
            )
            .getMultimap()
    );
    // will default to DfaScannerMatcher when the matcher class is unspecified:
    expectedResults.put(null, expectedResults.get(DfaScannerMatcher.class));
  }

  public void tearDown() throws Exception {
    expectedResults = null;
    if (embeddedTomcatServer != null) {
      embeddedTomcatServer.stop();
      embeddedTomcatServer = null;
    }
    super.tearDown();
  }

  @Slow
  public void testIntegration() throws Exception {
    embeddedTomcatServer = new EmbeddedTomcatServer();
    ResourceLocator webAppDir = new ResourceLocator("/CachePolicyFilterTest_webapp", getClass());
    String contextPath = "/CachePolicyFilterTest";
    Context context = embeddedTomcatServer.addWebapp(contextPath, webAppDir.toFilepath());
    embeddedTomcatServer.start();
    ServletContext servletContext = context.getServletContext();
    // create some test cases
    Multimap<CachePolicy, String> testCases = generateExampleURIs(findFilterRegistration(servletContext));
    {
      System.out.println("test cases: " + testCases);
      // make sure we have enough test cases for each enum value
      assertEquals(EnumSet.allOf(CachePolicy.class), testCases.keySet());
      for (CachePolicy cachePolicy : CachePolicy.values()) {
        assertTrue(testCases.get(cachePolicy).size() > 1);
      }
    }
    // now test all the examples
    {
      String baseUrl = "http://localhost:" + embeddedTomcatServer.getPortNumber() + contextPath;
      WebClient webClient = new WebClient();
      SimpleDateFormat httpDateFormat = HttpDateParser.DateFormats.RFC_1123.createSimpleDateFormat();
      for (Map.Entry<CachePolicy, String> entry : testCases.entries()) {
        CachePolicy cachePolicy = entry.getKey();
        String uri = entry.getValue();
        String requestUrl = baseUrl + (uri.startsWith("/") ? uri : "/" + uri);
        // perform the request with HtmlUnit
        TextPage page = webClient.getPage(requestUrl);
        WebResponse webResponse = page.getWebResponse();
        // check the response headers
        List<NameValuePair> responseHeaders = webResponse.getResponseHeaders();
        System.out.printf("Response headers for <GET %s>: %s%n", requestUrl, responseHeaders);
        String cacheControlHeader = webResponse.getResponseHeaderValue("Cache-Control");
        String expiresHeader = webResponse.getResponseHeaderValue("Expires");
        switch (cachePolicy) {
          case CACHE_FOREVER:
            assertEquals(CACHE_CONTROL_HEADER_VALUE_10YEARS, cacheControlHeader);
            assertEquals(httpDateFormat.format(TEN_YEARS_FROM_NOW_DATE), expiresHeader);
            break;
          case NO_CACHE:
            assertEquals("no-cache", webResponse.getResponseHeaderValue("Pragma"));
            assertEquals(httpDateFormat.format(0L), expiresHeader);
            assertThat(cacheControlHeader)
                .contains("no-cache")
                .contains("no-store")
                .contains("must-revalidate")
                .contains("max-age=0")
                .contains("no-transform");
            break;
          default:
            // either way, we set the no-transform directive for reasons described in http://stackoverflow.com/questions/4113268/how-to-stop-javascript-injection-from-vodafone-proxy
            assertEquals("no-transform", cacheControlHeader);
        }
      }
    }
  }

  /**
   * @return the {@link FilterRegistration} for the first instance of {@link CachePolicyFilter} in the given context
   */
  private FilterRegistration findFilterRegistration(ServletContext servletContext) {
    String className = CachePolicyFilter.class.getName();
    for (FilterRegistration registration : servletContext.getFilterRegistrations().values()) {
      if (className.equals(registration.getClassName()))
        return registration;
    }
    fail(String.format("No instances of %s registered in %s", className, servletContext));
    throw new IllegalStateException();
  }

  public void testInit() throws Exception {
    // check parsing the init-param values from a FilterConfig
    // 1) without init params, will default to using cachePolicyMatcher = DfaScannerMatcher
    assertEquals(DfaScannerMatcher.class,
        newInstance(null).getCachePolicyMatcher().getClass());
    // 2) otherwise, will use the given class name
    for (Class<? extends CachePolicyMatcher> matcherClass : Arrays.asList(
        DfaScannerMatcher.class,
        SimpleStringMatcher.class,
        RegexMatcher.class)) {
      assertEquals(matcherClass, newInstance(matcherClass).getCachePolicyMatcher().getClass());
    }
    // 3) will throw an exception if the value of the cachePolicyMatcher param is not a name of a valid subclass of CachePolicyMatcher
    ServletException ex = assertThrows(ServletException.class,
        (Function0_t<? extends Throwable>)() -> newInstance(ArrayList.class));
    assertTrue(ex.getCause() instanceof InitParameterParseException);
  }

  public void testDoHttpFilter() throws Exception {
    for (Class<? extends CachePolicyMatcher> matcherClass : expectedResults.keySet()) {
      CachePolicyFilter cachePolicyFilter = newInstance(matcherClass);
      for (Map.Entry<CachePolicy, String> entry : expectedResults.get(matcherClass).entries()) {
        CachePolicy cachePolicy = entry.getKey();
        String uri = entry.getValue();
        DummyHttpServletRequest request = new DummyHttpServletRequest().setRequestURI(uri);
        DummyHttpServletResponse response = new DummyHttpServletResponse();
        DummyFilterChain filterChain = new DummyFilterChain();
        cachePolicyFilter.doHttpFilter(request, response, filterChain);
        assertTrue(filterChain.wasInvoked());
        // check the response headers
        Multimap<String, Object> headers = response.getHeaders();
        System.out.printf("CachePolicyFilter(%s).doHttpFilter(\"%s\") -> %s%n",
            matcherClass == null ? "null" : matcherClass.getSimpleName(), uri, headers);
        String cacheControlHeader = (String)getSingleValue(headers, "Cache-Control");
        switch (cachePolicy) {
          case CACHE_FOREVER:
            assertThat(cacheControlHeader)
                .isEqualTo(CACHE_CONTROL_HEADER_VALUE_10YEARS)
                .contains("public")
                .contains("no-transform")
                .contains(String.format("max-age=%d", (long)TimeUnit.YEARS.to(TimeUnit.SECONDS, 10)));
            assertEquals(TEN_YEARS_FROM_NOW_DATE, getSingleValue(headers, "Expires"));
            break;
          case NO_CACHE:
            assertEquals("no-cache", response.getHeader("Pragma"));
            assertEquals(0L, getSingleValue(headers, "Expires"));
            assertThat(cacheControlHeader)
                .contains("no-cache")
                .contains("no-store")
                .contains("must-revalidate")
                .contains("max-age=0")
                .contains("no-transform");
            break;
          default:
            // either way, we set the no-transform directive for reasons described in http://stackoverflow.com/questions/4113268/how-to-stop-javascript-injection-from-vodafone-proxy
            assertEquals("no-transform", cacheControlHeader);
        }
      }
    }
  }

  public void testInferCachePolicy() throws Exception {
    for (Class<? extends CachePolicyMatcher> matcherClass : expectedResults.keySet()) {
      CachePolicyFilter cachePolicyFilter = newInstance(matcherClass);
      for (Map.Entry<CachePolicy, String> entry : expectedResults.get(matcherClass).entries()) {
        String uri = entry.getValue();
        CachePolicy result = cachePolicyFilter.inferCachePolicy(uri);
        String msg = String.format("CachePolicyFilter(%s).inferCachePolicy(\"%s\")",
            cachePolicyFilter.getCachePolicyMatcher().getClass().getSimpleName(), uri);
        System.out.printf("%s -> %s%n", msg, result);
        assertEquals(uri, entry.getKey(), result);
      }
    }
  }

  /**
   * Tests the available implementations of {@link CachePolicyMatcher}
   */
  public void testCachePolicyMatcher() throws Exception {
    {
      DfaScannerMatcher scannerMatcher = new DfaScannerMatcher();
      for (Map.Entry<CachePolicy, String> entry : expectedResults.get(DfaScannerMatcher.class).entries()) {
        checkMatchResult(scannerMatcher, entry.getValue(), entry.getKey());
      }
      // now check that it's equivalent to RegexMatcher for some random examples
      RegexMatcher regexMatcher = new RegexMatcher();
      List<String> args = TestData.randomURIs(20);
      for (String arg : args) {
        checkMatchResult(scannerMatcher, arg, regexMatcher.inferCachePolicy(arg));
      }
    }
    {
      SimpleStringMatcher simpleStringMatcher = new SimpleStringMatcher();
      for (Map.Entry<CachePolicy, String> entry : expectedResults.get(SimpleStringMatcher.class).entries()) {
        checkMatchResult(simpleStringMatcher, entry.getValue(), entry.getKey());
      }
    }
  }

  private void checkMatchResult(CachePolicyMatcher scannerMatcher, String uri, CachePolicy expected) {
    CachePolicy result = scannerMatcher.inferCachePolicy(uri);
    System.out.printf("%s.inferCachePolicy(\"%s\") -> %s%n", scannerMatcher.getClass().getSimpleName(), uri, result);
    assertEquals(uri, expected, result);
  }

  private CachePolicyFilter newInstance(Class matcherClass) throws Exception {
    CachePolicyFilter cachePolicyFilter = new CachePolicyFilter();
    LinkedHashMap<String, String> initParameters = new LinkedHashMap<>();
    if (matcherClass != null) {
      initParameters.put("cachePolicyMatcher", matcherClass.getName());
    }
    cachePolicyFilter.init(new DummyFilterConfig(initParameters).setFilterName(getClass().getSimpleName()));
    return cachePolicyFilter;
  }

  @Slow
  @ExcludeFromSuite
  public void testPerformance() throws Exception {
    int n = 200;
    List<String> args = TestData.randomURIs(n);
    CachePolicyMatcherTester[] taskRunners = new CachePolicyMatcherTester[]{
        new CachePolicyMatcherTester(new SimpleStringMatcher(), args),
        new CachePolicyMatcherTester(new DfaScannerMatcher(), args),
        new CachePolicyMatcherTester(new RegexMatcher(), args)
    };
    TreeMap<Long, CachePolicyMatcherTester> timings = new TreeMap<>();
    int iterations = 10_000;
    for (CachePolicyMatcherTester task : taskRunners) {
      long nanoTime = PerformanceComparison.measureNanoTime(task, iterations);
      timings.put(nanoTime, task);
    }
    // print out the timings
    for (Map.Entry<Long, CachePolicyMatcherTester> entry : timings.entrySet()) {
      double totalNanos = entry.getKey();
      double totalMillis = TimeUnit.NANOSECONDS.toMillis(totalNanos);
      System.out.printf("%s took %,f ms (%,f ms per input)", entry.getValue(), totalMillis, totalMillis / (n * iterations));
      // print multiplier from the fastest
      Map.Entry<Long, CachePolicyMatcherTester> fastest = timings.firstEntry();
      if (!entry.equals(fastest)) {
        double multiplier = totalNanos / fastest.getKey();
        System.out.printf("; %.2f times slower than %s", multiplier, fastest.getValue());
      }
      System.out.println();
    }
  }

  /**
   * Generates some example URIs that satisfy the url-pattern mappings for the given filter.
   * @see ApplicationFilterFactory#matchFiltersURL(java.lang.String, java.lang.String)
   */
  private Multimap<CachePolicy, String> generateExampleURIs(FilterRegistration filterRegistration) {
    LinkedHashMultimap<CachePolicy, String> ret = LinkedHashMultimap.create();
    for (String pattern : filterRegistration.getUrlPatternMappings()) {
      if (pattern.equals("/*")) {
        // matches everything
        return expectedResults.get(null);
      }
      else if (pattern.endsWith("/*")) {
        // matches everything that starts with a specific path
        String pathPrefix = pattern.substring(0, pattern.length() - 1);
        for (int i = 0; i < 2; i++) {
          ret.put(DEFAULT, pathPrefix + TestData.randName());
          ret.put(CACHE_FOREVER, pathPrefix + TestData.randFileName(".cache."));
          ret.put(NO_CACHE, pathPrefix + TestData.randFileName(".nocache."));
        }
      }
      else if (pattern.startsWith("*.")) {
        // extension match
        String extension = pattern.substring(2);
        for (int i = 0; i < 2; i++) {
          ret.put(DEFAULT, TestData.randName() + "." + extension);
          ret.put(CACHE_FOREVER, TestData.randName() + ".cache." + extension);
          ret.put(NO_CACHE, TestData.randName() + ".nocache." + extension);
        }
      }
    }
    return ret;
  }

  private static class CachePolicyMatcherTester extends PerformanceComparison.BenchmarkTask<String> {
    private CachePolicyMatcher algorithm;

    CachePolicyMatcherTester(CachePolicyMatcher algorithm, List<String> args) {
      super(algorithm.getClass().getSimpleName(), args);
      this.algorithm = algorithm;
    }

    @Override
    protected void doIteration(String arg) {
      algorithm.inferCachePolicy(arg);
    }

    @Override
    public String toString() {
      return getName();
    }
  }

  /**
   * Uses a regular expression to accomplish the same thing as {@link SimpleStringMatcher} and
   * {@link DfaScannerMatcher}.
   * Provided only for speed comparison.
   */
  public static class RegexMatcher implements CachePolicyMatcher {
    private static Pattern filenameRegex = Pattern.compile(".*[^/]+\\.(cache|nocache)\\.[^/]+");

    /**
     * Determines if the requested file should be cached "forever" or not cached at all.
     *
     * @param uri path of the incoming request (obtained from {@link HttpServletRequest#getRequestURI()}
     * @return {@link CachePolicy#NO_CACHE} if the filename has a <i>.nocache.*</i> extension,
     *     {@link CachePolicy#CACHE_FOREVER} if the filename has a <i>.cache.*</i> extension,
     *     or {@code null} if neither.
     */
    @Nonnull
    @Override
    public CachePolicy inferCachePolicy(String uri) {
      Matcher matcher = filenameRegex.matcher(uri);
      if (matcher.matches()) {
        if ("cache".equals(matcher.group(1)))
          return CACHE_FOREVER;  // has a .cache.* extension
        else
          return NO_CACHE;  // has a .nocache.* extension
      }
      return DEFAULT;
    }
  }
}