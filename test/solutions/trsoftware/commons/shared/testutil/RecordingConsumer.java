package solutions.trsoftware.commons.shared.testutil;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Records the argument passed to every invocation of {@link #accept(Object)}
 */
public class RecordingConsumer<T> implements Consumer<T> {
  private final ArrayList<T> history = new ArrayList<>();

  @Override
  public void accept(T t) {
    history.add(t);
  }

  public ArrayList<T> getHistory() {
    return history;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("invocationHistory", history)
        .toString();
  }

  public boolean wasInvoked() {
    return !history.isEmpty();
  }

  public int invocationCount() {
    return history.size();
  }
}
