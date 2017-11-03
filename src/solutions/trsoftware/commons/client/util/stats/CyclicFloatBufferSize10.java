package solutions.trsoftware.commons.client.util.stats;

// TODO: replace PlayerStats.recentScores with this data structure for extra efficiency

/**
 * A fixed-size buffer of 10 floats is perfect for representing a player's recent scores. This is a specialized version of CyclicFloatBufferFixedSize, with the
 * size hard-coded for efficiency: it uses a bit less memory for field storage and perf testing shows it to be 1.6 faster
 * (not a huge difference though).
 *
 * solutions.trsoftware.commons.client.util.stats.CyclicFloatBufferSize10Test#testPerformanceVsCyclicFloatBuffer()
 *
 * Adding a new value when the buffer is full forces out the oldest value.
 * The number 10 is hardcoded for extra efficiency, but this structure can be generalized to an arbitrary size.
 * Can compute the mean of the contained numbers on-demand (it doesn't make sense to keep track of the mean
 * all the time, since that would require expensive arithmetic like division every time a new element is added).
 *
 * @author Alex, 1/2/14
 */
public class CyclicFloatBufferSize10 extends CyclicFloatBuffer {

  public CyclicFloatBufferSize10() {
    initBuffer();
  }

  @Override
  protected int maxSize() {
    return 10;
  }
}
