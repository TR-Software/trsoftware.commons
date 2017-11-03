package solutions.trsoftware.commons.server.util.reflect;

import com.google.common.base.Predicate;
import solutions.trsoftware.commons.client.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Specifies a subset of members ({@link Method}s and {@link Field}s) accessible from an instance of a class,
 * and provides an {@link #iterator()} over them.  The members returned by this iterator will match all of the predicates
 * added by calling {@link #addFilter(MemberPattern)} and will not match any of the predicates
 * added by calling {@link #exclude(MemberPattern)}, {@link #exclude(Member...)}, or {@link #excludeMethodsInheritedFromObject()}.
 */
public class MemberSet<T> implements Iterable<Member> {

  private final Class<T> type;
  private final LinkedHashSet<Member> filteredMembers = new LinkedHashSet<Member>();
//  private final CollectionFilter<Member> memberFilter = new CollectionFilter<Member>();

  /** A member must satisfy all the predicates in this list in order to be included in the iterator */
  private final List<Predicate<Member>> filters = new ArrayList<Predicate<Member>>();


  // TODO: implement MemberQuery.toString, equals, and hashCode


  /**
   * Factory method.
   * @return A default instance of this class for the given type
   */
  public static <T> MemberSet<T> create(Class<T> type) {
    return new MemberSet<T>(type);
  }

  /**
   * By default will include only public instance (non-static) fields and getters (non-void 0-arg instance methods).
   */
  public MemberSet(Class<T> type) {
    this(type, true, true);
  }

  public MemberSet(Class<T> type, boolean instanceMembersOnly, boolean fieldsAndGettersOnly) {
    Assert.assertNotNull(type);
    this.type = type;
    filteredMembers.addAll(ReflectionUtils.listMembersAccessibleFrom(type));
    if (instanceMembersOnly)
      exclude(MemberPattern.modifiers(Modifier.STATIC));
    if (fieldsAndGettersOnly) {
      addFilter(MemberPattern.or(
          MemberPattern.isField(),
          MemberPattern.and(
              MemberPattern.paramCount(0),
              MemberPattern.not(MemberPattern.valueTypeIs(void.class)))));
    }
  }

  public Class<T> getType() {
    return type;
  }

  public MemberSet<T> addFilter(MemberPattern memberPattern) {
    filters.add(memberPattern);
    Iterator<Member> it = iterator();
    while (it.hasNext()) {
      if (!memberPattern.matches(it.next()))
        it.remove();
    }
    return this;
  }

  public MemberSet<T> exclude(MemberPattern memberPattern) {
    return addFilter(MemberPattern.not(memberPattern));
  }

  public MemberSet<T> excludeMethodsInheritedFromObject() {
    return exclude(MemberPattern.inheritedFrom(Object.class));
  }

  public MemberSet<T> exclude(Member... specificMembers) {
    return exclude(MemberPattern.specificMembers(specificMembers));
  }

  @Override
  public Iterator<Member> iterator() {
    return filteredMembers.iterator();
  }

  public Set<Member> getFilteredMembers() {
    return filteredMembers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MemberSet memberSet = (MemberSet)o;

    if (!filteredMembers.equals(memberSet.filteredMembers)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return filteredMembers.hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("MemberQuery{");
    sb.append("type=").append(type.getSimpleName());
    sb.append(", filters=").append(filters);
    sb.append('}');
    return sb.toString();
  }


}
