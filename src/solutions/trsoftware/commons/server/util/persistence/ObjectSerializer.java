package solutions.trsoftware.commons.server.util.persistence;

import java.util.function.Function;

/**
 * An object that can serialize and parse an instance of type {@link T} to/from a data type {@link R} (such as a string
 * or byte array).
 *
 * @param <T> type of the objects being serialized
 * @param <R> type of the serialized representation
 *
 * @author Alex
 * @since 4/23/2019
 */
public interface ObjectSerializer<T, R> extends Function<T, R> {

  /**
   * Serializes the given object.
   * @param instance the object to serialize
   * @return the serialized representation of the given instance
   */
  R serialize(T instance);

  /**
   * Parses the given data into an object of type {@link T}.
   * @param data the serialized representation of an object of type {@link T}
   * @return an instance of type {@link T} parsed from the given data
   */
  T parse(R data);

  /**
   * Serializes the given object.
   * @param instance the object to serialize
   * @return the serialized representation of the given instance
   * @see #serialize(Object)
   */
  @Override
  default R apply(T instance) {
    return serialize(instance);
  }
}
