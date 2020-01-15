package solutions.trsoftware.commons.server.util.gql;

/**
 * A component of a GQL query
 *
 * @author Alex
 * @since 12/3/2019
 * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
 */
public interface GqlElement {
  /**
   * @return a GQL query substring representing this element
   */
  String toGql();
}
