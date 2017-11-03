package solutions.trsoftware.commons.server.util.reflect;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;

/**
 * A {@link Comparator} that allows sorting classes in order of decreasing "complexity", which we define to be the
 * number of public instance members (non-static fields and methods) accessible from an instance of the class.
 * If the complexity of two classes is the same, this comparator breaks ties in such a ways as to ensure
 * that any implementation of an interface comes before the interface, and any subclass comes before its superclass.
 *
 * This metric is useful for dynamically deciding which widening conversion to use when comparing objects
 * (see {@link ObjectDiffs}).
 *
 * @author Alex, 4/21/2016
 * @see #complexityOf(Class)
 */
public class InstanceComplexityComparator implements Comparator<Class<?>> {

  private static InstanceComplexityComparator INSTANCE = new InstanceComplexityComparator();

  /**
   * @return the singleton instance of this class
   */
  public static InstanceComplexityComparator get() {
    return INSTANCE;
  }

  private InstanceComplexityComparator() {}   // singleton

  /**
   * @return the "complexity" of the given class, which we define as follows:
   * <ol>
   *   <li>complexity of <code>{@link Object}.class</code> is 1</li>
   *   <li>complexity of any other class or interface is 1 plus the number of public members accessible from
   *       an instance of the class (excluding the methods defined by the {@link Object} class.
   *   </li>
   * </ol>
   * to be
   */
  public static int complexityOf(Class<?> cls) {
    if (cls == Object.class)
      return 1;
    return 1 + countPublicInstanceMembers(cls);
  }

  private static int countPublicInstanceMembers(Class<?> cls) {
    return countNonStatic(cls.getFields()) + countNonStatic(cls.getMethods());
  }

  /**
   * @return the number of elements in the given array that don't have the {@code static} modifier and don't
   * correspond to methods declared by the {@link Object} class.
   */
  private static int countNonStatic(Member[] members) {
    int count = 0;
    for (Member member : members)
      if (!Modifier.isStatic(member.getModifiers()) && !memberDeclaredByObject(member))
        count++;
    return count;
  }

  /**
   * @return true iff the given member corresponds to a method declared by the {@link Object} class ({@link Object} only
   * declares methods, no fields).
   */
  private static boolean memberDeclaredByObject(Member member) {
    if (member instanceof Method) {
      Method method = (Method)member;
      try {
        Object.class.getMethod(method.getName(), method.getParameterTypes());
        return true;  // the above line would have thrown an exception if Object doesn't declare a method with the same name and parameter types
      }
      catch (NoSuchMethodException e) {
        return false;
      }
    }
    return false;
  }

  @Override
  public int compare(Class<?> a, Class<?> b) {
    if (a == b)
      return 0;
    int ret = complexityOf(b) - complexityOf(a);
    if (ret == 0) {
      // if the two classes have the same "complexity" we might have to break a tie to ensure that
      // any implementation of an interface should come before the interface, and any subclass should come before its superclass
      if (a.isAssignableFrom(b))
        return 1;  // b is "more specific" than a
      else if (b.isAssignableFrom(a))
        return -1;  // a is "more specific" than b
    }
    return ret;
  }
}
