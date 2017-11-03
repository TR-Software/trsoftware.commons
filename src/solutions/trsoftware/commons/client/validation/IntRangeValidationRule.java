package solutions.trsoftware.commons.client.validation;

import solutions.trsoftware.commons.client.util.StringUtils;

/**
 * Date: Jun 5, 2008 Time: 6:31:35 PM
 *
 * @author Alex
 */
public class IntRangeValidationRule extends ValidationRule {
  private int minValue;
  private int maxValue;

  public IntRangeValidationRule(String fieldName, boolean acceptNull, int minValue, int maxValue) {
    super(fieldName, acceptNull);
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  /** Accepts all numbers in the int range */
  public IntRangeValidationRule(String fieldName, boolean acceptNull) {
    this(fieldName, acceptNull, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  public int getMinValue() {
    return minValue;
  }

  public int getMaxValue() {
    return maxValue;
  }

  protected ValidationResult applyValidationLogic(String value) {
    boolean valid;
    try {
      int number = Integer.parseInt(value);
      valid = number >= minValue && number <= maxValue;
    }
    catch (NumberFormatException e) {
      valid = false;
    }
    
    if (valid)
      return success();
    else {
      String errorMsg = "must be a number";
      if (minValue != Integer.MIN_VALUE && maxValue != Integer.MAX_VALUE)
        errorMsg += StringUtils.template(" between $1 and $2", minValue, maxValue);
      else if (minValue == Integer.MIN_VALUE)
        errorMsg += " less than or equal to " + maxValue;
      else // maxValue == Integer.MAX_VALUE
        errorMsg += " greater than or equal to " + minValue;
      return error(errorMsg);
    }
  }
}
