package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.RelationSchema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Alex, 1/10/14
 */
public class NaturalJoin extends EquiJoin {

  public NaturalJoin(RelationalExpression lhs, RelationalExpression rhs, Type type) {
    super(lhs, rhs, new Params(type, buildColNameCorrespondence(lhs.getOutputSchema(), rhs.getOutputSchema())));
  }

  /** Generates the correspondence of the equivalent cols from both sides */
  private static Map<String, String> buildColNameCorrespondence(RelationSchema leftSchema, RelationSchema rightSchema) {
    Set<String> sharedNames = getAttributeNamesIntersection(leftSchema, rightSchema);
    LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
    for (String name : sharedNames) {
      ret.put(name, name);
    }
    return ret;
  }

}
