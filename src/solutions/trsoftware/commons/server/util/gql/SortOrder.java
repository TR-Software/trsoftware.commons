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

package solutions.trsoftware.commons.server.util.gql;

import com.google.common.annotations.Beta;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a {@code <property> [ASC | DESC]} entry in the {@code ORDER BY} clause of a GQL query.
 *
 * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
 * @author Alex
 * @since 1/7/2020
 */
@Beta  // Not tested
public class SortOrder implements GqlElement {

  // TODO: unit test this class

  private static final Pattern FORMAL_PATTERN = Pattern.compile("(\\w+)\\s+(ASC|DESC)");
  private static final Pattern SHORTHAND_PATTERN = Pattern.compile("(-)?(\\w+)");

  public enum Direction {ASC, DESC;}

  private String property;
  private Direction direction;

  public SortOrder(@Nonnull String property, @Nonnull Direction direction) {
    if (StringUtils.isBlank(property))
      throw new IllegalArgumentException("Property name cannot be empty");
    this.property = property;
    this.direction = Objects.requireNonNull(direction, "Sort direction must be specified");
  }

  /**
   * Ascending order on the given property.
   *
   * @param property the name of a property
   */
  public SortOrder(@Nonnull String property) {
    this(property, Direction.ASC);
  }

  public String getProperty() {
    return property;
  }

  public Direction getDirection() {
    return direction;
  }

  @Override
  public String toGql() {
    if (direction != Direction.DESC)
      return property;
    else
      return property + " " + direction;
  }

  @Override
  public String toString() {
    return toGql();
  }

  /**
   * Creates an instance of this class from the given string representation.
   *
   * @param spec can be either:
   *     {@code "<property> [ASC | DESC]"}
   *     or just the name of a property, optionally prefixed with "-" to denote descending order.
   */
  public static SortOrder valueOf(String spec) {
    spec = spec.trim();
    Matcher matcher = FORMAL_PATTERN.matcher(spec);
    if (matcher.matches()) {
      // string has the "<property> [ASC | DESC]" form
      return new SortOrder(matcher.group(1), Direction.valueOf(matcher.group(2)));
    }
    else if ((matcher = SHORTHAND_PATTERN.matcher(spec)).matches()) {
      // string has the shorhand "[-]<property>" form
      String propName = matcher.group(2);
      if (matcher.group(1) == null) {
        // not prefixed with "-"
        return new SortOrder(propName);
      }
      else {
        assert matcher.group(1).equals("-");
        return new SortOrder(propName, Direction.DESC);
      }
    }
    else {
      throw new IllegalArgumentException("Unable to parse the given string");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    SortOrder sortOrder = (SortOrder)o;

    if (!property.equals(sortOrder.property))
      return false;
    return direction == sortOrder.direction;
  }

  @Override
  public int hashCode() {
    int result = property.hashCode();
    result = 31 * result + direction.hashCode();
    return result;
  }
}
