package solutions.trsoftware.commons.shared.validation;

/**
 * A convenience base class for {@link ValidationRule}s that don't need a name.
 * Provides a default implementation of {@link #getFieldName()} that returns {@code null}.
 *
 * @author Alex
 * @since 11/20/2017
 */
public abstract class AnonymousValidationRule<V> implements ValidationRule<V> {

  @Override
  public String getFieldName() {
    return null;
  }
}
