package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.RelationSchema;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * A relational algebra operation, which is a parametrized specification for transforming an input relation (schema) into
 * an output relation.  The parameters define the schema for the output relation.  Each instance is immutable.
 *
 * @author Alex, 1/14/14
 */
public abstract class RelationalOperation<P> implements RelationalExpression {

  private RelationSchema outputSchema;

  protected final P params;

  public RelationalOperation(P params) {
    this.params = params;
  }

  public RelationSchema getOutputSchema() {
    if (outputSchema == null)
      outputSchema = createOutputSchema(); // can't init this field in the constructor because subclasses won't have the required fields set until fully baked
    return outputSchema;
  }

  public P getParams() {
    return params;
  }

  private RelationSchema createOutputSchema() {
    List<ColSpec> outputCols = new ArrayList<ColSpec>();
    for (String name : getOutputColNames())
      outputCols.add(createColSpec(name));
    return new RelationSchema(createOutputName(), outputCols);
  }

  /** @return the names of all the columns in the output schema */
  protected abstract Iterable<String> getOutputColNames();

  /** Creates the named column in the output schema. */
  protected abstract ColSpec createColSpec(String name);

  /** @return the name of the output relation */
  protected abstract String createOutputName();

}
