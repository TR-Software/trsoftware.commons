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

package solutions.trsoftware.commons.shared.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import solutions.trsoftware.commons.shared.util.callables.Function1;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static solutions.trsoftware.commons.shared.util.CollectionUtils.*;
import static solutions.trsoftware.commons.shared.util.StringUtils.ASCII_PRINTABLE_CHARS;

/**
 * Jun 8, 2009
 *
 * @author Alex
 */
public class CollectionUtilsTest extends TestCase {

  private class RockNRolla {
    private String name;
    private String band;
    private String position;

    private RockNRolla(String name, String position, String band) {
      this.band = band;
      this.name = name;
      this.position = position;
    }
  }

  private RockNRolla bono, edge, adam, larry, roger, david, rick, nick, syd;
  private List<RockNRolla> rockers;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    rockers = Arrays.asList(
        bono = new RockNRolla("Bono", "vocals", "U2"),
        edge = new RockNRolla("The Edge", "guitar", "U2"),
        adam = new RockNRolla("Adam Clayton", "bass", "U2"),
        larry = new RockNRolla("Larry Mullen", "drums", "U2"),
        roger = new RockNRolla("Roger Waters", "bass", "Pink Floyd"),
        david = new RockNRolla("David Gilmour", "guitar", "Pink Floyd"),
        rick = new RockNRolla("Richard Wright", "keyboards", "Pink Floyd"),
        nick = new RockNRolla("Nick Mason", "drums", "Pink Floyd"),
        syd = new RockNRolla("Syd Barret", "guitar", "Pink Floyd")
    );
  }

  public void testCollect() throws Exception {
    assertEquals(
        Arrays.asList(bono.name, edge.name, adam.name, larry.name, roger.name, david.name, rick.name, nick.name, syd.name),
        collect(rockers, parameter -> parameter.name));
    assertEquals(
        Arrays.asList(bono.position, edge.position, adam.position, larry.position, roger.position, david.position, rick.position, nick.position, syd.position),
        collect(rockers, parameter -> parameter.position));
  }

  public void testBuildIndex() throws Exception {
    // 1) test indexing by band name
    {
      Map<String, List<RockNRolla>> bandIndex = buildIndex(rockers, new Function1<RockNRolla, String>() {
        public String call(RockNRolla parameter) {
          return parameter.band;
        }
      });
      assertEquals(2, bandIndex.size());
      assertEquals(Arrays.asList(bono, edge, adam, larry), bandIndex.get("U2"));
      assertEquals(Arrays.asList(roger, david, rick, nick, syd), bandIndex.get("Pink Floyd"));
    }

    // 2) test indexing by position
    {
      Map<String, List<RockNRolla>> positionIndex = buildIndex(rockers, new Function1<RockNRolla, String>() {
        public String call(RockNRolla parameter) {
          return parameter.position;
        }
      });
      assertEquals(5, positionIndex.size());
      assertEquals(Arrays.asList(bono), positionIndex.get("vocals"));
      assertEquals(Arrays.asList(edge, david, syd), positionIndex.get("guitar"));
      assertEquals(Arrays.asList(adam, roger), positionIndex.get("bass"));
      assertEquals(Arrays.asList(larry, nick), positionIndex.get("drums"));
      assertEquals(Arrays.asList(rick), positionIndex.get("keyboards"));
    }
  }

  public void testRemoveMatchingEntries() throws Exception {
    Predicate<String> matchB = new Predicate<String>() {
      public boolean apply(String item) {
        return "b".equals(item);
      }
    };
    assertEquals(ListUtils.arrayList("a", "c"), removeMatchingEntries(
        ListUtils.arrayList("a", "b", "c"),
        matchB));

    // test with an empty list
    assertEquals(ListUtils.<String>arrayList(), removeMatchingEntries(
        ListUtils.<String>arrayList(),
        matchB));
  }


  public void testIteratorToList() throws Exception {
    assertEquals(strList("a", "b", "c"), asList(strList("a", "b", "c").iterator()));
    assertEquals(emptyList(), asList(emptyList().iterator()));
  }

  public void testAddFromSupplier() throws Exception {
    class IntSequenceSupplier implements Supplier<Integer> {
      // generates an infinite sequence of consecutive integers
      private int next;

      IntSequenceSupplier() {
      }

      IntSequenceSupplier(int start) {
        this.next = start;
      }

      @Override
      public Integer get() {
        return next++;
      }
    }
    {
      // 1) test some edge cases
      // 1.a) n <= 0: should not invoke the supplier
      IntSequenceSupplier supplier = new IntSequenceSupplier();
      ArrayList<Integer> list = new ArrayList<>();
      for (int n = -5; n <= 0; n++) {
        addFromSupplier(list, n, supplier);
        assertTrue(list.isEmpty());  // no elements added
        assertEquals(0, supplier.next);  // supplier not invoked
      }
      // 1.b) n = 1:
      addFromSupplier(list, 1, supplier);
      assertEquals(Collections.singletonList(0), list);
      assertEquals(1, supplier.next);
    }
    {
      // 2) compare CollectionUtils.addFromSupplier with some other implementations of the same concept
      // 2.a) starting with an empty collection
      IntSequenceSupplier supplier = new IntSequenceSupplier();
      ArrayList<Integer> list = new ArrayList<>();
      ArrayList<Integer> expected = asList(new NumberRange<>(0, 9));
      assertEquals(expected, addFromSupplier(list, 10, supplier));
      assertEquals(expected, Stream.generate(new IntSequenceSupplier()).limit(10).collect(Collectors.toList()));
      // 2.b) appending to an existing collection
      Stream.generate(new IntSequenceSupplier(10)).limit(3).forEach(expected::add);
      assertEquals(expected, addFromSupplier(list, 3, supplier));
    }
  }

  /**
   * @deprecated because {@link CollectionUtils#last} is deprecated
   */
  @Deprecated
  public void testGetLast() throws Exception {
    // test with Iterator argument
    assertEquals(null, last(emptyList().iterator()));
    assertEquals("a", last(strList("a").iterator()));
    assertEquals("b", last(strList("a", "b").iterator()));
    assertEquals("c", last(strList("a", "b", "c").iterator()));
    assertEquals((Integer)1, last(Arrays.asList(1).iterator()));
    assertEquals((Integer)2, last(Arrays.asList(1, 2).iterator()));
    assertEquals((Integer)3, last(Arrays.asList(1, 2, 3).iterator()));

    // test with Iterable argument
    assertEquals(null, last(emptyList()));
    assertEquals("a", last(strList("a")));
    assertEquals("b", last(strList("a", "b")));
    assertEquals("c", last(strList("a", "b", "c")));
    assertEquals((Integer)1, last(Arrays.asList(1)));
    assertEquals((Integer)2, last(Arrays.asList(1, 2)));
    assertEquals((Integer)3, last(Arrays.asList(1, 2, 3)));
  }

  private static List<String> strList(String... args) {
    return Arrays.asList(args);
  }

  public void testToStringArray() throws Exception {
    assertTrue(Arrays.equals(new String[0], toStringArray(Arrays.<String>asList())));
    assertTrue(Arrays.equals(new String[]{"foo"}, toStringArray(strList("foo"))));
    assertTrue(Arrays.equals(new String[]{"foo", "bar"}, toStringArray(strList("foo", "bar"))));
    assertTrue(Arrays.equals(new String[0], toStringArray(Arrays.<Integer>asList())));
    assertTrue(Arrays.equals(new String[]{"1"}, toStringArray(Arrays.asList(1))));
    assertTrue(Arrays.equals(new String[]{"1", "2"}, toStringArray(Arrays.asList(1, 2))));
  }

  public void testContainsAny() throws Exception {
    assertFalse(containsAny(Collections.<String>emptySet(), strList("a", "b", "c")));
    assertFalse(containsAny(strList("a", "b", "c"), Collections.<String>emptySet()));
    assertFalse(containsAny(strList("a", "b"), strList("c")));
    assertTrue(containsAny(strList("a", "b", "c"), strList("a", "b", "c")));
    assertTrue(containsAny(strList("a"), strList("a", "b", "c")));
    assertTrue(containsAny(strList("a", "b", "c"), strList("c")));
  }
  
  public void testConcat() throws Exception {
    assertEquals(strList(), CollectionUtils.<String>concat());
    assertEquals(strList(), concat(strList()));
    assertEquals(strList(), concat(strList(), strList()));
    assertEquals(strList("a"), concat(strList("a")));
    assertEquals(strList("a"), concat(strList("a"), strList()));
    assertEquals(strList("a", "b", "c"), concat(strList("a"), strList(), strList("b", "c"), strList()));
  }

  public void testFilter() {
    assertEquals(new ArrayList(), filter(new ArrayList<Integer>(), Predicates.alwaysTrue()));
    assertEquals(Arrays.asList("foo", "bar", "baz"), filter(Arrays.asList("foo", "bar", "baz"), Predicates.alwaysTrue()));
    assertEquals(new ArrayList(), filter(new ArrayList<Integer>(), Predicates.alwaysFalse()));
    assertEquals(new ArrayList(), filter(Arrays.asList("foo", "bar", "baz"), Predicates.alwaysFalse()));
    assertEquals(
        Arrays.asList("foo", "bar", "baz"),
        filter(Arrays.asList("foo", "a", "bar", "cigar", "baz"),
            new Predicate<String>() {
              public boolean apply(String item) {
                return item.length() == 3;
              }
            })
    );
  }

  public void testPrintTotalOrdering() throws Exception {
    assertEquals("1", printTotalOrdering(Collections.<Integer>singletonList(1)));
    assertEquals("1 == 1", printTotalOrdering(Arrays.asList(1, 1)));
    assertEquals("1 < 2", printTotalOrdering(Arrays.asList(1, 2)));
    assertEquals("1 < 2", printTotalOrdering(Arrays.asList(2, 1)));
    assertEquals("1 < 2 < 3 == 3 < 4 < 5", printTotalOrdering(Arrays.asList(5, 2, 4, 3, 3, 1)));
  }

  public void testSortedCopy() throws Exception {
    // generate a list of random strings
    List<String> data = randomStrings(20);
    // ensure that the data is not already sorted (this is very unlikely)
    while (isSorted(data)) {
      Collections.shuffle(data, RandomUtils.rnd);
    }
    ArrayList<String> dataCopy = new ArrayList<>(data);
    assertFalse(isSorted(data));
    assertEquals(data, dataCopy);  // sanity check
    ArrayList<String> sortedData = sortedCopy(data);
    assertNotSame(data, sortedData);  // a defensive copy should've been created
    assertTrue(isSorted(sortedData));  // the result should be sorted
    assertEquals(dataCopy, data);  // make sure the original list wasn't mutated

    // make sure it works for a collection that's already sorted
    assertEquals(sortedData, sortedCopy(sortedData));

    // make sure it works for a collection containing duplicate values
    assertTrue(isSorted(sortedCopy(Arrays.asList(-1, 2, 3, 3, 3, 15))));
    assertTrue(isSorted(sortedCopy(Arrays.asList(-1, -1, 3, 3, 3, 15))));
    assertTrue(isSorted(sortedCopy(Arrays.asList(1, 15, 15, 3))));
    assertTrue(isSorted(sortedCopy(Arrays.asList(-1, -1, 15, 3))));

    // now try passing a different collection type to sorted() to be sure it doesn't just work for lists
    assertTrue(isSorted(sortedCopy(new HashSet<>(data))));
    assertTrue(isSorted(sortedCopy(new LinkedHashSet<>(data))));
    assertTrue(isSorted(sortedCopy(new TreeSet<>(data))));

    // TODO: (probably overkill) test the overload version that takes a Comparator?
  }

  /**
   * @return a list containing the given number of random (ASCII) strings, each between 1 and 10 chars long
   * @see RandomUtils#randString(String, int, int)
   */
  // TODO: move to TestData?
  public static List<String> randomStrings(int n) {
    return Stream.generate(() -> RandomUtils.randString(ASCII_PRINTABLE_CHARS, 1, 10))
        .limit(n).collect(Collectors.toList());
  }

  public void testIsSorted() throws Exception {
    // 1) test the trivial cases first
    // an empty collection should be considered sorted
    assertTrue(isSorted(Collections.<Integer>emptyList()));  // NOTE: have to use a generic type that implements Comparable<T>
    assertTrue(isSorted(Collections.<String>emptySet()));
    // same with a collection containing only 1 element
    assertTrue(isSorted(singletonList(1)));
    assertTrue(isSorted(Collections.singleton("foo")));
    // 2) test some basic examples manually
    assertTrue(isSorted(SetUtils.newSet("", "a", "foo", "foos")));  // NOTE: Sets are supported too because they might be ordered (like LHS or SortedSet)
    assertFalse(isSorted(SetUtils.newSet("a", "", "foo", "foos")));
    // a list with duplicate values
    assertTrue(isSorted(Arrays.asList(-1, 2, 3, 3, 3, 15)));
    assertTrue(isSorted(Arrays.asList(-1, -1, 3, 3, 3, 15)));
    assertFalse(isSorted(Arrays.asList(1, 15, 15, 3)));
    assertFalse(isSorted(Arrays.asList(-1, -1, 15, 3)));
    // 3) test some large inputs
    List<Integer> intList = IntStream.rangeClosed(-50, 185).boxed().collect(Collectors.toList());
    assertTrue(isSorted(intList));
    assertTrue(isSorted(new LinkedHashSet<>(intList)));
    assertTrue(isSorted(new TreeSet<>(intList)));
    ArrayList<Integer> reversedIntList = reversedCopy(intList);
    assertFalse(isSorted(reversedIntList));
    assertFalse(isSorted(new LinkedHashSet<>(reversedIntList)));
    // 4) test that the arg is not structurally modified
    List<MockInteger> mockInts = IntStream.rangeClosed(-50, 185).mapToObj(MockInteger::new).collect(Collectors.toList());
    List<MockInteger> mockIntsCopy = new ArrayList<>(mockInts);
    assertEquals(mockInts, mockIntsCopy);
    assertTrue(isSorted(mockInts));
    assertEquals(mockIntsCopy, mockInts);  // no structural modifications to the arg
  }

  public void testReversedCopy() throws Exception {
    // test some trivial cases:
    assertTrue(reversedCopy(emptyList()).isEmpty());
    assertTrue(reversedCopy(emptySet()).isEmpty());
    assertTrue(reversedCopy(SetUtils.newSortedSet()).isEmpty());
    assertEquals(singletonList(1), reversedCopy(singletonList(1)));
    assertEquals(singletonList(1), reversedCopy(singleton(1)));
    assertEquals(singletonList(1), reversedCopy(singletonList(1)));
    assertEquals(singletonList(1), reversedCopy(singleton(1)));
    assertEquals(singletonList(1), reversedCopy(SetUtils.newSortedSet(1)));
    assertEquals(Arrays.asList(2, 1), reversedCopy(Arrays.asList(1, 2)));
    assertEquals(Arrays.asList(2, 1), reversedCopy(SetUtils.newSet(1, 2)));
    assertEquals(Arrays.asList(2, 1), reversedCopy(SetUtils.newSortedSet(1, 2)));

    // now test that it returns a copy without structurally modifying the arg
    List<String> strings = randomStrings(20);
    List<String> stringsCopy = new ArrayList<>(strings);
    assertEquals(strings, stringsCopy);
    List<String> expected = new ArrayList<>(strings);
    Collections.reverse(expected);
    ArrayList<String> actual = reversedCopy(strings);
    assertEquals(expected, actual);  // reversed as expected
    assertNotSame(strings, actual);  // defensive copy was made
    assertEquals(stringsCopy, strings);  // original list unmodified
  }

  /**
   * Helper class for {@link #testIsSorted()}: wraps an int, and uses it in its {@link Comparable},
   * but purposefully omits {@link #equals(Object)} and {@link #hashCode()}, to allow checking that a collection arg
   * was not modified by an operation.
   */
  private static class MockInteger implements Comparable<MockInteger> {
    private int i;

    public MockInteger(int i) {
      this.i = i;
    }

    @Override
    public int compareTo(@NotNull MockInteger o) {
      return Integer.compare(i, o.i);
    }

  }

}