package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * @author Alex, 4/20/2015
 */
public class IsNull extends ColValuePredicate<Object> {

  public IsNull(String colName) {
    super(colName);
  }

  @Override
  public boolean eval(Object value) {
    return value == null;
  }

  @Override
  public String toString() {
    return String.format("`%s` == NULL", colName);
  }
}
