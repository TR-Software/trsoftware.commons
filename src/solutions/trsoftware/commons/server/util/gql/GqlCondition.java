package solutions.trsoftware.commons.server.util.gql;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A component of the {@code WHERE} clause in a GQL query.
 * Also known as a "filter" in Google App Engine terminology.
 *
 * <pre>{@code
 *   <condition> := <property> {< | <= | > | >= | = | != } <value>
 *   <condition> := <property> IN <list>
 *   <condition> := ANCESTOR IS <entity or key>
 *   <list> := (<value> [, <value> ...]])
 * }</pre>
 *
 * @author Alex
 * @since 12/3/2019
 * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
 */
@Beta  // Not tested
public abstract class GqlCondition implements GqlElement {

  // TODO: finish implementing and unit test this class


  public enum Operator implements GqlElement {
    // see db._OPERATORS list in gappengine/google/appengine/ext/db/__init__.py:275

    LT("<"),
    LE("<="),
    GT(">"),
    GE(">="),
    EQ("="),
    NE("!="),
    IN("IN")
    ;
    /** The GQL representation of this operator (e.g. {@code < | <= | > | >= | = | != } */
    private final String gql;

    Operator(String gql) {
      this.gql = gql;
    }

    @Override
    public String toGql() {
      return gql;
    }

    /**
     * Looks up the appropriate operator instance for the given string.
     * This is the inverse of {@link #toGql()}.
     *
     * @param repr a string like {@code "<"} {@code "<="}, etc.
     * @return the enum constant corresponding to the given string, or {@code null} if not found
     */
    public static Operator get(String repr) {
      Preconditions.checkNotNull(repr);
      repr = repr.trim();
      Preconditions.checkArgument(!repr.isEmpty());
      // the search space here is pretty small, so probably not worth bothering with a static lookup table
      for (Operator op : values()) {
        if (op.gql.equals(repr))
          return op;
      }
      return null;
    }

  }

  /**
   * A condition comparing a property against a single literal value:
   * <pre>{@code
   *   <condition> := <property> {< | <= | > | >= | = | != } <value>
   * }</pre>
   */
  public static class SimpleCondition extends GqlCondition {
    /** Name of the datastore property involved in this condition */
    private final String propName;
    /*
    TODO: don't use solutions.trsoftware.commons.shared.util.compare.Operator here
    because Operator.EQ.toString() returns "==" which won't suffice here; we need "=" for GQL
    - instead create a different enum in this class, which also includes the "IN" operator (see db._OPERATORS list in gappengine/google/appengine/ext/db/__init__.py:275)
     */
    private final Operator operator;
    private final GqlLiteral value;

    public SimpleCondition(@Nonnull String propName, @Nonnull Operator operator, @Nonnull GqlLiteral value) {
      this.propName = propName;
      this.operator = operator;
      this.value = value;
    }

    @Override
    public String toGql() {
      return new StringBuilder(propName).append(' ').append(operator).append(' ').append(value.toGql()).toString();
    }
  }

  /**
   * A condition comparing a property against a list of literal values:
   * <pre>{@code
   *   <condition> := <property> IN <list>
   *   <list> := (<value> [, <value> ...]])
   * }</pre>
   */
  public static class InListCondition extends GqlCondition {
    /** Name of the datastore property involved in this condition */
    private final String propName;
    private final List<GqlLiteral> values;

    public InListCondition(@Nonnull String propName, @Nonnull List<GqlLiteral> values) {
      Preconditions.checkNotNull(propName);
      Preconditions.checkNotNull(values);
      Preconditions.checkArgument(!values.isEmpty());
      this.propName = propName;
      this.values = values;
    }

    @Override
    public String toGql() {
      return new StringBuilder(propName).append(" IN ").append(
          values.stream().map(GqlElement::toGql).collect(Collectors.joining(", ", "(", ")"))
      ).toString();
    }
  }

  // TODO: unit test this class
  // TODO: add support for the ANCESTOR IS condition
  // TODO: create factory methods (or builder) to simplify creation (e.g. take a property_operation string like db.Query.filter)


}
