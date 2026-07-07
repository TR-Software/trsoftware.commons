package solutions.trsoftware.commons.shared.testutil;

import java.util.function.IntConsumer;

/**
 * Records the argument passed to every invocation of {@link #accept(int)}.
 *
 * @author Alex
 * @since 5/8/2026
 */
public class RecordingIntConsumer extends RecordingConsumer<Integer> implements IntConsumer {

  @Override
  public void accept(int value) {
    super.accept(value);
  }
}
