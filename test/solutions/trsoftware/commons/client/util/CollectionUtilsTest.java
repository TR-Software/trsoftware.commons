package solutions.trsoftware.commons.client.util;

import static solutions.trsoftware.commons.client.util.CollectionUtils.*;

import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.client.util.callables.Function1;
import solutions.trsoftware.commons.client.util.mutable.MutableInteger;
import solutions.trsoftware.commons.client.util.stats.HashCounter;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.*;

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
        collect(rockers, new Function1<RockNRolla, String>() {
          public String call(RockNRolla parameter) {
            return parameter.name;
          }
        }));
    assertEquals(
        Arrays.asList(bono.position, edge.position, adam.position, larry.position, roger.position, david.position, rick.position, nick.position, syd.position),
        collect(rockers, new Function1<RockNRolla, String>() {
          public String call(RockNRolla parameter) {
            return parameter.position;
          }
        }));
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
    assertEquals(Collections.emptyList(), asList(Collections.emptyList().iterator()));
  }

  public void testGetLast() throws Exception {
    // test with Iterator argument
    assertEquals(null, last(Collections.emptyList().iterator()));
    assertEquals("a", last(strList("a").iterator()));
    assertEquals("b", last(strList("a", "b").iterator()));
    assertEquals("c", last(strList("a", "b", "c").iterator()));
    assertEquals((Integer)1, last(Arrays.asList(1).iterator()));
    assertEquals((Integer)2, last(Arrays.asList(1, 2).iterator()));
    assertEquals((Integer)3, last(Arrays.asList(1, 2, 3).iterator()));

    // test with Iterable argument
    assertEquals(null, last(Collections.emptyList()));
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

}