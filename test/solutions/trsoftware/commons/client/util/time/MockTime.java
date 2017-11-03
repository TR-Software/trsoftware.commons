package solutions.trsoftware.commons.client.util.time;

/**
 * @author Alex, 4/7/2015
 */
public class MockTime extends Time {

  private double time;

  public MockTime(double time) {
    this.time = time;
  }

  public void advance(double offset) {
    time += offset;
  }

  @Override
  public double currentTimeMillis() {
    return time;
  }
}
