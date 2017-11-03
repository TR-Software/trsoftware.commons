package solutions.trsoftware.commons.client.util.callables;

/**
 * A function {@link A1} &times; {@link A2} &times; {@link A3} &rarr; &empty;
 *
 * @author Alex
 */
public interface Function3_<A1, A2, A3> {
  void call(A1 arg1, A2 arg2, A3 arg3);
}