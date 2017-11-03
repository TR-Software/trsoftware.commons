package solutions.trsoftware.commons.client.util.callables;

/**
 * A function {@link A} &rarr; {@link R} that might throw exception {@link E}
 *
 * @author Alex
 */
public interface Function1t<A, R, E extends Throwable> {
  R call(A arg) throws E;
}