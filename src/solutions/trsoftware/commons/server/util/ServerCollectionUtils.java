package solutions.trsoftware.commons.server.util;

import java.util.*;

/**
 * Date: Jun 12, 2008 Time: 4:20:29 PM
 *
 * @author Alex
 */
public class ServerCollectionUtils {

  /** Returns the powerset (the set of all subsets) of the given set */
  public static <T> Set<Set<T>> powerset(Set<T> set) {
    // there is a simpler recursive alg, but we use iteration here for speed
    ArrayList<T> elements = new ArrayList<T>(set);
    LinkedHashSet<Set<T>> powerset = new LinkedHashSet<Set<T>>();

    int powersetSize = 2 << set.size() - 1; // 2^n
    for (int i = 0; i < powersetSize; i++) {
      // take the binary for of i (e.g. 1001) and create a member set with those elts (e.g. {3, 0})
      LinkedHashSet<T> memberSet = new LinkedHashSet<T>();  // the i-th member set
      for (int j = 0; j < elements.size(); j++) {
        if (((i >> j) & 1) != 0)
          memberSet.add(elements.get(j));
      }
      powerset.add(memberSet);
    }

    return powerset;
  }

  private static final Random rnd = new Random();

  /** Returns a random element from the collection */
  public static <T> T randomElement(Collection<T> collection) {
    int index = rnd.nextInt(collection.size());

    if (collection instanceof List && collection instanceof RandomAccess)
      return ((List<T>)collection).get(index);

    // not a random access list - use this, slower seek
    Iterator<T> iter = collection.iterator();
    while (index > 0) {
      iter.next();
      index--;
    }
    return iter.next();
    
    // NOTE: there's a more concise approach, but 50% slower:
    //    return new ArrayList<T>(collection).get(rnd.nextInt(collection.size()));
  }


}
