package solutions.trsoftware.commons.client.util.callables;

/**
 * A function {@link A} &rarr; &empty; that might throw exception {@link E}
 *
 * @author Alex
 */
public interface Function1_t<A, E extends Throwable> {
  void call(A arg) throws E;
}