package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;

import java.util.Collections;
import java.util.List;

/**
 * An simple relational projection operation (&pi;): projects a subset of attribute names from the input relation.
 *
 * @author Alex, 1/14/14
 */
public class Projection extends StreamableUnaryOperation<List<String>> {

  /**
   * The output relation will contain the given attributes from the input relation.
   */
  public Projection(RelationalExpression input, List<String> attrs) {
    super(input, Collections.unmodifiableList(attrs));
  }

  @Override
  protected Iterable<String> getOutputColNames() {
    return this.params;
  }

  @Override
  protected ColSpec createColSpec(String name) {
    return new NameAccessorColSpec(getInputSchema().get(name));
  }

}
