/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.memquery.util;

import solutions.trsoftware.commons.server.memquery.HasName;
import solutions.trsoftware.commons.server.memquery.HasValue;
import solutions.trsoftware.commons.server.memquery.RelationSchema;
import solutions.trsoftware.commons.shared.util.Pair;

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
    return new LinkedHashSet<>(getNames(namedObjects));
  }

  /**
   * @return the names of the given named objects.
   */
  public static List<String> getNames(Iterable<? extends HasName> namedObjects) {
    ArrayList<String> ret = new ArrayList<>();
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
    ArrayList<String> ret = new ArrayList<>();
    for (String name : schema.getColNames())
      ret.add(prefix + name);
    return ret;
  }

  /**
   * @return a pair containing 1) the partial path up to the last element and 2) the last element of the given path.
   */
  public static Pair<String, String> splitPrefixedName(String name) {
    int i = name.lastIndexOf(SEPARATOR);
    return new Pair<>(name.substring(0, i), name.substring(i));
  }

  /**
   * @return an ordered mapping of the given objects by their names
   */
  public static <T extends HasName> Map<String, T> mapByName(Iterable<T> namedObjects) {
    LinkedHashMap<String, T> ret = new LinkedHashMap<>();
    for (T namedObject : namedObjects)
      ret.put(namedObject.getName(), namedObject);
    return ret;
  }

  /**
   * @return an ordered mapping of the names of the given objects to their ordinal numbers.
   */
  public static Map<String, Integer> mapNamesToOrdinals(Iterable<? extends HasName> namedObjects) {
    LinkedHashMap<String, Integer> ret = new LinkedHashMap<>();
    int i = 0;
    for (HasName namedObject : namedObjects)
      ret.put(namedObject.getName(), i++);
    return ret;
  }

  /**
   * @return an ordered mapping of the names of the given objects to their ordinal numbers.
   */
  public static <T extends HasName & HasValue<V>, V> Map<String, V> mapNamesToValues(Iterable<T> namedObjects) {
    LinkedHashMap<String, V> ret = new LinkedHashMap<>();
    for (T namedObject : namedObjects)
      ret.put(namedObject.getName(), namedObject.get());
    return ret;
  }
}
