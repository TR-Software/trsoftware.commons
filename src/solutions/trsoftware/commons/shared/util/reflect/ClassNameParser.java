package solutions.trsoftware.commons.shared.util.reflect;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import solutions.trsoftware.commons.shared.util.StringUtils;

import static solutions.trsoftware.commons.shared.util.StringUtils.nonNull;

/**
 * Parses a value returned by {@link Class#getName()} into its components, which include {@link #packageName},
 * {@link #complexName}, {@link #simpleName}, and {@link #anonymousId} (if the class is anonymous).
 *
 * @author Alex
 * @since 12/25/2017
 */
public class ClassNameParser {

  private static final RegExp regExp = RegExp.compile("^(?:(.*)\\.)?([^.]+?(?:\\$(\\d+))?)$");

  /**
   * The portion of the class name that denotes the package
   * (e.g. {@code "com.example"} if class name is {@code "com.example.Foo$Bar$1"})
   */
  private final String packageName;
  /**
   * The portion of the class name that follows the package
   * (e.g. {@code "Foo$Bar$1"} if class name is {@code "com.example.Foo$Bar$1"})
   */
  private final String complexName;
  /**
   * The value that would be returned by {@link Class#getSimpleName()}
   * (e.g. {@code "Bar"} if class name is {@code "com.example.Foo$Bar"}).
   * If the class is anonymous, this will be an empty string
   * (e.g. {@code ""} if class name is {@code "com.example.Foo$Bar$1"}).
   */
  private final String simpleName;
  /**
   * The portion of the class name that identifies the position of an anonymous class within its enclosing class
   * (e.g. {@code "1"} if class name is {@code "com.example.Foo$Bar$1"}).
   * If the class is not anonymous, this will be an empty string
   * (e.g. {@code ""} if class name is {@code "com.example.Foo$Bar"}).
   */
  private final String anonymousId;

  /**
   * The result of matching {@link #regExp} against the class name passed to the constructor
   */
  private final MatchResult match;

  public ClassNameParser(String clsName) {
    match = regExp.exec(clsName);
    if (match == null)
      throw new IllegalArgumentException("Invalid class name: " + StringUtils.quote(clsName));
    packageName = nonNull(match.getGroup(1));
    complexName = nonNull(match.getGroup(2));
    anonymousId = nonNull(match.getGroup(3));
    // this is where our regex magic ends; have to parse out the simpleName manually
    if (isAnonymous())
      simpleName = "";
    else {
      int lastDollar = complexName.lastIndexOf('$');
      simpleName = complexName.substring(lastDollar + 1);
    }
  }

  public ClassNameParser(Class cls) {
    this(cls.getName());
  }

  /**
   * @return {@link #packageName}
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * @return {@link #complexName}
   */
  public String getComplexName() {
    return complexName;
  }

  /**
   * @return {@link #simpleName}
   */
  public String getSimpleName() {
    return simpleName;
  }

  /**
   * @return {@link #anonymousId}
   */
  public String getAnonymousId() {
    return anonymousId;
  }

  /**
   * @return the full string specifying the class name (which was passed to the constructor)
   */
  public String getFullName() {
    return match.getGroup(0);
  }

  /**
   * @return {@code true} iff the given class name specifies an anonymous class
   */
  public boolean isAnonymous() {
    return !anonymousId.isEmpty();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ClassNameParser{");
    sb.append("packageName='").append(packageName).append('\'');
    sb.append(", complexName='").append(complexName).append('\'');
    sb.append(", simpleName='").append(simpleName).append('\'');
    sb.append(", anonymousId='").append(anonymousId).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
