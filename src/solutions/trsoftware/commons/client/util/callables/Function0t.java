package solutions.trsoftware.commons.client.util.callables;

/**
 A function &empty; &rarr; {@link R} that might throw exception {@link E}
 *
 * @author Alex
 */
public interface Function0t<R, E extends Throwable> {
  R call() throws E;
}