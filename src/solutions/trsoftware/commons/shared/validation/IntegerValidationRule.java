package solutions.trsoftware.commons.shared.validation;

import solutions.trsoftware.commons.shared.util.NumberRange;

/**
 * Checks the min/max value of an integer.
 *
 * @author Alex
 * @since 12/31/2017
 */
public class IntegerValidationRule extends NumberValidationRule<Integer> {

  public IntegerValidationRule(String fieldName, int min, int max, boolean acceptNull) {
    super(fieldName, new NumberRange<Integer>(min, max), acceptNull);
  }

  public IntegerValidationRule(String fieldName, int min, int max) {
    this(fieldName, min, max, false);
  }


}
