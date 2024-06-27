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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.*;
import com.google.common.reflect.ClassPath;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.server.io.file.FileUtils;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.collections.ListAdapter;
import solutions.trsoftware.commons.shared.util.iterators.IndexedIterator;
import solutions.trsoftware.tools.util.BytecodeParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static solutions.trsoftware.commons.shared.util.StringUtils.capitalize;
import static solutions.trsoftware.commons.shared.util.StringUtils.indent;
import static solutions.trsoftware.junit.TestSuiteBuilder.FilterMode.EXCLUDE;
import static solutions.trsoftware.junit.TestSuiteBuilder.FilterMode.INCLUDE;

/**
 * Builds a suite of tests from the test classes found in one or more {@linkplain #addContentRoot(Path) filesystem paths},
 * filtered using one or more of the provided builder methods (e.g. {@link #includeOnlySubclassesOf},
 * {@link #excludeSubpackagesOf}, etc.) and/or {@linkplain System#getProperty(String) system properties}
 * prefixed with {@value #CONFIG_PROPERTY_PREFIX}.
 * <p>
 * <em>Note</em>: this class is designed to work with JUnit 3.8; newer versions of JUnit might provide some (or all) of this
 * functionality natively.
 * <p>
 * <p>
 *
 * <h3>Examples:</h3>
 * <ol>
 *   <li>
 *     Programmatic configuration:
 *     <pre>
 *       new {@link TestSuiteBuilder#TestSuiteBuilder()}
 *         .{@link #addContentRoot}(Paths.get("~/projects/myproject"))
 *         .{@link #includeOnlySubpackagesOf}("com.example")
 *         .{@link #excludeSubpackagesOf}("com.example.server.model", "com.example.server.servlet")
 *         .{@link #excludeSubclassesOf}(com.example.DatabaseTestCase.class)
 *         .{@link #excludeTestsAnnotatedWith}(com.example.annotation.Slow.class)
 *         .{@link #buildSuite()}
 *     </pre>
 *   </li>
 *   <li>
 *     Equivalent system properties (specified as VM options):
 *     <pre>
 *       -DTestSuiteBuilder.package.include="com.example"
 *       -DTestSuiteBuilder.package.exclude="com.example.server.model,com.example.server.servlet"
 *       -DTestSuiteBuilder.superclass.exclude="com.example.DatabaseTestCase"
 *       -DTestSuiteBuilder.annotations.exclude="com.example.annotation.Slow"
 *     </pre>
 *     Property name syntax:
 *     <pre style="font-style: italic;">
 *       TestSuiteBuilder.[package|superclass|annotation].[include|exclude]
 *     </pre>
 *   </li>
 * </ol>
 *
 * @author Alex
 * @since 4/18/2018
 */
public class TestSuiteBuilder {

  private static final Logger LOGGER = Logger.getLogger(TestSuiteBuilder.class.getName());

  /**
   * Matches filenames ending with {@code "Test.class"}}
   */
  public static final String DEFAULT_FILENAME_REGEX = ".*Test\\.class";
  /**
   * Additional configuration can be specified using {@linkplain System#getProperty(String) system properties}
   * having this prefix.
   * <p>
   * <b>Example:</b>
   * <pre>
   *   -DTestSuiteBuilder.package.include="com.example"
   *   -DTestSuiteBuilder.package.exclude="com.example.server.model,com.example.server.servlet"
   *   -DTestSuiteBuilder.superclass.exclude="com.example.DatabaseTestCase"
   *   -DTestSuiteBuilder.annotations.exclude="com.example.annotation.Slow"
   * </pre>
   * is equivalent to:
   * <pre>
   *   new {@link TestSuiteBuilder#TestSuiteBuilder()}
   *     .{@link #includeOnlySubpackagesOf}("com.example")
   *     .{@link #excludeSubpackagesOf}("com.example.server.model", "com.example.server.servlet")
   *     .{@link #excludeSubclassesOf}(com.example.DatabaseTestCase.class)
   *     .{@link #excludeTestsAnnotatedWith}(com.example.annotation.Slow.class)
   * </pre>
   * The system property naming syntax is
   * <code>{@value #CONFIG_PROPERTY_PREFIX}.[package|superclass|annotation].[include|exclude]</code>
   * @see #filterNames
   * @see FilterMode
   */
  public static final String CONFIG_PROPERTY_PREFIX = "TestSuiteBuilder";

//  /**
//   * Matches filenames ending with {@code "Test.class"} or {@code "TestCase.class"}
//   */
//  public static final String DEFAULT_FILENAME_REGEX = ".*Test(?:Case)?\\.class";

  /** Regular expression that matches the test class file (path) names to be considered for inclusion in the suite */
  private String filenameRegex = DEFAULT_FILENAME_REGEX;;

  /** Paths to be searched for test classes */
  private final LinkedHashSet<Path> contentRoots = new LinkedHashSet<>();

  /**
   * Whether the result should be a tree of suites, grouped by package. Otherwise it will be a single suite containing
   * just the individual test cases (each of which will be a suite of the test methods defined in a subclass of
   * {@link TestCase})
   * <p>
   * <b>NOTE:</b> this setting is incompatible with {@link #useGwtTestSuite}
   */
  private boolean groupByPackage;

  /**
   * Whether the result should be an instance of {@link GWTTestSuite}, which is preferable to a normal {@link TestSuite}
   * when instances of {@link GWTTestCase} might be involved, because {@link GWTTestSuite} improves compilation
   * time by re-ordering the tests "so that all cases that share a module are run back to back"
   * <p>
   * <b>NOTE:</b> this setting is incompatible with {@link #groupByPackage}
   *
   * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTesting.html#DevGuideJUnitSuites">GWT Testing Guide</a>
   */
  private boolean useGwtTestSuite;

  /**
   * If not {@code null}, individual tests will be wrapped with a {@link TestTimeBoxDecorator}
   */
  private TestTimeBoxDecorator.TimeBoxSettings timeBoxSettings;

  /**
   * Used by {@link #isValidPackage(String)}
   * <p>
   * TODO(9/18/2023): could refactor our whole whole from contentRoots file-tree walking mechanism
   *       using with Guava's ClassPath, and apply package filtering that way
   */
  private ClassPath classPath;  // TODO(9/20/2023): not used for now b/c adds a significant startup perf penalty

  /**
   * Test classes inheriting from these will be either included in or excluded from the suite,
   * depending on the {@link FilterMode} key.
   */
  private final FilterSet<Class<?>, Class<?>> superclassFilters = new FilterSet<>(Class::isAssignableFrom);
  /**
   * Test methods (or classes) marked with these annotations will be either included in or
   * excluded from the suite, depending on the {@link FilterMode} key.
   */
  private final FilterSet<Class<? extends Annotation>, AnnotatedElement> annotationFilters
      = new FilterSet<>(ReflectionUtils::hasDeclaredAnnotation);

  private final FilterSet<String, Class<?>> packageFilters = new FilterSet<>(TestSuiteBuilder::isInSubpackageOf);

  /**
   * Names used with {@link #CONFIG_PROPERTY_PREFIX} for specifying filters as system properties.
   * @see #addFiltersFromSystemProps
   */
  private final BiMap<String, FilterSet<?, ?>> filterNames = ImmutableBiMap.of(
      "superclass", superclassFilters,
      "annotation", annotationFilters,
      "package", packageFilters
  );

  /**
   * Determines how {@link #superclassFilters}, {@link #packageFilters}, or {@link #annotationFilters} will be used.
   */
  public enum FilterMode {
    /**
     * In relation to {@link #superclassFilters}:
     *   test classes will be included in the suite if (and only if) they inherit from these superclasses.
     * In relation to {@link #annotationFilters}:
     *   test methods/classes will be included in the suite if (and only if) they are annotated with these annotations.
     */
    INCLUDE,
    /**
     * In relation to {@link #superclassFilters}:
     *   test classes will be included in the suite if (and only if) they don't inherit from these superclasses.
     * In relation to {@link #annotationFilters}:
     *   test methods/classes will be included if (and only if) they are not annotated with particular annotations.
     * @see #annotationFilters
     */
    EXCLUDE;

    /**
     * @return the opposite of this value
     * (i.e. {@code INCLUDE.opposite() -> EXCLUDE}, and {@code EXCLUDE.opposite() -> INCLUDE})
     */
    public FilterMode opposite() {
      FilterMode[] values = values();
      assert values.length == 2;
      return values[(ordinal() + 1) % 2];
    }

    public String prettyName() {
      return name().toLowerCase();
    }
  }

  /**
   *
   * @param <T> the filter condition type
   * @param <V> type of elements being filtered against the condition
   */
  private static class FilterSet<T, V> implements Predicate<V> {

    private final BiPredicate<T, V> matcher;
    private final SetMultimap<FilterMode, T> map = MultimapBuilder.enumKeys(FilterMode.class).linkedHashSetValues().build();

    /**
     * @param matcher a predicate that determines whether the filter condition object, {@link T},
     *     matches the second argument, {@link V}
     */
    FilterSet(BiPredicate<T, V> matcher) {
      this.matcher = matcher;
    }

    @SafeVarargs
    final void add(FilterMode mode, T... filters) {
      FilterMode oppositeMode = mode.opposite();
      for (T filter : filters) {
        // make sure this map doesn't already contain the opposite of this filter
        if (map.get(oppositeMode).contains(filter))
          throw new IllegalStateException(format("Cannot %s <%s> because it is already being explicitly %sd",
              mode.prettyName(), filter, oppositeMode.prettyName()));
        map.put(mode, filter);
      }
    }

    /**
     * @return {@code true} iff either
     *     (1) given class or method is explicitly included, OR
     *     (2) there aren't any explicit inclusions
     */
    boolean includes(V value) {
      return !map.containsKey(INCLUDE) || matches(INCLUDE, value);
    }

    /**
     * @return {@code true} iff the given class or method is explicitly excluded
     */
    boolean excludes(V value) {
//      return !get(EXCLUDE).isEmpty() && isExcluded(value);
      return matches(EXCLUDE, value);
    }

    private boolean matches(FilterMode mode, V value) {
      return map.get(mode).stream().anyMatch(t -> matcher.test(t, value));
    }

    @Override
    public boolean test(V value) {
      return includes(value) && !excludes(value);
    }

    public void clear() {
      map.clear();
    }

    @Override
    public String toString() {
      return map.toString();
    }
  }


  public TestSuiteBuilder() {
//    classPath = parseClassPath();
  }

  static ClassPath parseClassPath() {  // TODO: experimental
    try {
      return ClassPath.from(ClassLoader.getSystemClassLoader());
    }
    catch (IOException e) {
      LOGGER.log(Level.WARNING, e, () -> format("Unable to parse classpath with %s", ClassPath.class));
      // suppress exception; classPath will simply not be available as a validation method for isValidPackage
    }
    return null;
  }

  /**
   * @param filenameRegex see {@link #filenameRegex}
   * @return self, for chaining
   */
  public TestSuiteBuilder setFilenameRegex(String filenameRegex) {
    this.filenameRegex = filenameRegex;
    return this;
  }

  public String getFilenameRegex() {
    return filenameRegex;
  }

  /**
   * @param groupByPackage see {@link #groupByPackage}
   * @return self, for chaining
   */
  public TestSuiteBuilder setGroupByPackage(boolean groupByPackage) {
    this.groupByPackage = groupByPackage;
    return this;
  }

  /**
   * @param useGwtTestSuite see {@link #useGwtTestSuite}
   * @return self, for chaining
   */
  public TestSuiteBuilder setUseGwtTestSuite(boolean useGwtTestSuite) {
    this.useGwtTestSuite = useGwtTestSuite;
    return this;
  }

  /**
   * Adds the given path to the set of content roots to be scanned for classes.
   *
   * @param path a path containing test classes
   * @return self, for chaining
   * @see #addContentRoot(Class)
   */
  public TestSuiteBuilder addContentRoot(@Nonnull Path path) {
    contentRoots.add(Objects.requireNonNull(path, "path"));
    return this;
  }

  /**
   * Adds the path of the given reference class to the set of content roots to be scanned for classes.
   *
   * @param refClass the location of the corresponding {@code .class} file will be used to look for tests to add to the suite
   * @return self, for chaining
   * @see ReflectionUtils#getCompilerOutputPath(Class)
   */
  public TestSuiteBuilder addContentRoot(Class<?> refClass) {
    Path path = ReflectionUtils.getCompilerOutputPath(refClass);
    if (path == null) {
      LOGGER.severe("Unable to find compiler output path for " + refClass);
      return this;
    }
    return addContentRoot(path);
  }

  /**
   * Adds the compiler output path containing the given package to the set of content roots to be scanned for classes.
   *
   * @return self, for chaining
   * @see ReflectionUtils#getCompilerOutputPath(Class)
   */
  public TestSuiteBuilder addPackage(String packageName) {
    /*
     TODO(9/13/2023): this doesn't work if project contains multipe source roots with the same package
       Example: given source roots "src" and "tools", IntelliJ might put the "tools" root ahead of "src" on classpath,
       which would make ClassLoader.getResource return the URL for the "tools" directory instead of "src".
       Therefore:
         - either deprecate or remove this method
         - instead, use addContentRoot(Class<?> refClass) in conjunction w/"TestSuiteBuilder.packages" sys prop to filter
           based on package
    */

    String resourceName = packageName.replace('.', '/');
    ResourceLocator res = new ResourceLocator(resourceName);
    if (res.exists()) {
      Path path = res.toPath();
      if (Files.exists(path)) {
        return addContentRoot(path);
      }
    }
    LOGGER.severe("Unable to find compiler output path for package " + packageName);
    return this;
  }

  /**
   * Adds the compiler output path containing the package of the given class
   * to the set of content roots to be scanned for classes.
   *
   * @return self, for chaining
   * @see ReflectionUtils#getCompilerOutputPath(Class)
   */
  public TestSuiteBuilder addPackageOf(Class<?> classInPackage) {
    /*
     TODO(9/13/2023): this doesn't work if project contains multipe source roots with the same package
       Example: given source roots "src" and "tools", IntelliJ might put the "tools" root ahead of "src" on classpath,
       which would make ClassLoader.getResource return the URL for the "tools" directory instead of "src".
       Therefore:
         - either deprecate or remove this method
         - instead, use addContentRoot(Class<?> refClass) in conjunction w/"TestSuiteBuilder.packages" sys prop to filter
           based on package
    */
    return addPackage(classInPackage.getPackage().getName());
  }

  /**
   * Asserts that none of the settings clash with each other and clears the transient state of this builder in preparation
   * to build a new suite.
   *
   * @throws IllegalStateException if the current configuration of this builder violates any constraints
   */
  private void prepareToBuild() throws IllegalStateException {
    LOGGER.config(() -> format("Building a test suite from %s files in %s", filenameRegex, contentRoots));
    if (useGwtTestSuite && groupByPackage)
      throw new IllegalStateException("useGwtTestSuite and groupByPackage are mutually exclusive");
    classToTestSuiteMap.clear();  // reset the state
    // add any additional filters specified as System properties
    // TODO: unit test this
    addFiltersFromSystemProps(packageFilters, Function.identity());  // TODO: not calling isValidPackage b/c that method currently doesn't do anything
    addFiltersFromSystemProps(superclassFilters, TestSuiteBuilder::loadClass);
    addFiltersFromSystemProps(annotationFilters, TestSuiteBuilder::loadAnnotation);
  }

  /**
   * Adds to the given filter set any conditions specified as {@linkplain #CONFIG_PROPERTY_PREFIX system properties}.
   *
   * @param parser parses a system property value string into a filter condition for the given filter set;
   *   should return {@code null} if unable to derive an appropriate object from the string
   *
   * @param <T> the filter condition type
   */
  private <T> void addFiltersFromSystemProps(FilterSet<T, ?> filterSet, Function<String, T> parser) {
    String filterName = filterNames.inverse().get(filterSet);
    for (FilterMode filterMode : FilterMode.values()) {
      String filterModeName = filterMode.prettyName();
      String propName = String.join(".", CONFIG_PROPERTY_PREFIX, filterName, filterModeName);
      String propValue = System.getProperty(propName);
      if (propValue != null) {
        Pattern.compile(",").splitAsStream(propValue).map(String::trim)
            .map(parser).filter(Objects::nonNull)
            .forEach(item -> {
              LOGGER.config(() -> format("%s %s: %s", capitalize(filterModeName), filterName, item));
              filterSet.add(filterMode, item);
            });
      }
    }
  }

  @Nullable
  private static Class<?> loadClass(String className) {
    // TODO: experimental helper for parsisng sys props
    try {
      return Class.forName(className);
    }
    catch (Throwable e) {
      e.printStackTrace();
      return null;
    }
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private static Class<? extends Annotation> loadAnnotation(String className) {
    // TODO: experimental helper for parsisng sys props
    Class<?> cls = loadClass(className);
    if (cls != null && cls.isAnnotation())
      return (Class<? extends Annotation>)cls;
    return null;
  }

  /**
   * Same as {@link #buildSuite(String)}, but uses the name of the caller's class as the name of the suite.
   * <p>
   * <b>CAUTION</b>: the caller's class name will be derived from {@link Throwable#getStackTrace()}, which may not be what you want.
   *
   * @return the generated test suite (which might be an instance of {@link GWTTestSuite} if the {@link #useGwtTestSuite}
   * setting was specified).
   *
   * @throws IllegalStateException if the current configuration of this builder violates any constraints (see {@link #prepareToBuild()})
   * @throws IOException any exception thrown while searching for and parsing the test class files.
   */
  public TestSuite buildSuite() throws IllegalStateException, IOException {
    // attempt to infer the caller's class name to use as the name of the suite
    String callerClassName = new Throwable().getStackTrace()[1].getClassName();
    return buildSuite(callerClassName);
  }

  /**
   * Builds a test suite containing the matching test classes found in the paths provided by {@link #addContentRoot(Path)}
   *
   * @param name the name for the generated test suite
   * @return the generated test suite (which might be an instance of {@link GWTTestSuite} if the {@link #useGwtTestSuite}
   * setting was specified).
   *
   * @throws IllegalStateException if the current configuration of this builder violates any constraints (see {@link #prepareToBuild()})
   * @throws IOException any exception thrown while searching for and parsing the test class files.
   */
  public TestSuite buildSuite(String name) throws IllegalStateException, IOException {
    prepareToBuild();
    TestSuite testSuite = useGwtTestSuite ? new GWTTestSuite(name) : new TestSuite(name);
    Pattern filenamePattern = Pattern.compile(filenameRegex);
    /* TODO(5/29/2023): filter the contentRoots to make sure that nothing is included more than once,
         e.g.  if contentRoots = ["/a/b/c", "/a/b/"], the "/a/b/c" entry is superfluous since already included in "/a/b/"
        Update(9/13/2023): this is low priority since we're now using a hash map (testSuitesByClass) to prevent duplicates
      */
    for (Path root : contentRoots) {
      if (groupByPackage)
        addGroupedByPackage(testSuite, root, filenamePattern);
      else
        addFlat(testSuite, root, filenamePattern);
    }
    if (testSuite instanceof GWTTestSuite)
      prettifyGwtModuleNames((GWTTestSuite)testSuite);
    return testSuite;
  }

  /**
   * Builds a {@link GWTTestSuite} containing the matching test classes found in the paths provided by {@link #addContentRoot(Path)}.
   * This is preferable to a normal {@link TestSuite} when instances of {@link GWTTestCase} might be involved,
   * because {@link GWTTestSuite} improves compilation time by re-ordering the tests "so that all cases that share a
   * module are run back to back"
   *
   * @param name the name for the generated test suite
   * @return the generated test suite
   * @throws IOException any exception thrown while searching for and parsing the test class files.
   * @throws IllegalStateException if {@link #groupByPackage} is set to {@code true}
   * @deprecated call {@link #setUseGwtTestSuite(boolean)} followed by {@link #buildSuite(String)} instead
   *
   * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTesting.html#DevGuideJUnitSuites">GWT Testing Guide</a>
   */
  public TestSuite buildGwtSuite(String name) throws IOException {
    setUseGwtTestSuite(true);
    return buildSuite(name);
  }

  /**
   * Builds the test suite.
   *
   * @param name the name for the generated test suite
   * @param roots the roots of the file trees to search for tests.
   * @return the generated test suite
   * @throws IOException any exception thrown while searching for and parsing the test class files.
   * @deprecated use {@link #addContentRoot(Path)} followed by {@link #buildSuite(String)}
   */
  public TestSuite buildSuite(String name, Path... roots) throws IOException {
    for (Path root : roots) {
      addContentRoot(root);
    }
    return buildSuite(name);
  }

  /**
   * Builds a test suite containing the tests specified by this builder, using the compiler output file tree
   * of the given class.
   *
   * @param refClass The location of this class on disk will be used to look for tests to add to the suite.
   * @return the generated test suite (with the same name as the given class)
   * @throws IOException any exception thrown while searching for and parsing the test class files.
   * @deprecated use {@link #addContentRoot(Class)} followed by {@link #buildSuite(String)}
   */
  public TestSuite buildSuite(Class<?> refClass) throws IOException {
    addContentRoot(refClass);
    return buildSuite(refClass.getName());
  }

  private void addGroupedByPackage(TestSuite rootTestSuite, Path root, Pattern filenamePattern) throws IOException {
    Deque<TestSuite> suiteStack = new LinkedList<>(); // will be used to build a tree of test suites
    Files.walkFileTree(root, new FileUtils.FilenamePatternVisitor(filenamePattern) {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.equals(root))
          suiteStack.push(rootTestSuite);
        else
          suiteStack.push(new TestSuite(dir.getFileName().toString()));
        return FileVisitResult.CONTINUE;
      }

      @Override
      protected FileVisitResult visitMatchedFile(Path file, BasicFileAttributes attrs, Matcher match) {
        Class<?> cls = getClassFromFile(file);
        if (cls != null && includeClass(cls)) {
          TestSuite dirTestSuite = suiteStack.peek();
          addIfNotEmpty(dirTestSuite, createTestSuiteForClass(cls));
          Package pkg = cls.getPackage();
          if (dirTestSuite != null && pkg != null)
            dirTestSuite.setName(pkg.getName());  // update the name of the suite to be the proper name of the package
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        TestSuite dirTestSuite = suiteStack.pop();
        if (!dirTestSuite.equals(rootTestSuite) && !isEmpty(dirTestSuite)) {
          if (suiteStack.isEmpty()) {
            // we're back up at the root of the tree (traversal completed)
            rootTestSuite.addTest(dirTestSuite);
          }
          else {
            // add this suite to the one for the parent directory
            suiteStack.peek().addTest(dirTestSuite);
          }
        }
        return FileVisitResult.CONTINUE;
      }
    });
    assert suiteStack.isEmpty();
  }

  private void addFlat(TestSuite rootTestSuite, Path root, Pattern filenamePattern) throws IOException {
    Files.walkFileTree(root, new FileUtils.FilenamePatternVisitor(filenamePattern) {
      @Override
      protected FileVisitResult visitMatchedFile(Path file, BasicFileAttributes attrs, Matcher match) {
        Class<?> cls = getClassFromFile(file);
        if (cls != null && includeClass(cls)) {
          addIfNotEmpty(rootTestSuite, createTestSuiteForClass(cls));
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * Uses {@link Class#forName(String)} to get the {@link Class} object associated with the given
   * compiled {@code .class} file.
   *
   * @param file location of a compiled {@code .class} file
   * @return the class object or {@code null} if an exception occurred during the lookup
   */
  @Nullable
  private static Class<?> getClassFromFile(Path file) {
    /* TODO: consider moving method to ReflectionUtils (as the inverse of ReflectionUtils.getClassFile)
     *   - the extracted method should rethrow exceptions rather than return null
     *   Note: this would require moving BytecodeParser from tools to src, thereby adding a BCEL dependency in prod
     */
    Class<?> cls = null;
    try {
      String className = BytecodeParser.extractClassName(file.toFile());
      cls = Class.forName(className);
    }
    catch (Throwable e) {
      // the file is probably not a valid class; print stack trace and ignore this exception
      LOGGER.log(Level.WARNING, "Unable to find Class for " + file, e);
      e.printStackTrace();
    }
    return cls;
  }

  /**
   * Adds a test suite to another if it's not empty.
   * @param parent suite to add to
   * @param child will be added if it contains any tests
   * @return the {@code parent}, for method chaining
   */
  private static TestSuite addIfNotEmpty(TestSuite parent, TestSuite child) {
    if (!isEmpty(child))
      parent.addTest(child);
    return parent;
  }

  /**
   * @return {@code true} iff the given {@link TestSuite} contains no tests
   */
  public static boolean isEmpty(TestSuite suite) {
    return suite == null || suite.testCount() == 0;
  }

  /**
   * Caches tests to ensure there are no duplicates
   */
  private LinkedHashMap<Class<?>, TestSuite> classToTestSuiteMap = new LinkedHashMap<>();

  private TestSuite createTestSuiteForClass(Class<?> testClass) {
    if (classToTestSuiteMap.containsKey(testClass)) {
      // already added a suite for this class; probably a duplicate contentRoots entry
      return null;
    }
    // leverage the functionality already provided by TestSuite to convert a TestCase into a suite containing its test methods
    TestSuite testSuiteForClass = new TestSuite(testClass);
    // now modify that TestSuite as needed
    SortedMap<String, Test> testsByName = new TreeMap<>();  // alphabetically sort the tests by name
    for (Test test : listTests(testSuiteForClass)) {
      /*
      the default test suite generated for a class will contain an entry for each test method, each being an instance
      of that same class (which is almost certainly an instance of TestCase), so the following cast should succeed
      */
      TestCase testCase = (TestCase)test;
      String testName = testCase.getName();
      Method testMethod;
      try {
        testMethod = testClass.getMethod(testName);
      }
      catch (NoSuchMethodException e) {
        if (testCase.toString().equals("warning(junit.framework.TestSuite$1)")) {
          /*
          this is a special-case marker inserted by the TestSuite(Class) constructor to warn about
          a problem with the test class (e.g. "No tests found in ..."),
          in which case we should simply add it to our result as-is
          */
          testsByName.put(testName, testCase);
          continue;
        }
        else {
          // otherwise, we expect each test in a TestSuite for a class to correspond to a method in the class, so rethrow the ex
          throw new RuntimeException(e);
        }
      }
      if (applyAnnotationFilters(testMethod)) {
        testsByName.put(testName,
            timeBoxSettings != null ?
                new TestTimeBoxDecorator(testCase, testMethod, timeBoxSettings)
                : testCase);
      }
    }
    testSuiteForClass = new TestSuite(testSuiteForClass.getName());
    for (Test test : testsByName.values()) {
      testSuiteForClass.addTest(test);
    }
    classToTestSuiteMap.put(testClass, testSuiteForClass);
    return testSuiteForClass;
  }

  /**
   * Subclasses of the given superclasses or interfaces will be excluded from the suite.
   */
  public TestSuiteBuilder excludeSubclassesOf(Class<?>... superClasses) {
    superclassFilters.add(EXCLUDE, superClasses);
    return this;
  }

  /**
   * Only subclasses of the given superclasses or interfaces will be included in the suite.
   */
  public TestSuiteBuilder includeOnlySubclassesOf(Class<?>... superClasses) {
    superclassFilters.add(INCLUDE, superClasses);
    return this;
  }

  /**
   * Clears any {@linkplain #superclassFilters superclass filters} that might have been previously set
   * via {@link #includeOnlyTestsAnnotatedWith} or {@link #excludeTestsAnnotatedWith}.
   */
  public TestSuiteBuilder clearSuperclassFilters() {
    superclassFilters.clear();
    return this;
  }

  /**
   * Test methods (or classes) marked with the given annotations will be excluded from the suite.
   * @param annotationClasses an {@code @interface}
   */
  @SafeVarargs
  public final synchronized TestSuiteBuilder excludeTestsAnnotatedWith(Class<? extends Annotation>... annotationClasses) {
    annotationFilters.add(EXCLUDE, annotationClasses);
    return this;
  }

  /**
   * Test methods (or classes) will be included in the suite <b>only if</b> marked with any of the given annotations.
   * @param annotationClasses an {@code @interface}
   */
  @SafeVarargs
  public final synchronized TestSuiteBuilder includeOnlyTestsAnnotatedWith(Class<? extends Annotation>... annotationClasses) {
    annotationFilters.add(INCLUDE, annotationClasses);
    return this;
  }

  /**
   * Clears any {@linkplain #annotationFilters annotation filters} that might have been previously set
   * via {@link #includeOnlyTestsAnnotatedWith} or {@link #excludeTestsAnnotatedWith}.
   */
  public TestSuiteBuilder clearAnnotationFilters() {
    annotationFilters.clear();
    return this;
  }

  /**
   * Include only tests from the given packages (and their descendents)
   * @param parentPackages the names of packages whose tests are to be included
   */
  public TestSuiteBuilder includeOnlySubpackagesOf(String... parentPackages) {
    packageFilters.add(INCLUDE, validatePackageNames(parentPackages));
    return this;
  }

  /**
   * Exclude any tests from the given packages (and their descendents)
   * @param parentPackages the names of packages whose tests are to be excluded
   */
  public TestSuiteBuilder excludeSubpackagesOf(String... parentPackages) {
    packageFilters.add(EXCLUDE, validatePackageNames(parentPackages));
    return this;
  }

  /**
   * Clears any {@linkplain #packageFilters package filters} that might have been previously set
   * via {@link #includeOnlySubpackagesOf} or {@link #excludeSubpackagesOf}.
   */
  public TestSuiteBuilder clearPackageFilters() {
    packageFilters.clear();
    return this;
  }

  private String[] validatePackageNames(String... pkgNames) {
    return Arrays.stream(pkgNames).filter(name -> {
      /*
      NOTE: would be nice if we could just use Package.getPackage(name) to validate a package name,
      but unfortunately, that returns null until an actual class from that package has been loaded by the ClassLoader,
      even if the package name is valid
       */
      if (!isValidPackage(name)) {
        // TODO: maybe throw exception instead?
        LOGGER.warning(() -> format("Unable to find package <%s>", name));
        return false;
      }
      return true;
    }).toArray(String[]::new);
  }

  private boolean isValidPackage(String pkgName) {
    /*
    NOTE: would be nice if we could just use Package.getPackage(name) to validate a package name,
    but unfort., that returns null until an actual class from that package has been loaded by the ClassLoader,
    even if the package name is valid
     */
//    return Package.getPackage(pkgName) != null;
    if (classPath != null) {
      // TODO: temporarily disabled (classPath always null) due to startup perf overhead of using ClassPath
      ImmutableSet<ClassPath.ClassInfo> classesInPkg = classPath.getTopLevelClassesRecursive(pkgName);
      return !classesInPkg.isEmpty();
    }
    return true;  // don't have a way to validate the package
  }

  /**
   * Decides whether to include a test class or method in the suite based on present or absent annotations.
   * @param annotatedElement the {@link TestCase} subclass, or the original method corresponding to a test in such a class.
   * @return {@code true} iff the given class or test method should be included in the suite
   * @see #annotationFilters
   * @see FilterMode
   */
  @VisibleForTesting
  boolean applyAnnotationFilters(AnnotatedElement annotatedElement) {  // exposed with package-private visibility for unit testing
    FilterSet<Class<? extends Annotation>, AnnotatedElement> filters = this.annotationFilters;
    if (annotatedElement instanceof Class) {
      /*
      for classes, test only the exclusion rules
      because some of its methods might have a required ("include") annotation even if the class itself does not
      */
      return !filters.excludes(annotatedElement);
      // TODO(9/15/2023): should we also check annotations on the package of the class?
    }
    else {
      // for methods, we test both inclusion and exclusion rules
      assert annotatedElement instanceof Method;
      Method method = (Method)annotatedElement;

      // we also want to consider the annotations present on the method's declaring class
      Class<?> declaringClass = method.getDeclaringClass();

      // a method is included if either it or its class has an included ann, and neither it nor its class has an excluded ann
      return (filters.includes(method) || filters.includes(declaringClass))
          && !(filters.excludes(method) || filters.excludes(declaringClass));
    }
  }

  /**
   * Decides whether to include the given test class into the suite based on the {@link #superclassFilters}
   * @param cls a test class ({@link TestCase})
   * @return {@code true} iff the given class should be included in the suite
   */
  @VisibleForTesting
  boolean applySuperclassFilters(Class<?> cls) {  // exposed with package-private visibility for unit testing
    return superclassFilters.test(cls);
  }

  /**
   * Decides whether to include the given test class into the suite based on the {@link #packageFilters}
   * @param cls a test class ({@link TestCase})
   * @return {@code true} iff the given class should be included in the suite
   */
  @VisibleForTesting
  boolean applyPackageFilters(Class<?> cls) {  // exposed with package-private visibility for unit testing
    return packageFilters.test(cls);
  }

  /**
   * Wrap individual tests with a {@link TestTimeBoxDecorator} using the given settings.
   * Don't use this method if wrapping tests with a {@link TestTimeBoxDecorator} is not desired.
   * @return self, for method chaining
   */
  public TestSuiteBuilder setTimeBoxSettings(TestTimeBoxDecorator.TimeBoxSettings timeBoxSettings) {
    this.timeBoxSettings = Objects.requireNonNull(timeBoxSettings);
    return this;
  }

  /**
   * Determines if the given class should be included in the test suite.
   *
   * @param cls candidate class for inclusion in the suite
   * @return {@code true} iff the given class is a test class that should be included in the test suite.
   */
  protected boolean includeClass(Class<?> cls) {
    int modifiers = cls.getModifiers();
    return
        // make sure this is a concrete class (otherwise it can't be instantiated by the test runner)
        !Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers)
            // make sure this is a subclass of TestCase that's not excluded by any filters
            && TestCase.class.isAssignableFrom(cls)
            && applyPackageFilters(cls)
            && applySuperclassFilters(cls)
            && applyAnnotationFilters(cls);
    // can override to provide additional filters
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("filenameRegex", StringUtils.quote(filenameRegex))
        .add("contentRoots", contentRoots.stream().map(Path::toString).map(StringUtils::quote).toArray())
        .add("packageFilters", packageFilters)
        .add("superclassFilters", superclassFilters)
        .add("annotationFilters", annotationFilters)
        .add("groupByPackage", groupByPackage)
        .add("useGwtTestSuite", useGwtTestSuite)
        .add("timeBoxSettings", timeBoxSettings)
        .toString();
  }

  // Static utility methods:

  /**
   * Returns true if the given class is in the given package or in a subpackage (direct or indirect descendent)
   * of the given package.
   * @param parentPackage name of parent package
   * @param cls a class that may (or may not) be a member of the given package or of a subpackage of that package
   */
  static boolean isInSubpackageOf(String parentPackage, Class<?> cls) {
    // TODO: move method (& test) to ReflectionUtils?
    return isDescendentPackage(parentPackage, cls.getPackage().getName());
  }

  /**
   * Tests whether {@code childPackage} is a direct or indirect descendent of {@code parentPackage}.
   * @param parentPackage name of parent package
   * @param childPackage name of potential subpackage
   * @return {@code true} iff {@code childPackage} is the same as or is a descendent of {@code parentPackage}
   */
  static boolean isDescendentPackage(@Nonnull String parentPackage, @Nonnull String childPackage) {
    // TODO: move method (& test) to ReflectionUtils?
    if (childPackage.length() < parentPackage.length())
      return false;  // fail early
    // could be the same package or a subpackage
    return (childPackage + ".").startsWith(parentPackage + ".");  // Note: append dot on both sides b/c could be the same package
  }

  /**
   * Recursively prints the tests in a tree of test suites.
   *
   * @param suite the suite whose tests are to be printed
   */
  public static void printTestSuite(TestSuite suite) {
    String heading = format("------------ TestSuite(name=\"%s\", %d tests): ------------", suite.getName(), suite.countTestCases());
    System.out.println(heading);
    printTestSuiteRecursive(suite, System.out, 0);
    System.out.println(StringUtils.repeat('-', heading.length()));
  }

  private static void printTestSuiteRecursive(TestSuite suite, PrintStream out, int indent) {
    Enumeration tests = suite.tests();
    while (tests.hasMoreElements()) {
      Test test = (Test)tests.nextElement();
      out.printf("%s+ %s%n", indent(indent), test);
      if (test instanceof TestSuite)
        printTestSuiteRecursive((TestSuite)test, out, indent + 2);
    }
  }

  /**
   * Converts a tree of test suites to a single suite containing all the individual tests (leaf nodes).
   * @param suite the root of a tree of suites
   * @return a single suite containing all of the individual tests from the given tree of suites.
   */
  public static TestSuite flatten(TestSuite suite) {
    TestSuite result = new TestSuite(suite.getName());
    flattenRecursive(suite, result);
    return result;
  }

  /**
   * Helper for {@link #flatten(TestSuite)}. Recursively adds all the leaf tests from one suite to another.
   * @param suite the test suite being processed
   * @param collector collects the individual tests
   */
  private static void flattenRecursive(TestSuite suite, TestSuite collector) {
    Enumeration tests = suite.tests();
    while (tests.hasMoreElements()) {
      Test test = (Test)tests.nextElement();
      if (test instanceof TestSuite)
        flattenRecursive((TestSuite)test, collector);
      else
        collector.addTest(test);
    }
  }


  /**
   * Factory method for a new {@link TestSuite} that contains the given tests.
   * @param name to be used for the generated suite
   * @param tests to be included in the suite
   * @return a new {@link TestSuite} that contains the given tests
   */
  public static TestSuite combineTests(String name, Test... tests) {
    TestSuite suite = new TestSuite(name);
    for (Test test : tests) {
      suite.addTest(test);
    }
    return suite;
  }

  /**
   * Same as {@link #combineTests(String, Test...)}, but uses the name of the caller's class as the name of the suite.
   * <p>
   * <b>CAUTION</b>: the caller's class name will be derived from {@link Throwable#getStackTrace()}, which may not be what you want.
   *
   * @param tests to be included in the suite
   * @return a new {@link TestSuite} that contains the given tests
   */
  public static TestSuite combineTests(Test... tests) {
    // attempt to infer the caller's class name to use as the name of the suite
    String callerClassName = new Throwable().getStackTrace()[1].getClassName();
    return combineTests(callerClassName, tests);
  }

  /**
   * Adds all tests from several suites to the given target suite.
   *
   * @param target all tests will be added to this suite
   * @param sources the tests from these suites will be added to the target
   * @return the target suite, after all other tests have been merged into it
   */
  public static TestSuite merge(TestSuite target, TestSuite... sources) {
    for (Iterator<Test> it = iterTests(sources); it.hasNext(); ) {
      target.addTest(it.next());
    }
    return target;
  }

  /**
   * Returns a typed {@link Iterator} over all the top-level {@link Test} elements in the given suites.
   * @param testSuites the input suites
   * @return an {@link Iterator} that sequentially iterates the individual {@link Test} elements in the given suites.
   * @see #listTests(TestSuite)
   * @see #walkTestSuiteTree(TestSuite, TestSuiteVisitor)
   */
  public static Iterator<Test> iterTests(TestSuite... testSuites) {
    return Iterators.concat(Arrays.stream(testSuites)
            .map(TestSuiteBuilder::iterTests)
            .iterator());
  }

  /**
   * Returns a typed {@link Iterator} over all the top-level {@link Test} elements in the given suite.
   * @return an {@link Iterator} that sequentially iterates the individual {@link Test} elements in the given suite.
   * @see #listTests(TestSuite)
   * @see #walkTestSuiteTree(TestSuite, TestSuiteVisitor)
   */
  public static Iterator<Test> iterTests(TestSuite suite) {
    return new IndexedIterator<Test>(suite.testCount()) {
      @Override
      protected Test get(int idx) {
        return suite.testAt(idx);
      }
    };
  }

  /**
   * Returns a typed {@link List} view of the {@link Test} elements contained in the given suite.
   * @return an unmodifiable list
   * @see #iterTests(TestSuite)
   * @see #walkTestSuiteTree(TestSuite, TestSuiteVisitor)
   */
  public static List<Test> listTests(TestSuite suite) {
    return new ListAdapter<>(suite::testAt, suite::testCount);
  }


  /**
   * Combines all the tests in the given suites into a single {@link GWTTestSuite}
   * @param name the name of the resulting suite
   * @param testSuites the tests to add to the {@link GWTTestSuite}
   * @return a new {@link GWTTestSuite} containing all the tests in the given suites
   */
  public static GWTTestSuite toGWTTestSuite(String name, TestSuite... testSuites) {
    GWTTestSuite gwtTestSuite = new GWTTestSuite(name);
    for (Iterator<Test> it = iterTests(testSuites); it.hasNext(); ) {
      gwtTestSuite.addTest(it.next());
    }
    prettifyGwtModuleNames(gwtTestSuite);
    return gwtTestSuite;
  }

  /**
   * The default suites for each module in a {@link GWTTestSuite} have an ugly {@code ".JUnit.gwt.xml"} suffix in their
   * names. This method replaces those suffixes with {@code " GWT Module"}
   * <p>Example:
   * {@code "solutions.trsoftware.commons.Commons.JUnit.gwt.xml"} &rarr; {@code "solutions.trsoftware.commons.Commons GWT Module"}
   * </p>
   * @param gwtTestSuite the suite whose names are to be prettified
   * @return the argument that was passed in
   */
  public static GWTTestSuite prettifyGwtModuleNames(GWTTestSuite gwtTestSuite) {
    // give each module suite a prettier name (strip the ".JUnit.gwt.xml" extension to get the actual name of the module)
    for (Enumeration gwtModuleSuites = gwtTestSuite.tests(); gwtModuleSuites.hasMoreElements(); ) {
      TestSuite testSuite = (TestSuite)gwtModuleSuites.nextElement();
      String testSuiteName = testSuite.getName();
      int suffixPos = testSuiteName.indexOf(".JUnit.gwt.xml");
      if (suffixPos >= 0)
        testSuite.setName(testSuiteName.substring(0, suffixPos) + " GWT Module");
    }
    return gwtTestSuite;
  }

  /**
   * Recursively visits all the tests contained in given suite.
   *
   * @param suite root of the test suite tree
   * @param visitor will be invoked for every {@link Test} and {@link TestSuite} contained within the given suite
   * @see #iterTests(TestSuite)
   */
  public static void walkTestSuiteTree(TestSuite suite, TestSuiteVisitor visitor) {
    // pre-order traversal (visit self before children)
    for (Test test : listTests(suite)) {
      if (test instanceof TestSuite) {
        TestSuite nextSuite = (TestSuite)test;
        visitor.preVisitSuite(nextSuite);
        walkTestSuiteTree(nextSuite, visitor);
        visitor.postVisitSuite(nextSuite);
      }
      else  {
        visitor.visitTest(test);
      }
    }
  }

  /**
   * Visitor interface for {@link #walkTestSuiteTree}
   */
  public interface TestSuiteVisitor {
    default void preVisitSuite(TestSuite suite) {}
    default void visitTest(Test test) {}
    default void postVisitSuite(TestSuite suite) {}
  }

}
