package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * Computes the Cartesian product of two relations that have no shared attribute names.
 *
 * @author Alex, 1/12/14
 */
public class CrossJoin extends Join<Join.Params> {

  public CrossJoin(RelationalExpression lhs, RelationalExpression rhs) {
    super(lhs, rhs, new Params(Type.INNER));
    if (!getAttributeNamesIntersection(getLHSchema(), getRHSchema()).isEmpty())
      throw new IllegalArgumentException("The Cartesian product is not defined when the input relations have any attribute names in common");
  }

  @Override
  public boolean match(Row leftRow, Row rightRow) {
    return true;  // every row on either side participates in the cross product
  }

}
