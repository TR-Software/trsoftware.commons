package solutions.trsoftware.commons.server.util.persistence;

/**
 * @author Alex, 12/31/2014
 */
public interface JsonSerializer<T> {

  /** @return a new instance of {@link T} based on the data encoded in the given JSON object string. */
  T parseJson(String json);

  /**
   * @return a JSON string encoding the given instance in such a way that it may later be parsed back to an equivalent
   * instance of {@link T} using the {@link #parseJson(String)} method.
   */
  String toJson(T instance);

  Class<T> getValueType();
}
