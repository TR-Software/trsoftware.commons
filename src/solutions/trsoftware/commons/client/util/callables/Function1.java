package solutions.trsoftware.commons.client.util.callables;

/**
 * A function {@link A} &rarr; {@link R}.
 *
 * @author Alex
 */
public interface Function1<A, R> {
  R call(A arg);
}