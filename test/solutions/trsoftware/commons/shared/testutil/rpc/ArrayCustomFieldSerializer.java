package solutions.trsoftware.commons.shared.testutil.rpc;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import solutions.trsoftware.commons.shared.testutil.MockSerializationStreamReader;
import solutions.trsoftware.commons.shared.testutil.MockSerializationStreamWriter;

import java.util.function.IntFunction;

/**
 * Convenience base class for implementing generic array serializers that can be passed to
 * {@link MockSerializationStreamWriter#setClassSerializer(Class, CustomFieldSerializer)} in order to allow
 * {@link MockSerializationStreamReader} to deserialize typed object arrays.
 * <p>
 * Subclasses just have to implement {@link #instantiateArray(int)}.
 * <p>
 * Or, more conveniently, can use the factory method {@link #create(IntFunction)} with a method
 * reference to an array constructor e.g. {@code Integer[]::new}
 *
 * @see #create(IntFunction)
 * @author Alex
 * @since 1/15/2023
 */
public abstract class ArrayCustomFieldSerializer<T> extends CustomFieldSerializer<T[]> {

  @SuppressWarnings("unchecked")
  @Override
  public void deserializeInstance(SerializationStreamReader streamReader, T[] arr) throws SerializationException {
    for (int i = 0; i < arr.length; i++) {
      arr[i] = (T)streamReader.readObject();
    }
  }

  @Override
  public void serializeInstance(SerializationStreamWriter streamWriter, T[] arr) throws SerializationException {
    streamWriter.writeInt(arr.length);
    for (T elt : arr) {
      streamWriter.writeObject(elt);
    }
  }

  @Override
  public boolean hasCustomInstantiateInstance() {
    return true;
  }

  @Override
  public T[] instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
    return instantiateArray(streamReader.readInt());
  }

  protected abstract T[] instantiateArray(int length);

  public static <T> ArrayCustomFieldSerializer<T> create(IntFunction<T[]> arrayConstructor) {
    return new ArrayCustomFieldSerializer<T>() {
      @Override
      protected T[] instantiateArray(int length) {
        return arrayConstructor.apply(length);
      }
    };
  }
}
