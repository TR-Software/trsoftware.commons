package solutions.trsoftware.commons.shared.util;


import com.google.gwt.core.shared.GwtIncompatible;

import java.io.*;
import java.util.Base64;

/**
 * Supplements {@link java.util.Objects} and {@link com.google.common.base.MoreObjects}
 *
 * @author Alex
 * @since 7/29/2025
 */
public abstract class ObjectUtils {

  // TODO: document methods

  @GwtIncompatible @SuppressWarnings("NonJREEmulationClassesInClientCode")
  public static <T extends Serializable> T copySerializable(T object) throws IOException, ClassNotFoundException {
    return deserialize(serialize(object));
  }

  @GwtIncompatible @SuppressWarnings("NonJREEmulationClassesInClientCode")
  public static <T extends Serializable> byte[] serialize(T object) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(object);
    }
    return bos.toByteArray();
  }

  @GwtIncompatible @SuppressWarnings("NonJREEmulationClassesInClientCode")
  public static <T extends Serializable> String serializeBase64(T object) throws IOException {
    return Base64.getEncoder().encodeToString(serialize(object));
  }

  @GwtIncompatible @SuppressWarnings("NonJREEmulationClassesInClientCode")
  public static <T extends Serializable> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
    ByteArrayInputStream bos = new ByteArrayInputStream(bytes);
    try (ObjectInputStream oos = new ObjectInputStream(bos)) {
      //noinspection unchecked
      return (T)oos.readObject();
    }
  }

  @GwtIncompatible @SuppressWarnings("NonJREEmulationClassesInClientCode")
  public static <T extends Serializable> T deserializeBase64(String encoded) throws IOException, ClassNotFoundException {
    // TODO: maybe rename this method to just `deserialize` (overloading the byte[] version)
    return deserialize(Base64.getDecoder().decode(encoded));
  }

}
