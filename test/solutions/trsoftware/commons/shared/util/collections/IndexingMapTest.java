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

package solutions.trsoftware.commons.shared.util.collections;

import com.google.common.collect.Sets;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.*;

public class IndexingMapTest extends TestCase {

  private enum IndexSpec {
    A, B, C
  }

  private interface Indexable {
    Object get(IndexSpec indexSpec);
  }

  private static <K, V extends Indexable> IndexingMap<K, V, IndexSpec> createIndexingMap() {
    return new IndexingMap<K, V, IndexSpec>(IndexSpec.class, new IndexingMap.HashIndexer<K, V, IndexSpec>() {
      @Override
      public Object computeIndexValue(IndexSpec indexSpec, K key, V value) {
        return value.get(indexSpec);
      }
    });
  }

  public void testWith1Index() throws Exception {
    Tester<String, IndexableA> tester = new Tester<String, IndexableA>();
    // add all the negative expectations
    for (IndexSpec indexSpec : EnumSet.of(IndexSpec.B, IndexSpec.C)) {
      for (Object value : new Object[]{"a1", "a2", "foo", null}) {
        tester.setExpectation(indexSpec, value);
      }
    }
    tester.setExpectation(IndexSpec.A, null)
        .setExpectation(IndexSpec.A, "foo")
        .setExpectation(IndexSpec.A, "a2")
        .check();
    // add "k1"
    tester.put("k1", new IndexableA("a1"))
        .setExpectation(IndexSpec.A, "a1", "k1")
        .check();
    // add "k2", with the same value for A
    tester.put("k2", new IndexableA("a1"))
        .setExpectation(IndexSpec.A, "a1", "k1", "k2")
        .check();
    // replace "k2" with a different value for A
    tester.put("k2", new IndexableA("a2"))
        .setExpectation(IndexSpec.A, "a1", "k1")
        .setExpectation(IndexSpec.A, "a2", "k2")
        .check();
    // add another entry
    tester.put("k3", new IndexableA("a1"))
        .setExpectation(IndexSpec.A, "a1", "k1", "k3")
        .setExpectation(IndexSpec.A, "a2", "k2")
        .check();
    tester.remove("k2")
        .setExpectation(IndexSpec.A, "a2")
        .check();
    tester.clear()
        .setExpectation(IndexSpec.A, "a1")
        .check();
  }


  public void testWith2Indexes() throws Exception {
    Tester<String, IndexableAC> tester = new Tester<String, IndexableAC>();
    // add all the negative expectations
    for (Object value : new Object[]{"a1", "a2", "foo", null}) {
      tester.setExpectation(IndexSpec.B, value);
    }
    for (IndexSpec indexSpec : EnumSet.of(IndexSpec.A, IndexSpec.C)) {
      tester.setExpectation(indexSpec, null)
          .setExpectation(indexSpec, "foo")
          .setExpectation(indexSpec, "a2")
          .check();
    }
    // add "k1"
    tester.put("k1", new IndexableAC("a1", "c1"))
        .setExpectation(IndexSpec.A, "a1", "k1")
        .setExpectation(IndexSpec.C, "c1", "k1")
        .check();
    // add "k2", with the same value for A
    tester.put("k2", new IndexableAC("a1", "c2"))
        .setExpectation(IndexSpec.A, "a1", "k1", "k2")
        .setExpectation(IndexSpec.C, "c2", "k2")
        .check();
    // replace "k2" with a different value for A
    tester.put("k2", new IndexableAC("a2", "c2"))
        .setExpectation(IndexSpec.A, "a1", "k1")
        .setExpectation(IndexSpec.A, "a2", "k2")
        .check();
    // add another entry
    tester.put("k3", new IndexableAC("a1", "c3"))
        .setExpectation(IndexSpec.A, "a1", "k1", "k3")
        .setExpectation(IndexSpec.C, "c3", "k3")
        .check();
    tester.remove("k2")
        .setExpectation(IndexSpec.A, "a2")
        .setExpectation(IndexSpec.C, "c2")
        .check();
    tester.clear()
        .setExpectation(IndexSpec.A, "a1")
        .setExpectation(IndexSpec.A, "a2")
        .setExpectation(IndexSpec.A, "a3")
        .setExpectation(IndexSpec.C, "c1")
        .setExpectation(IndexSpec.C, "c2")
        .setExpectation(IndexSpec.C, "c3")
        .check();
  }


  private static class Expectation {
    private final IndexSpec indexSpec;
    private final Object value;

    public Expectation(IndexSpec indexSpec, Object value) {
      this.indexSpec = indexSpec;
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Expectation that = (Expectation)o;

      if (indexSpec != that.indexSpec) return false;
      if (value != null ? !value.equals(that.value) : that.value != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = indexSpec.hashCode();
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
    }
  }

  private static class Tester<K, V extends Indexable> {
    /** The instance being tested */
    private IndexingMap<K, V, IndexSpec> actual = createIndexingMap();
    /** What we expect {@link #actual} to contain */
    private Map<K, V> expected = new HashMap<K, V>();

    private LinkedHashMap<Expectation, Collection<K>> expectations = new LinkedHashMap<Expectation, Collection<K>>();

    public Tester() {
      assertExpectedContents();
    }

    private Tester<K, V> setExpectation(IndexSpec indexSpec, Object value, K... expectedMatches) {
      Collection<K> expectedResults = expectedMatches == null
          ? Collections.<K>emptySet()
          : Sets.newHashSet(expectedMatches);
      expectations.put(new Expectation(indexSpec, value), expectedResults);
      return this;
    }

    private Tester<K, V> removeExpectation(IndexSpec indexSpec, Object value) {
      expectations.remove(new Expectation(indexSpec, value));
      return this;
    }

    public Tester<K, V> put(K key, V value) {
      return compareResults(expected.put(key, value), actual.put(key, value));
    }

    public Tester<K, V> remove(Object key) {
      return compareResults(expected.remove(key), actual.remove(key));
    }

    public Tester<K, V> clear() {
      expected.clear();
      actual.clear();
      return assertExpectedContents();
    }

    private Tester<K, V> compareResults(V expected, V actual) {
      assertEquals(expected, actual);
      assertExpectedContents();
      return this;
    }

    private Tester<K, V> assertExpectedContents() {
      assertEquals(expected, actual);  // this checks pretty much all other Map operations that we're not already checking - size, iterators, etc.
      assertTrue(actual.sanityCheck());
      return this;
    }

    public void check() {
      System.out.println("assertExpectations(" + expected.toString() + "):");
      for (Map.Entry<Expectation, Collection<K>> expectationEntry : expectations.entrySet()) {
        Expectation expectation = expectationEntry.getKey();
        Collection<K> expectedResults = expectationEntry.getValue();
        Collection<K> results = actual.findKeys(expectation.indexSpec, expectation.value);
        System.out.println("  " + StringUtils.methodCallToStringWithResult("findKeys", results, expectation.indexSpec, expectation.value));
        assertEquals(expectedResults, results);
      }
    }
  }

  private static class BaseIndexable implements Indexable {
    protected EnumMap<IndexSpec, Object> values = new EnumMap<IndexSpec, Object>(IndexSpec.class);

    @Override
    public Object get(IndexSpec indexSpec) {
      return values.get(indexSpec);
    }

    @Override
    public String toString() {
//      return new StringBuilder(getClass().getSimpleName()).append('(').append(values).append(')').toString();
      return values.toString();
    }
  }

  /** Dummy value class that supports only {@link IndexSpec#A} */
  private static class IndexableA extends BaseIndexable {
    private IndexableA(Object a) {
      values.put(IndexSpec.A, a);
    }
  }

  /** Dummy value class that supports only {@link IndexSpec#A} */
  private static class IndexableAC extends BaseIndexable {
    private IndexableAC(Object a, Object c) {
      values.put(IndexSpec.A, a);
      values.put(IndexSpec.C, c);
    }
  }
}