package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.client.util.LogicUtils;
import solutions.trsoftware.commons.server.memquery.Row;

import java.util.Map;

/**
 * Matches rows whose columns have the same value for a given column mapping.
 *
 * @author Alex, 1/10/14
 */
public class EquiJoin extends Join<EquiJoin.Params> {

  public EquiJoin(RelationalExpression lhs, RelationalExpression rhs, Params params) {
    super(lhs, rhs, params);
  }

  @Override
  public boolean match(Row leftRow, Row rightRow) {
    for (Map.Entry<String, String> colNames : params.getColNameCorrespondence().entrySet()) {
      String leftName = colNames.getKey();
      String rightName = colNames.getValue();
      if (!LogicUtils.eq(leftRow.getValue(leftName), rightRow.getValue(rightName)))
        return false;
    }
    return true;
  }

  public static class Params extends Join.Params {

    private final Map<String, String> colNameCorrespondence;

    public Params(Type type, Map<String, String> colNameCorrespondence) {
      super(type);
      this.colNameCorrespondence = colNameCorrespondence;
    }

    public Map<String, String> getColNameCorrespondence() {
      return colNameCorrespondence;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder(super.toString());
      sb.append(" ON ").append(colNameCorrespondence);
      return sb.toString();
    }
  }

}
