package solutions.trsoftware.commons.client.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Date: Oct 24, 2007
 * Time: 12:09:43 PM
 *
 * @author Alex
 */
public class CompositeValidationRule extends ValidationRule {

  List<ValidationRule> rules = new ArrayList<ValidationRule>();

  public CompositeValidationRule(List<ValidationRule> rules) {
    this.rules.addAll(rules);
  }

  public CompositeValidationRule(ValidationRule... rules) {
    this.rules.addAll(Arrays.asList(rules));
  }

  protected ValidationResult applyValidationLogic(String value) {
    for (ValidationRule rule : rules) {
      ValidationResult validationResult = rule.applyValidationLogic(value);
      if (!validationResult.isValid())
        return validationResult;
    }
    return success();
  }
}
