package solutions.trsoftware.commons.client.util.callables;

/**
 * A function {@link A} &times; {@link B} &rarr; {@link R}.
 *
 * @author Alex
 */
public interface Function2<A, B, R> {
  R call(A a, B b);
}