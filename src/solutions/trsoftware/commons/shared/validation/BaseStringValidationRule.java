package solutions.trsoftware.commons.shared.validation;

import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * @author Alex
 * @since 12/30/2017
 */
public abstract class BaseStringValidationRule extends BaseValidationRule<String> {

  protected BaseStringValidationRule(String fieldName, boolean acceptNull) {
    super(fieldName, acceptNull);
  }

  @Override
  protected boolean isNull(String value) {
    return StringUtils.isBlank(value);
  }

  @Override
  protected abstract ValidationResult applyValidationLogic(String value);
}
