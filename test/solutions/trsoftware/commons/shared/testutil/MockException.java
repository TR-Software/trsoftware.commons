package solutions.trsoftware.commons.shared.testutil;

import java.util.Objects;

/**
 * Can be used in conjunction {@link AssertUtils#assertThrows} to implement test logic that requires an arbitrary
 * checked exception.
 * <p>
 * Overrides {@link #equals(Object)} and {@link #hashCode()} to compare 2 instances based on their
 * {@linkplain #getMessage() message}.
 *
 * @author Alex
 * @since 1/17/2023
 */
public class MockException extends Exception {

  public MockException() {
  }

  public MockException(String message) {
    super(message);
  }

  public MockException(String message, Throwable cause) {
    super(message, cause);
  }

  public MockException(Throwable cause) {
    super(cause);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    MockException that = (MockException)o;
    return Objects.equals(getMessage(), that.getMessage());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getMessage());
  }

}
