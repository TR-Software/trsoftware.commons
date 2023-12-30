package solutions.trsoftware.commons.client.testutil;

/**
 * @author Alex
 * @since 8/29/2023
 */
public class SimulatedException extends RuntimeException {
  private int id;

  public SimulatedException(int id) {
    this("Simulated exception #" + id);
    this.id = id;
  }

  public SimulatedException(String message) {
    super(message);
  }

  private SimulatedException() {
    // empty constructor to suppress warnings about Serializable
  }

  public int getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    SimulatedException that = (SimulatedException)o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
