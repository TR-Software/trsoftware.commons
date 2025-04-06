package solutions.trsoftware.commons.shared.util.callables;

import java.util.concurrent.Callable;

/**
 * Facilitates using a lambda expression for a {@link Callable} that doesn't need a return value
 *
 * @author Alex
 * @since 3/6/2025
 */
@FunctionalInterface
public interface VoidCallable extends Callable<Void> {

  @Override
  default Void call() throws Exception {
    doCall();
    return null;
  }

  void doCall() throws Exception;
}
