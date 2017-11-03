package solutions.trsoftware.commons.client.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Alex, 1/9/14
 */
public abstract class SetUtils {

  /**
   * @return A new set that represents the "asymmetric set difference" of the two sets.  Neither arg is modified.
   */
  public static <T> Set<T> difference(Set<T> s1, Set<T> s2) {
    s1 = newSet(s1);
    s1.removeAll(s2);
    return s1;
  }

  /**
   * @return A new set that represents the intersection of the two sets.  Neither arg is modified.
   */
  public static <T> Set<T> intersection(Set<T> s1, Set<T> s2) {
    s1 = newSet(s1);
    s1.retainAll(s2);
    return s1;
  }
  
  /**
   * @return A new set that represents the union of the two sets.  Neither arg is modified.
   */
  public static <T> Set<T> union(Set<T> s1, Set<T> s2) {
    s1 = newSet(s1);
    s1.addAll(s2);
    return s1;
  }

  /** Returns a new set initialized from the given collection */
  public static <T> LinkedHashSet<T> newSet(Collection<T> col) {
    return new LinkedHashSet<T>(col);
  }

  /** Returns a new set initialized from the given collection */
  public static <T> LinkedHashSet<T> newSet(T... items) {
    return CollectionUtils.addAll(new LinkedHashSet<T>(), items);
  }

  /** Returns a new set initialized from the given iterator */
  public static <T> LinkedHashSet<T> newSet(Iterator<T> it) {
    LinkedHashSet<T> ret = new LinkedHashSet<T>();
    while (it.hasNext())
      ret.add(it.next());
    return ret;
  }

  /** Returns a new set of strings parsed from a comma-separated string. The inverse of {@link #print(Set)} */
  public static LinkedHashSet<String> parse(String csv) {
    return new LinkedHashSet<String>(StringUtils.splitAndTrim(csv, ","));
  }

  /**
   * Prints the elements of the given set as comma separated strings. The opposite of {@link #parse(String)}.
   */
  public static String print(Set set) {
    return StringUtils.join(",", set);
  }
}
