package solutions.trsoftware.commons.client.util.callables;

/**
 * A var-args function, {@link Object}<sub>1</sub> &times; ... &times; {@link Object}<sub>N</sub> &rarr; {@link V}
 *
 * @author Alex
 */
public interface FunctionN<V> {
  V call(Object... args);
}