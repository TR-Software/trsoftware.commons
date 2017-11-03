package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * @author Alex, 4/20/2015
 */
public class IsNotNull extends ColValuePredicate<Object> {

  public IsNotNull(String colName) {
    super(colName);
  }

  @Override
  public boolean eval(Object value) {
    return value != null;
  }

  @Override
  public String toString() {
    return String.format("`%s` != NULL", colName);
  }
}
