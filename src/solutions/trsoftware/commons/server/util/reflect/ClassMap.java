package solutions.trsoftware.commons.server.util.reflect;

import java.util.*;

/**
 * A {@link LinkedHashMap} keys of type {@link Class}.  Provides additional operations that allow looking up entries
 * based on the inheritance relationships of classes.
 *
 * @author Alex, 4/21/2016
 * @see #getKeysAssignableFrom(Class[])
 * @see #getBestAssignableFrom(Class[])
 */
public class ClassMap<T> extends LinkedHashMap<Class<?>, T> {

  private final Comparator<Class<?>> comparator;

  /**
   * Uses {@link InstanceComplexityComparator} as the default comparator for sorting the keys returned by the
   * {@link #getKeysAssignableFrom(Class[])} method (which, in turn, affects the value returned by the
   * {@link #getBestAssignableFrom(Class[])} method).
   */
  public ClassMap() {
    this(InstanceComplexityComparator.get());
  }

  /**
   * @param comparator a comparator that affects the ordering of keys returned by the {@link #getKeysAssignableFrom(Class[])} method
   * (which, in turn, affects the value returned by the {@link #getBestAssignableFrom(Class[])} method).
   */
  public ClassMap(Comparator<Class<?>> comparator) {
    this.comparator = comparator;
  }

  /**
   * Computes a subset of {@link #keySet()} such that each class in the resulting set is assignable from (i.e.
   * the same as, superclass of, or interface implemented by) all of args.  The results will be sorted in
   * order of decreasing "complexity" (as defined by {@link InstanceComplexityComparator}), unless a custom comparator
   * was passed to {@link #ClassMap(Comparator)}.
   *
   * @return a sorted list constructed from the set
   * <code>{K: K &isin; {@link #keySet()} && &forall;i(K.isAssignableFrom(args<sub>i</sub>))}</code>
   *
   * @see Class#isAssignableFrom(Class)
   * @see ReflectionUtils#getAllTypesAssignableFromAll(Class[])
   */
  public List<Class<?>> getKeysAssignableFrom(Class... args) {
    Set<Class<?>> typesAssignableFromArgs = ReflectionUtils.getAllTypesAssignableFromAll(args);
    typesAssignableFromArgs.retainAll(keySet());
    ArrayList<Class<?>> ret = new ArrayList<Class<?>>(typesAssignableFromArgs);
    Collections.sort(ret, comparator);
    return ret;
  }

  /**
   * @return The value to which the first key in the set returned by {@link #getKeysAssignableFrom(Class[]) getKeysAssignableFrom(args)}
   * is mapped, or {@code null} if there is no such mapping.
   */
  public T getBestAssignableFrom(Class<?>... args) {
    List<Class<?>> compatibleKeys = getKeysAssignableFrom(args);
    if (compatibleKeys.isEmpty())
      return null;
    else
      return get(compatibleKeys.iterator().next());
  }

}
