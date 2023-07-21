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

import com.google.common.collect.Iterators;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.server.io.file.FileUtils;
import solutions.trsoftware.commons.server.util.reflect.ReflectionPredicates;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.collections.DefaultHashSetMap;
import solutions.trsoftware.tools.util.BytecodeParser;

import javax.annotation.Nonnull;
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
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static solutions.trsoftware.commons.shared.util.StringUtils.indent;
import static solutions.trsoftware.junit.TestSuiteBuilder.FilterMode.EXCLUDE;
import static solutions.trsoftware.junit.TestSuiteBuilder.FilterMode.INCLUDE;

/**
 * Builds a suite of tests by scanning filesystem paths for matching {@code .class} files.
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
   * Test classes inheriting from these will be either included in or excluded from the suite,
   * depending on the {@link FilterMode} key.
   */
  private final FilterMap<Class<?>> superclassFilters = new FilterMap<>();
  /**
   * Test methods (or classes) marked with these annotations ({@code @interface}s) will be either included in or
   * excluded from the suite, depending on the {@link FilterMode} key.
   */
  private FilterMap<Class<? extends Annotation>> annotationFilters = new FilterMap<>();

  /**
   * Determines how {@link #superclassFilters} or {@link #annotationFilters} will be used.
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

  private static class FilterMap<T> extends DefaultHashSetMap<FilterMode, T> {
    FilterMap() {
      super(new EnumMap<>(FilterMode.class));
    }

    @SafeVarargs
    final void add(FilterMode mode, T... filters) {
      FilterMode oppositeMode = mode.opposite();
      for (T filter : filters) {
        // make sure this map doesn't already contain the opposite of this filter
        if (get(oppositeMode).contains(filter))
          throw new IllegalStateException(String.format("Cannot %s <%s> because it is already being explicitly %sd",
              mode.prettyName(), filter, oppositeMode.prettyName()));
        get(mode).add(filter);
      }
    }
  }

  /**
   * If not {@code null}, individual tests will be wrapped with a {@link TestTimeBoxDecorator}
   */
  private TestTimeBoxDecorator.TimeBoxSettings timeBoxSettings;

  public TestSuiteBuilder() {
    // TODO: don't do this by default: caller should specify if they want this feature (extract method)
    String packages = System.getProperty("TestSuiteBuilder.packages");
    if (packages != null) {
      Arrays.stream(packages.split(",")).map(String::trim).filter(StringUtils::notBlank)
          .forEach(this::addPackage);
    }
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
   */
  public TestSuiteBuilder addContentRoot(@Nonnull Path path) {
    if (contentRoots.add(Objects.requireNonNull(path, "path"))) {
      LOGGER.info("Adding tests from " + path);
    }
    return this;
  }

  /**
   * Adds the path of the given reference class to the set of content roots to be scanned for classes.
   *
   * @param refClass the location of the corrseponding {@code .class} file on will be used to look for tests to add to the suite
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
    // get the .class file path (compiler output dir) for the given package
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
    return addPackage(classInPackage.getPackage().getName());
  }

  /**
   * Asserts that none of the settings clash with each other.
   * @throws IllegalStateException if the current configuration of this builder violates any constraints
   */
  private void validateSettings() throws IllegalStateException {
    if (useGwtTestSuite && groupByPackage)
      throw new IllegalStateException("useGwtTestSuite and groupByPackage are mutually exclusive");
  }

  /**
   * Same as {@link #buildSuite(String)}, but uses the name of the caller's class as the name of the suite.
   * <p>
   * <b>CAUTION</b>: the caller's class name will be derived from {@link Throwable#getStackTrace()}, which may not be what you want.
   *
   * @return the generated test suite (which might be an instance of {@link GWTTestSuite} if the {@link #useGwtTestSuite}
   * setting was specified).
   *
   * @throws IllegalStateException if the current configuration of this builder violates any constraints (see {@link #validateSettings()})
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
   * @throws IllegalStateException if the current configuration of this builder violates any constraints (see {@link #validateSettings()})
   * @throws IOException any exception thrown while searching for and parsing the test class files.
   */
  public TestSuite buildSuite(String name) throws IllegalStateException, IOException {
    validateSettings();
    TestSuite testSuite = useGwtTestSuite ? new GWTTestSuite(name) : new TestSuite(name);
    Pattern filenamePattern = Pattern.compile(filenameRegex);
    /* TODO(5/29/2023): filter the contentRoots to make sure that nothing is included more than once,
         e.g.  if contentRoots = ["/a/b/c", "/a/b/"], the "/a/b/c" entry is superfluous since already included in "/a/b/"
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
        Class<?> cls = null;
        try {
          String className = BytecodeParser.extractClassName(file.toFile());
          cls = Class.forName(className);
        }
        catch (Exception e) {
          // the file is probably not a valid class; print stack trace and ignore this exception
          e.printStackTrace();
        }
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
        Class<?> cls = null;
        try {
          String className = BytecodeParser.extractClassName(file.toFile());
          cls = Class.forName(className);
        }
        catch (Exception e) {
          // the file is probably not a valid class; print stack trace and ignore this exception
          e.printStackTrace();
        }
        if (cls != null && includeClass(cls)) {
          addIfNotEmpty(rootTestSuite, createTestSuiteForClass(cls));
        }
        return FileVisitResult.CONTINUE;
      }
    });
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
  private LinkedHashMap<Class<?>, TestSuite> testSuitesByClass = new LinkedHashMap<>();

  private TestSuite createTestSuiteForClass(Class<?> testClass) {
    if (testSuitesByClass.containsKey(testClass)) {
      // already have a suite for this class; probably a duplicate contentRoots entry
      return null;
    }
    // leverage the functionality already provided by TestSuite to convert a TestCase into a suite containing its test methods
    TestSuite testSuiteForClass = new TestSuite(testClass);
    // now modify that TestSuite as needed
    SortedMap<String, Test> testsByName = new TreeMap<>();  // alphabetically sort the tests by name
    Enumeration methodTests = testSuiteForClass.tests();
    while (methodTests.hasMoreElements()) {
      /*
      the default test suite generated for a class will contain an entry for each test method, each being an instance
      of that same class (which is almost certainly an instance of TestCase), so the following cast should succeed
      */
      TestCase test = (TestCase)methodTests.nextElement();
      String testName = test.getName();
      Method testMethod;
      try {
        testMethod = testClass.getMethod(testName);
      }
      catch (NoSuchMethodException e) {
        if (test.toString().equals("warning(junit.framework.TestSuite$1)")) {
          /*
          this is a special-case marker inserted by the TestSuite(Class) constructor to warn about
          a problem with the test class (e.g. "No tests found in ..."),
          in which case we should simply add it to our result as-is
          */
          testsByName.put(testName, test);
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
                new TestTimeBoxDecorator(test, testMethod, timeBoxSettings)
                : test);
      }
    }
    testSuiteForClass = new TestSuite(testSuiteForClass.getName());
    for (Test test : testsByName.values()) {
      testSuiteForClass.addTest(test);
    }
    testSuitesByClass.put(testClass, testSuiteForClass);
    return testSuiteForClass;
  }

  /**
   * Subclasses of the given superclasses will be excluded from the suite.
   * @return self, for method chaining
   */
  public TestSuiteBuilder excludeSubclassesOf(Class<?>... superClasses) {
    superclassFilters.add(EXCLUDE, superClasses);
    return this;
  }

  /**
   * Only subclasses of the given superclasses will be included in the suite.
   * @return self, for method chaining
   */
  public TestSuiteBuilder includeOnlySubclassesOf(Class<?>... superClasses) {
    superclassFilters.add(INCLUDE, superClasses);
    return this;
  }

  /**
   * Clears any superclass filters that might have been previously set
   * (via {@link #includeOnlyTestsAnnotatedWith(Class[])} or {@link #excludeTestsAnnotatedWith(Class[])}.
   * @return self, for method chaining
   */
  public TestSuiteBuilder clearSuperclassFilters() {
    superclassFilters.clear();
    return this;
  }

  /**
   * Test methods (or classes) marked with the given annotations will be excluded from the suite.
   * @param annotationClasses an {@code @interface}
   * @return self, for method chaining
   */
  @SafeVarargs
  public final synchronized TestSuiteBuilder excludeTestsAnnotatedWith(Class<? extends Annotation>... annotationClasses) {
    annotationFilters.add(EXCLUDE, annotationClasses);
    return this;
  }

  /**
   * Test methods (or classes) will be included in the suite <b>only if</b> marked with any of the given annotations.
   * @param annotationClasses an {@code @interface}
   * @return self, for method chaining
   */
  @SafeVarargs
  public final synchronized TestSuiteBuilder includeOnlyTestsAnnotatedWith(Class<? extends Annotation>... annotationClasses) {
    annotationFilters.add(INCLUDE, annotationClasses);
    return this;
  }

  /**
   * Clears any annotation filters that might have been previously set
   * (via {@link #includeOnlyTestsAnnotatedWith(Class[])} or {@link #excludeTestsAnnotatedWith(Class[])}.
   * @return self, for method chaining
   */
  public TestSuiteBuilder clearAnnotationFilters() {
    annotationFilters.clear();
    return this;
  }

  /**
   * Decides whether to include a test class or method in the suite based on present or absent annotations.
   * @param annotatedElement the {@link TestCase} subclass, or the original method corresponding to a test in such a class.
   * @return {@code true} iff the given class or test method should be included in the suite
   * @see #annotationFilters
   * @see FilterMode
   */
  boolean applyAnnotationFilters(AnnotatedElement annotatedElement) {  // exposed with package-private visibility for unit testing
    Predicate<AnnotatedElement> excludePredicate = annotationFilters.get(EXCLUDE).stream()
        .map(ReflectionPredicates::mustHaveDeclaredAnnotation)
        .reduce(Predicate::or)
        .orElse(x -> false);

    if (annotatedElement instanceof Class) {
      /*
      for classes, test only the exclusion rules
      (because some of its methods might have a required annotation even if the class itself does not)
      */
      return !excludePredicate.test(annotatedElement);
    }
    else {
      // for methods, we test both inclusion and exclusion rules,
      assert annotatedElement instanceof Method;
      Method method = (Method)annotatedElement;
      Predicate<AnnotatedElement> includePredicate = annotationFilters.get(INCLUDE).stream()
          .map(ReflectionPredicates::mustHaveDeclaredAnnotation)
          .reduce(Predicate::or)
          .orElse(x -> true);
      // for methods we also want to consider the annotations present on the method's declaring class
      Class<?> declaringClass = method.getDeclaringClass();
      return (includePredicate.test(method) || includePredicate.test(declaringClass)) &&
          !(excludePredicate.test(method) || excludePredicate.test(declaringClass));

    }
  }

  /**
   * Decides whether to include the given test class in the suite based on the {@link #superclassFilters}
   * @param cls a test class ({@link TestCase})
   * @return {@code true} iff the given class should be included in the suite
   */
  boolean applySuperclassFilters(Class<?> cls) {  // exposed with package-private visibility for unit testing
    Predicate<Class<?>> includePredicate = superclassFilters.get(INCLUDE).stream()
              .map(ReflectionPredicates::mustBeSubclassOf)
              .reduce(Predicate::or)
              .orElse(x -> true);
    Predicate<Class<?>> excludePredicate = superclassFilters.get(EXCLUDE).stream()
              .map(ReflectionPredicates::mustBeSubclassOf)
              .reduce(Predicate::or)
              .orElse(x -> false);
    return includePredicate.test(cls) && !excludePredicate.test(cls);
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
            && TestCase.class.isAssignableFrom(cls) && applySuperclassFilters(cls) && applyAnnotationFilters(cls);
    // may override to provide additional filters
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TestSuiteBuilder{");
    sb.append("superclassFilters=").append(superclassFilters);
    sb.append(", annotationFilters=").append(annotationFilters);
    sb.append(", filenameRegex='").append(filenameRegex).append('\'');
    sb.append(", contentRoots=").append(contentRoots);
    sb.append(", groupByPackage=").append(groupByPackage);
    sb.append(", useGwtTestSuite=").append(useGwtTestSuite);
    sb.append(", timeBoxSettings=").append(timeBoxSettings);
    sb.append('}');
    return sb.toString();
  }

  /**
   * Recursively prints the tests in a tree of test suites.
   *
   * @param suite the suite whose tests are to be printed
   */
  public static void printTestSuite(TestSuite suite) {
    String heading = String.format("------------ TestSuite(\"%s\"): ------------", suite.getName());
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
   * Constructs a typed {@link Iterator} over all {@link Test} elements in the given suites.
   * @param testSuites the input suites
   * @return an {@link Iterator} that sequentially iterates the individual {@link Test} elements in the given suites.
   */
  public static Iterator<Test> iterTests(TestSuite... testSuites) {
    @SuppressWarnings("unchecked")
    List<Iterator<Test>> iterators = (List<Iterator<Test>>)Arrays.stream(testSuites)
        .map(TestSuite::tests)
        .map(Iterators::forEnumeration)
        .collect(Collectors.toList());
    return Iterators.concat(iterators.iterator());
  }

  public static GWTTestSuite toGWTTestSuite(String name, TestSuite... testSuites) {
    GWTTestSuite gwtTestSuite = new GWTTestSuite(name);
    for (Iterator<Test> it = iterTests(testSuites); it.hasNext(); ) {
      gwtTestSuite.addTest(it.next());
    }
    prettifyGwtModuleNames(gwtTestSuite);
    return gwtTestSuite;
  }

  /**
   * The suites for each module in a {@link GWTTestSuite} have an ugly {@code ".JUnit.gwt.xml"} suffix in their
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

}
