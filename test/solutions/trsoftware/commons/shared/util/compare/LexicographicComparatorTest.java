package solutions.trsoftware.commons.shared.util.compare;

import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Strings.lenientFormat;
import static solutions.trsoftware.commons.shared.util.MathUtils.signum;

/**
 * @author Alex
 * @since 4/17/2025
 */
public class LexicographicComparatorTest extends BaseTestCase {

  public void testCompare() {
    testCompare("", "");
    testCompare("", "a");
    testCompare("a", "a");
    testCompare("a", "ab");
    testCompare("ab", "a");
    testCompare("ab", "abcd");
    testCompare("abcd", "ab");
  }

  private void testCompare(String s1, String s2) {
    // verify that comparing lists of chars from the given strings matches the String.compare for the strings
    List<Character> l1 = StringUtils.toCharacterCollection(s1, ArrayList::new);
    List<Character> l2 = StringUtils.toCharacterCollection(s2, ArrayList::new);
    Comparator<String> sComparator = Comparator.naturalOrder();
    LexicographicComparator<Character> lComparator = new LexicographicComparator<Character>(Comparator.naturalOrder());
    assertEquals(lenientFormat("compare(%s, %s)", l1, l2),
        signum(sComparator.compare(s1, s2)),
        signum(lComparator.compare(l1, l2)));
    // TODO: test with Comparator.<Character>reverseOrder();
  }
}