package solutions.trsoftware.commons.shared.util.time;

import com.google.common.base.Ticker;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * А delegated ticker that can be paused, similar to a {@link Stopwatch}.
 * <p>
 * But in contrast to {@link Stopwatch}, this class is intended to be used as an actual {@link Ticker},
 * rather than just providing a way to measure elapsed time.
 *
 * @author Alex
 * @since 9/25/2024
 */
public class StoppableTicker extends Ticker {
  // TODO: improve doc

  private final Ticker source;

  private final AtomicBoolean running = new AtomicBoolean(true);
  /**
   * The {@linkplain Ticker#read() value} of {@link #source} ticker during the most-recent invocation of {@link #stop()}.
   */
  private long stoppedTick;
  /**
   * The lag of this ticker behind the {@link #source} ticker
   */
  private long offset;


  public StoppableTicker(Ticker source) {
    this.source = source;
  }

  public StoppableTicker(Ticker source, long initialValue) {
    this.source = source;
    this.offset = source.read() - initialValue;
  }

  @Override
  public long read() {
    long sourceTick = running.get() ? source.read() : stoppedTick;
    return sourceTick - offset;
  }

  public void stop() {
    if (running.compareAndSet(true, false)) {
      stoppedTick = source.read();
    }
  }

  public void resume() {
    if (running.compareAndSet(false, true)) {
      offset += source.read() - stoppedTick;
    }
  }

  public boolean isRunning() {
    return running.get();
  }
}
