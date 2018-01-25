package solutions.trsoftware.commons.shared.validation;

/**
 * Specifies a general user input validator.  Can use this interface to validate values of any type (strings most commonly).
 *
 * @param <V> the type of value being validated
 * @author Alex
 * @since 11/11/2017
 */
public interface ValidationRule<V> {
  String getFieldName();

  ValidationResult validate(V value);
}
