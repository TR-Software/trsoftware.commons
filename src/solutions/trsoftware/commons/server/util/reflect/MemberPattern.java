package solutions.trsoftware.commons.server.util.reflect;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import solutions.trsoftware.commons.client.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A predicate that selects/filters members of a class.  Instances of this class (and its subclasses) are immutable.
 */
public abstract class MemberPattern implements Predicate<Member> {

  /** Subclasses should implement this method to test their specific conditions. */
  protected abstract boolean matches(@Nonnull Member member);

  @Override
  public final boolean apply(@Nullable Member member) {
    return member != null && matches(member);
  }

  @Override
  public final String toString() {
    StringBuilder str = new StringBuilder(getClass().getSimpleName());
    str.append('(');
    print(str);
    str.append(')');
    return str.toString();
  }

  protected abstract void print(StringBuilder str);

  // factory methods:

  public static MemberPattern and(MemberPattern... args) {
    return new And(args);
  }

  public static MemberPattern or(MemberPattern... args) {
    return new Or(args);
  }

  public static MemberPattern not(MemberPattern pattern) {
    return new Not(pattern);
  }

  public static MemberType<Field> isField() {
    return MemberType.FIELD;
  }

  public static MemberType<Method> isMethod() {
    return MemberType.METHOD;
  }

  public static Modifiers modifiers(int modifiers) {
    return new Modifiers(modifiers);
  }

  public static NameMatches nameMatches(Pattern regex) {
    return new NameMatches(regex);
  }

  public static NameMatches nameMatches(String regex) {
    return nameMatches(Pattern.compile(regex));
  }

  public static ValueType valueTypeIs(Class<?> returnType) {
    return new ValueType(returnType);
  }

  public static ParameterCount paramCount(int parameterCount) {
    return new ParameterCount(parameterCount);
  }

  public static SpecificMembers specificMembers(@Nonnull Member... specificMembers) {
    return new SpecificMembers(specificMembers);
  }

  public static InheritedFrom inheritedFrom(Class<?> declaringClass) {
    return new InheritedFrom(declaringClass);
  }


  private static abstract class Combination extends MemberPattern {
    private static final int MAX_ARGS = 100;
    private final Predicate<Member> predicate;
    private final MemberPattern[] patterns;

    protected Combination(MemberPattern... patterns) {
      this.patterns = patterns;
      if (patterns.length > MAX_ARGS)
        throw new IllegalArgumentException(getClass().getSimpleName() + " can't have more than " + MAX_ARGS + " values");
      predicate = combine(patterns);
      Assert.assertNotNull(predicate);
    }

    protected abstract Predicate<Member> combine(MemberPattern... patterns);

    @Override
    protected boolean matches(Member member) {
      return predicate.apply(member);
    }

    @Override
    protected void print(StringBuilder str) {
      str.append(Arrays.toString(patterns));
    }
  }

  /**
   * Matches members in the set <code>X<sub>1</sub> &cap; X<sub>2</sub> ... &cap; X<sub>n</sub></code>, where
   * <code>X<sub>i</sub></code> is the set of members matched by an instance of {@link MemberPattern}.
   */
  private static class And extends Combination {
    private And(MemberPattern... patterns) {
      super(patterns);
    }

    @Override
    protected Predicate<Member> combine(MemberPattern... patterns) {
      return Predicates.and(patterns);
    }
  }
  
  /**
   * Matches members in the set <code>X<sub>1</sub> &cup; X<sub>2</sub> ... &cup; X<sub>n</sub></code>, where
   * <code>X<sub>i</sub></code> is the set of members matched by an instance of {@link MemberPattern}.
   */
  private static class Or extends Combination {

    private Or(MemberPattern... patterns) {
      super(patterns);
    }

    @Override
    protected Predicate<Member> combine(MemberPattern... patterns) {
      return Predicates.or(patterns);
    }
  }

  /**
   * Matches members in the set <code>X<sub>1</sub> &cup; X<sub>2</sub> ... &cup; X<sub>n</sub></code>, where
   * <code>X<sub>i</sub></code> is the set of members matched by an instance of {@link MemberPattern}.
   */
  private static class Not extends Combination {

    private Not(MemberPattern pattern) {
      super(pattern);
    }

    @Override
    protected Predicate<Member> combine(MemberPattern... patterns) {
      assert patterns.length == 1;
      return Predicates.not(patterns[0]);
    }
  }



  /**
   * Base class for member predicates that use {@link Member#getDeclaringClass()} information for matching.
  */
  private static abstract class TypePattern extends MemberPattern {
    /** The value to be used by {@link #matches(Member)}. */
    protected final Class<?> type;

    protected TypePattern(Class<?> type) {
      Assert.assertNotNull(type);
      this.type = type;
    }

    @Override
    protected void print(StringBuilder str) {
      str.append(type.getSimpleName());
    }
  }

  /**
   * Selects/filters members of a class that match the given member type (e.g. {@link Method} or {@link Field}).
   */
  public static class MemberType<M extends Member> extends TypePattern {

    public static MemberType<Field> FIELD = new MemberType<Field>(Field.class);
    public static MemberType<Method> METHOD = new MemberType<Method>(Method.class);
    
    // constructor is private because one of the above constants should be used instead
    private MemberType(Class<M> memberType) {
      super(memberType);
    }

    @Override
    protected boolean matches(Member member) {
      return type.isInstance(member);
    }

  }

  /**
   * Selects/filters members of a class by name.  Instances of this class are immutable.
   */
  public static class NameMatches extends MemberPattern {
    public static final Pattern NOTHING = Pattern.compile("a^");  // see http://stackoverflow.com/a/940840/1965404
    public static final Pattern EVERYTHING = Pattern.compile(".*");

    /** Matches members whose name matches this regex. */
    private final Pattern namePattern;

    public NameMatches(Pattern namePattern) {
      Assert.assertNotNull(namePattern);
      this.namePattern = namePattern;
    }

    @Override
    protected boolean matches(Member member) {
      return matches(member, namePattern);
    }

    public static boolean matches(Member member, Pattern namePattern) {
      return namePattern.matcher(member.getName()).matches();
    }

    @Override
    protected void print(StringBuilder str) {
      str.append('"').append(namePattern).append('"');
    }
  }

  /**
   * Selects/filters members of a class based on their modifiers.  Instances of this class are immutable.
   */
  public static class Modifiers extends MemberPattern {

    /** Matches members that have all the modifiers in this bit pattern. */
    private final int modifierPattern;

    public Modifiers(int modifierPattern) {
      this.modifierPattern = modifierPattern;
    }

    @Override
    protected boolean matches(Member member) {
      return matches(member, modifierPattern);
    }

    /**
     * @return true iff the given member's modifiers bitfield contains all of the bits that are set in the given pattern
     */
    public static boolean matches(Member member, int pattern) {
       return (~(~pattern | member.getModifiers())) == 0;
    }

    @Override
    protected void print(StringBuilder str) {
      str.append('{').append(Modifier.toString(modifierPattern)).append('}');
    }
  }

  /**
   * Selects/filters members of a class based on whether they appear the specified set.
   */
  public static class SpecificMembers extends MemberPattern {
    private final Set<Member> memberSet;

    public SpecificMembers(@Nonnull Member... specificMembers) {
      this(Arrays.asList(specificMembers));
    }

    public SpecificMembers(@Nonnull Collection<Member> specificMembers) {
      memberSet = new HashSet<Member>(specificMembers);
    }

    @Override
    protected boolean matches(Member member) {
      return memberSet.contains(member);
    }

    @Override
    protected void print(StringBuilder str) {
      str.append('{');
      Iterator<Member> it = memberSet.iterator();
      while (it.hasNext()) {
        str.append(ReflectionUtils.toString(it.next()));
        if (it.hasNext())
          str.append(", ");
      }
      str.append('}');
    }
  }

  /**
   * Selects/filters the methods of a class based on the number of parameters they declare.
  */
  public static class ParameterCount extends MemberPattern {
    /** Matches only methods that have this number of parameters. */
    private final int parameterCount;

    public ParameterCount(int parameterCount) {
      if (parameterCount < 0)
        throw new IllegalArgumentException();
      this.parameterCount = parameterCount;
    }

    @Override
    protected boolean matches(Member member) {
      return member instanceof Method && ((Method)member).getParameterTypes().length == parameterCount;
    }

    @Override
    protected void print(StringBuilder str) {
      str.append(parameterCount);
    }
  }

  /**
   * Selects/filters the methods of a class based on their return type and fields of a class based on their value type.
  */
  public static class ValueType extends TypePattern {

    public ValueType(Class<?> valueType) {
      super(valueType);
    }

    @Override
    protected boolean matches(Member member) {
      Class<?> valueType = null;
      if (member instanceof Method)
        valueType = ((Method)member).getReturnType();
      else if (member instanceof Field)
        valueType = ((Field)member).getType();
      return valueType == this.type;
    }

  }

  /**
   * Matches members declared in a particular class, or methods overriding those declared in that class.
  */
  public static class InheritedFrom extends TypePattern {

    public InheritedFrom(Class<?> type) {
      super(type);
    }

    @Override
    protected boolean matches(Member member) {
      if (member.getDeclaringClass() == this.type)
        return true;
      else if (member instanceof Method) {
        // does this method override one that's declared in the given type?
        Method method = (Method)member;
        try {
          this.type.getMethod(method.getName(), method.getParameterTypes());
          return true;
        }
        catch (NoSuchMethodException e) {
          return false;
        }
      }
      return false;
    }
  }


}
