package solutions.trsoftware.commons.server.memquery.util;

import solutions.trsoftware.commons.client.util.Pair;
import solutions.trsoftware.commons.server.memquery.HasName;
import solutions.trsoftware.commons.server.memquery.HasValue;
import solutions.trsoftware.commons.server.memquery.RelationSchema;

import java.util.*;

/**
 * @author Alex, 1/9/14
 */
public abstract class NameUtils {

  public static final String SEPARATOR = ".";

  /**
   * @return the set of the unique names among the given named objects.
   */
  public static Set<String> getUniqueNames(Iterable<? extends HasName> namedObjects) {
    return new LinkedHashSet<String>(getNames(namedObjects));
  }

  /**
   * @return the names of the given named objects.
   */
  public static List<String> getNames(Iterable<? extends HasName> namedObjects) {
    ArrayList<String> ret = new ArrayList<String>();
    for (HasName namedObject : namedObjects)
      ret.add(namedObject.getName());
    return ret;
  }

  /**
   * @return the the attribute names of the given relation schema with each one prefixed by the relation's name
   * followed by a dot ('.')
   */
  public static List<String> getPrefixedNames(RelationSchema schema) {
    String prefix = schema.getName() + SEPARATOR;
    ArrayList<String> ret = new ArrayList<String>();
    for (String name : schema.getColNames())
      ret.add(prefix + name);
    return ret;
  }

  /**
   * @return a pair containing 1) the partial path up to the last element and 2) the last element of the given path.
   */
  public static Pair<String, String> splitPrefixedName(String name) {
    int i = name.lastIndexOf(SEPARATOR);
    return new Pair<String, String>(name.substring(0, i), name.substring(i));
  }

  /**
   * @return an ordered mapping of the given objects by their names
   */
  public static <T extends HasName> Map<String, T> mapByName(Iterable<T> namedObjects) {
    LinkedHashMap<String, T> ret = new LinkedHashMap<String, T>();
    for (T namedObject : namedObjects)
      ret.put(namedObject.getName(), namedObject);
    return ret;
  }

  /**
   * @return an ordered mapping of the names of the given objects to their ordinal numbers.
   */
  public static Map<String, Integer> mapNamesToOrdinals(Iterable<? extends HasName> namedObjects) {
    LinkedHashMap<String, Integer> ret = new LinkedHashMap<String, Integer>();
    int i = 0;
    for (HasName namedObject : namedObjects)
      ret.put(namedObject.getName(), i++);
    return ret;
  }

  /**
   * @return an ordered mapping of the names of the given objects to their ordinal numbers.
   */
  public static <T extends HasName & HasValue<V>, V> Map<String, V> mapNamesToValues(Iterable<T> namedObjects) {
    LinkedHashMap<String, V> ret = new LinkedHashMap<String, V>();
    for (T namedObject : namedObjects)
      ret.put(namedObject.getName(), namedObject.get());
    return ret;
  }
}
