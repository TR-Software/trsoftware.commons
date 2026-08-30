package solutions.trsoftware.commons.shared.util.function;

/**
 * Wraps a checked exception to be rethrown as an unchecked exception.
 *
 * @see ThrowingFunction#unchecked(ThrowingFunction)
 * @see ThrowingSupplier#unchecked(ThrowingSupplier)
 *
 * @author Alex
 * @since 8/17/2026
 */
public class WrappedException extends RuntimeException {
  private WrappedException() {  // default private constructor for serialization
  }

  public WrappedException(String message, Throwable cause) {
    super(message, cause);
  }

  public WrappedException(Throwable cause) {
    super(cause);
  }
}
