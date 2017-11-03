package solutions.trsoftware.commons.client.util.callables;

/**
 * A function &empty; &rarr; &empty; that might throw exception {@link E}
 *
 * @author Alex
 */
public interface Function0_t<E extends Throwable> {
  void call() throws E;
}