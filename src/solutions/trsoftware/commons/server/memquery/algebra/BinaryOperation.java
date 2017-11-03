package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.client.util.SetUtils;
import solutions.trsoftware.commons.server.memquery.RelationSchema;

import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static solutions.trsoftware.commons.server.memquery.util.NameUtils.getUniqueNames;
import static solutions.trsoftware.commons.server.memquery.util.NameUtils.mapByName;

/**
 * @author Alex, 1/14/14
 */
public abstract class BinaryOperation<P> extends RelationalOperation<P> {

  private RelationalExpression lhs;
  private RelationalExpression rhs;

  protected final Map<String, RelationSchema> inputSchemasByName;

  public BinaryOperation(RelationalExpression lhs, RelationalExpression rhs, P parameters) {
    super(parameters);
    this.lhs = lhs;
    this.rhs = rhs;
    inputSchemasByName = unmodifiableMap(mapByName(asList(getLHSchema(), getRHSchema())));
  }

  public RelationalExpression getLHS() {
    return lhs;
  }

  public RelationalExpression getRHS() {
    return rhs;
  }

  @Override
  public void accept(RelationalExpressionVisitor visitor) {
    getLHS().accept(visitor);
    visitor.visit(this);
    getRHS().accept(visitor);
  }

  public RelationSchema getInputSchema(String name) {
    return inputSchemasByName.get(name);
  }

  public RelationSchema getLHSchema() {
    return lhs.getOutputSchema();
  }

  public RelationSchema getRHSchema() {
    return rhs.getOutputSchema();
  }

  public String getLeftName() {
    return getLHSchema().getName();
  }

  public String getRightName() {
    return getRHSchema().getName();
  }

  /**
   * @return The set of attribute names shared by the two input schemas.
   */
  protected static Set<String> getAttributeNamesIntersection(RelationSchema leftInputSchema, RelationSchema rightInputSchema) {
    return SetUtils.intersection(getUniqueNames(leftInputSchema), getUniqueNames(rightInputSchema));
  }

  @Override
  protected String createOutputName() {
    return new StringBuilder(getClass().getSimpleName())
        .append("(").append(getLeftName()).append(", ")
        .append(getRightName()).append(")").toString();
  }

}
