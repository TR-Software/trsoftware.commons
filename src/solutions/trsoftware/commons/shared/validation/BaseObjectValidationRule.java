package solutions.trsoftware.commons.shared.validation;

/**
 * @author Alex
 * @since 12/31/2017
 */
public abstract class BaseObjectValidationRule<V> extends BaseValidationRule<V> {

  public BaseObjectValidationRule(String fieldName, boolean acceptNull) {
    super(fieldName, acceptNull);
  }

  @Override
  protected boolean isNull(V value) {
    return value == null;
  }
}
