package solutions.trsoftware.commons.shared.testutil;

import java.util.Objects;

/**
 * Can be used in conjunction {@link AssertUtils#assertThrows} to implement test logic that requires an arbitrary
 * unchecked exception.
 * <p>
 * Overrides {@link #equals(Object)} and {@link #hashCode()} to compare 2 instances based on their
 * {@linkplain #getMessage() message}.
 *
 * @author Alex
 * @since 1/17/2023
 */
public class MockRuntimeException extends Exception {

  public MockRuntimeException() {
  }

  public MockRuntimeException(String message) {
    super(message);
  }

  public MockRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public MockRuntimeException(Throwable cause) {
    super(cause);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    MockRuntimeException that = (MockRuntimeException)o;
    return Objects.equals(getMessage(), that.getMessage());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getMessage());
  }

}
