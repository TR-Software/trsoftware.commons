package solutions.trsoftware.commons.server.memquery;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex, 1/16/14
 */
public abstract class AbstractRow implements Row {

  protected final RelationSchema schema; // TODO: do we really need to store the schema in every row?

  public AbstractRow(RelationSchema schema) {
    this.schema = schema;
  }

  @Override
  public RelationSchema getSchema() {
    return schema;
  }

  @Override
  public int size() {
    return schema.size();
  }

  @Override
  public <T> T getValue(String colName) {
    return (T)schema.get(colName).getValue(this);
  }

  @Override
  public <T> T getValue(int index) {
    return (T)schema.get(index).getValue(this);
  }

  @Override
  public List<String> getNames() {
    return schema.getColNames();
  }

  @Override
  public List<Object> getValues(List<String> names) {
    ArrayList<Object> ret = new ArrayList<Object>();
    for (String name : names) {
      ret.add(getValue(name));
    }
    return ret;
  }

}
