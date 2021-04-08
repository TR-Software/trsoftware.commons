/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.shared.util.reflect;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import solutions.trsoftware.commons.shared.util.StringUtils;

import static solutions.trsoftware.commons.shared.util.StringUtils.nonNull;

/**
 * Parses a value returned by {@link Class#getName()} into its components, which include {@link #packageName},
 * {@link #complexName}, {@link #simpleName}, and {@link #anonymousId} (if the class is anonymous).
 *
 * <p>
 *   <b>WARNING</b>: Parsing array type names (like {@code "[Ljava.lang.String;"} or {@code "[[[I"}) is not yet supported:
 *   will not throw an exception, but the parse result will not be meaningful.
 *   Refer to {@link Class#getName()} and {@link Class#getSimpleName()} to see how array type names are constructed.
 * </p>
 * <p style="color: #6495ed; font-weight: bold;">
 *   TODO: add support for array classes (with names like {@code "[[Ljava.lang.String;"} or {@code "[[[I"})
 * </p>
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-13.html#jls-13.1">JLS §13.1: The Form of a Binary (re: binary name of a class)</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-6.html#jls-6.7">JLS §6.7: Fully Qualified Names and Canonical Names</a>
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
   * (e.g. {@code "Foo$Bar$1"} if class name is {@code "com.example.Foo$Bar$1"}).
   * This is similar to {@link Class#getSimpleBinaryName()}, but also includes the leading enclosing class name.
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

  /**
   * @param clsName the "binary name" of a class (which would be returned by {@link Class#getName()}.
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-13.html#jls-13.1">JLS §13.1: The Form of a Binary (re: binary name of a class)</a>
   */
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
