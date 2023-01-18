/*
 * Copyright 2023 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.server.testutil.rpc;

import com.google.common.base.MoreObjects;
import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.server.Base64Utils;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.DequeMap;
import com.google.gwt.user.server.rpc.impl.SerializabilityUtil;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import solutions.trsoftware.commons.shared.testutil.MockSerializationStreamReader;
import solutions.trsoftware.commons.shared.testutil.MockSerializationStreamWriter;
import solutions.trsoftware.commons.shared.testutil.rpc.CustomFieldSerializerFactory;
import solutions.trsoftware.commons.shared.testutil.rpc.VectorWriter;
import solutions.trsoftware.commons.shared.util.MapUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.*;
import java.util.*;

import static solutions.trsoftware.commons.server.util.reflect.ReflectionUtils.ensureAccessible;

/**
 * Provides an instance of {@link CustomFieldSerializer} for any class, using the same reflection-based logic as
 * {@link ServerSerializationStreamWriter} and {@link ServerSerializationStreamReader} to implement serialization
 * of classes that don't already have custom serializers.
 * <p>
 * This allows keeping {@link MockSerializationStreamWriter} and {@link MockSerializationStreamReader} GWT-compilable,
 * while offering the full power of GWT serialization in non-GWT unit tests.
 *
 * implem
 * @author Alex
 * @since 1/11/2023
 */
@SuppressWarnings("NonJREEmulationClassesInClientCode")
@GwtIncompatible
public class CustomFieldSerializerFactoryByReflection extends CustomFieldSerializerFactory {

  @Nonnull
  protected final SerializationPolicy serializationPolicy;

  /**
   * Used to look up setter methods of the form 'void Class.setXXX(T value)'
   * given a Class type and a field name XXX corresponding to a field of type T.
   */
  private final Map<Class<?>, Map<String, Method>> settersByClass = new HashMap<>();

  public CustomFieldSerializerFactoryByReflection(@Nonnull SerializationPolicy serializationPolicy) {
    this.serializationPolicy = Objects.requireNonNull(serializationPolicy, "serializationPolicy");
  }

  @Override@SuppressWarnings("unchecked")
  public CustomFieldSerializer<Object> getCustomFieldSerializer(Class<?> instanceClass)
      throws SerializationException {
    return MapUtils.computeIfAbsent(serializerCache, instanceClass, this::createCustomFieldSerializer);
  }


  @SuppressWarnings("unchecked")
  @Nonnull
  private CustomFieldSerializer<Object> createCustomFieldSerializer(Class<?> instanceClass) throws SerializationException {
    Class<?> customSerializerClass = SerializabilityUtil.hasCustomFieldSerializer(instanceClass);
    CustomFieldSerializer<?> customFieldSerializer = null;
    if (customSerializerClass != null) {
      // Use custom field serializer
      customFieldSerializer = loadCustomFieldSerializer(customSerializerClass);
      if (customFieldSerializer == null) {
        customFieldSerializer = new LegacySerializerWrapper(customSerializerClass, instanceClass);
      }
    }
    if (customFieldSerializer == null) {
      customFieldSerializer = new DefaultSerializer(instanceClass);
    }
    return ensureHasCustomInstantiate(instanceClass, (CustomFieldSerializer<Object>)customFieldSerializer);
  }

  /**
   * If the given serializer doesn't have a custom {@link CustomFieldSerializer#instantiateInstance(SerializationStreamReader) instantiateInstance}
   * implementation, it will be wrapped in a {@link ForwardingCustomFieldSerializer} that implements that functionality.
   */
  private CustomFieldSerializer<Object> ensureHasCustomInstantiate(Class<?> instanceClass, CustomFieldSerializer<Object> serializer) {
    if (!serializer.hasCustomInstantiateInstance()) {
      return new ForwardingCustomFieldSerializer<Object>(serializer) {
        @Override
        public boolean hasCustomInstantiateInstance() {
          return true;
        }

        @Override
        public Object instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
          return instantiate(streamReader, instanceClass);
        }
      };
    }
    return serializer;
  }

  /**
   * Loads a {@link CustomFieldSerializer} from a class that may implement that interface.
   * <p>
   * Based on the package-private method {@link SerializabilityUtil#loadCustomFieldSerializer(Class)}
   * @return a new instance of the given class if it's a subclass of {@link CustomFieldSerializer}; otherwise {@code null}
   */
  @Nullable
  CustomFieldSerializer<?> loadCustomFieldSerializer(final Class<?> customSerializerClass) throws SerializationException {
    CustomFieldSerializer<?> customFieldSerializer = null;
    if (CustomFieldSerializer.class.isAssignableFrom(customSerializerClass)) {
      try {
        // NOTE: a CustomFieldSerializer should have a public default constructor, so any exception thrown by
        // Class.newInstance is abnormal
        customFieldSerializer = (CustomFieldSerializer<?>) customSerializerClass.newInstance();
      } catch (IllegalAccessException | InstantiationException | IllegalArgumentException
          | ExceptionInInitializerError | SecurityException e) {
        throw new SerializationException(e);
      }
    }
    return customFieldSerializer;
  }

  /**
   * Returns a Map from a field name to the setter method for that field, for a
   * given class. The results are computed once for each class and cached.
   *
   * @param instanceClass the class to query
   * @return a Map from Strings to Methods such that the name <code>XXX</code>
   *         (corresponding to the field <code>T XXX</code>) maps to the method
   *         <code>void setXXX(T value)</code>, or null if no such method
   *         exists.
   */
  private Map<String, Method> getSetters(Class<?> instanceClass) {
    synchronized (settersByClass) {
      Map<String, Method> setters = settersByClass.get(instanceClass);
      if (setters == null) {
        setters = new HashMap<String, Method>();

        // Iterate over each field and locate a suitable setter method
        Field[] fields = instanceClass.getDeclaredFields();
        for (Field field : fields) {
          // Consider non-static, non-transient (or @GwtTransient) fields only
          if (isNotStaticOrTransient(field)
              && isNotFinal(field)) {
            String fieldName = field.getName();
            String setterName =
                "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            try {
              Method setter = instanceClass.getMethod(setterName, field.getType());
              setters.put(fieldName, setter);
            } catch (NoSuchMethodException e) {
              // Just leave this field out of the map
            }
          }
        }

        settersByClass.put(instanceClass, setters);
      }

      return setters;
    }
  }

  /**
   * Returns true if this field has an annotation named "GwtTransient".
   */
  static boolean hasGwtTransientAnnotation(Field field) {
    return field.getAnnotation(GwtTransient.class) != null;
  }

  static boolean isNotStaticOrTransient(Field field) {
    int fieldModifiers = field.getModifiers();
    return !Modifier.isStatic(fieldModifiers)
        && !Modifier.isTransient(fieldModifiers)
        && !hasGwtTransientAnnotation(field);
  }

  static boolean isNotFinal(Field field) {
    return !Modifier.isFinal(field.getModifiers());
  }

  @Nonnull
  public SerializationPolicy getSerializationPolicy() {
    return serializationPolicy;
  }


  static class ForwardingCustomFieldSerializer<T> extends CustomFieldSerializer<T> {
    private final CustomFieldSerializer<T> delegate;

    public ForwardingCustomFieldSerializer(@Nonnull CustomFieldSerializer<T> delegate) {
      this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, T instance) throws SerializationException {
      delegate.deserializeInstance(streamReader, instance);
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
      return delegate.hasCustomInstantiateInstance();
    }

    @Override
    public T instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
      return delegate.instantiateInstance(streamReader);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, T instance) throws SerializationException {
      delegate.serializeInstance(streamWriter, instance);
    }
  }

  /**
   * Adapter for a legacy serializer, which is a class that doesn't implement {@link CustomFieldSerializer},
   * but instead defines static methods named {@code serialize} and {@code deserialize},
   * and possibly the optional methods {@code deserializeChecked}, {@code instantiate}, and {@code instantiateChecked}.
   */
  static class LegacySerializerWrapper extends CustomFieldSerializer<Object> {

    private final Class<?> instanceClass;

    private Method serialize;
    private Method deserialize;
    private Method deserializeChecked;
    private Method instantiate;
    private Method instantiateChecked;

    /* TODO: support deserializeChecked and instantiateChecked?
        Probably not worth it, since those legacy methods aren't actually present in any GWT package
     */

    /**
     * @param customSerializerClass a legacy serializer class with static methods "serialize" and "deserialize"
     * @param instanceClass the class of the instance being serialized/deserialized
     * @see ServerSerializationStreamWriter#serializeWithCustomSerializer
     * @see ServerSerializationStreamReader#deserializeWithCustomFieldDeserializer
     */
    public LegacySerializerWrapper(final Class<?> customSerializerClass, Class<?> instanceClass) throws SerializationException {
      this.instanceClass = instanceClass;
      for (Method method : customSerializerClass.getMethods()) {
        switch (method.getName()) {
          case "serialize":
            serialize = method;
            break;
          case "deserialize":
            deserialize = method;
            break;
          case "deserializeChecked":
            deserializeChecked = method;
            break;
          case "instantiate":
            instantiate = method;
            break;
          case "instantiateChecked":
            instantiateChecked = method;
            break;
        }
      }
      if (serialize == null)
        throw new SerializationException(
            String.format("Legacy custom field serializer class %s does not contain a '%s' method",
                customSerializerClass, "serialize"));
      if (deserialize == null && deserializeChecked == null)
        throw new SerializationException(
            String.format("Legacy custom field serializer class %s does not contain either a '%s' or '%s' method",
                customSerializerClass, "deserialize", "deserializeChecked"));
      // OK not to have any custom instantiate methods
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, Object instance) throws SerializationException {
      try {
        serialize.invoke(null, streamWriter, instance);
      }
      catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException | SecurityException e) {
        throw new SerializationException(e);
      }
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, Object instance) throws SerializationException {
      try {
        deserialize.invoke(null, streamReader, instance);
      }
      catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
        throw new SerializationException(e);
      }
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
      return instantiate != null || instantiateChecked != null;
    }

    @Override
    public Object instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
      try {
        // TODO: maybe support instantiateChecked method (see com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader.instantiateWithCustomFieldInstantiator)
        if (instantiate != null) {
          // static method
          return instantiate.invoke(null, streamReader);
        }
        else {
          Constructor<?> constructor = instanceClass.getDeclaredConstructor();
          constructor.setAccessible(true);
          return constructor.newInstance();
        }
      }
      catch (IllegalAccessException | InvocationTargetException | InstantiationException
          | IllegalArgumentException | NoSuchMethodException e) {
        throw new SerializationException(e);
      }
    }
  }



  class DefaultSerializer extends CustomFieldSerializer<Object> {
    private final Class<?> instanceClass;

    public DefaultSerializer(Class<?> instanceClass) {
      this.instanceClass = Objects.requireNonNull(instanceClass, "instanceClass");
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, Object instance) throws SerializationException {
      // this code is based on com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter.serializeClass
      Class<?> instanceClass = instance.getClass();
      serializeImpl(streamWriter, instance, instanceClass);
    }

    @Override
    public Object instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
      return instantiate(streamReader, this.instanceClass);
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
      return true;
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, Object instance) throws SerializationException {
      assert instanceClass == instance.getClass();
      deserializeImpl(streamReader, instance);
    }

    private Object deserializeImpl(SerializationStreamReader streamReader, Object instance) throws SerializationException {
      if (instanceClass.isArray()) {
        return deserializeArray(streamReader, instance);
      }
      else if (!instanceClass.isEnum()) {  // Enums are deserialized when they are instantiated
        try {
          deserializeClass(streamReader, instance, null, null);
        }
        catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
          throw new SerializationException(e);
        }
      }
      return instance;
    }

    private void deserializeClass(SerializationStreamReader streamReader, Object instance,
                                  Type[] expectedParameterTypes, DequeMap<TypeVariable<?>, Type> resolvedTypes) throws
        SerializationException, IllegalAccessException, NoSuchMethodException,
        InvocationTargetException, ClassNotFoundException {
      /*
        A map from field names to corresponding setter methods. The reference
        will be null for classes that do not require special handling for
        server-only fields.
       */
      Map<String, Method> setters = null;

      /*
        A list of fields of this class known to the client. If null, assume the
        class is not enhanced and don't attempt to deal with server-only fields.
       */
      Set<String> clientFieldNames = serializationPolicy.getClientFieldNamesForEnhancedClass(instanceClass);
      /*
       TODO: the following code was copied verbatim from com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader.deserializeClass
         - do we really need it?
      */
      if (clientFieldNames != null) {
        // Read and set server-only instance fields encoded in the RPC data
        try {
          String encodedData = streamReader.readString();
          if (encodedData != null) {
            byte[] serializedData = Base64Utils.fromBase64(encodedData);
            ByteArrayInputStream baos = new ByteArrayInputStream(serializedData);
            ObjectInputStream ois = new ObjectInputStream(baos);

            int count = ois.readInt();
            for (int i = 0; i < count; i++) {
              String fieldName = (String) ois.readObject();
              Object fieldValue = ois.readObject();
              Field field = instanceClass.getDeclaredField(fieldName);
              field.setAccessible(true);
              field.set(instance, fieldValue);
            }
          }
        } catch (IOException | NoSuchFieldException e) {
          throw new SerializationException(e);
        }

        setters = getSetters(instanceClass);
      }

      Field[] serializableFields = getSerializableFields(instanceClass);
      for (Field declField : serializableFields) {
        assert (declField != null);
        if ((clientFieldNames != null) && !clientFieldNames.contains(declField.getName())) {
          continue;
        }

        Type declGenericType = declField.getGenericType();
        Object value = deserializeValue(streamReader, declField.getType(), declGenericType, resolvedTypes);

        String fieldName = declField.getName();
        Method setter;
        /*
         * If setters is non-null and there is a setter method for the given
         * field, call the setter. Otherwise, set the field value directly. For
         * persistence APIs such as JDO, the setter methods have been enhanced to
         * manipulate additional object state, causing direct field writes to fail
         * to update the object state properly.
         */
        if ((setters != null) && ((setter = setters.get(fieldName)) != null)) {
          setter.invoke(instance, value);
        } else {
          ensureAccessible(declField).set(instance, value);
        }
      }
    }

    public Object deserializeValue(SerializationStreamReader streamReader, Class<?> rpcType, Type methodType,
                                   DequeMap<TypeVariable<?>, Type> resolvedTypes) throws SerializationException {
      return ValueReader.forClass(rpcType).read(streamReader);
      // TODO: support generic types? see com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader.deserializeValue(java.lang.Class<?>, java.lang.reflect.Type, com.google.gwt.user.server.rpc.impl.DequeMap<java.lang.reflect.TypeVariable<?>,java.lang.reflect.Type>)
    }

    private Object deserializeArray(SerializationStreamReader streamReader, Object instance) throws SerializationException {
      // TODO: add support for generic arrays? (see com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader.deserializeImpl)
      VectorReader vectorReader = VectorReader.forClass(instanceClass);
      vectorReader.read(streamReader, instance);
      return instance;
    }

    /**
     * @see ServerSerializationStreamWriter#serializeImpl(Object, Class)
     */
    private void serializeImpl(SerializationStreamWriter streamWriter, Object instance, Class<?> instanceClass) throws SerializationException {
      assert instance != null;
      CustomFieldSerializer<Object> customFieldSerializer = (instanceClass != this.instanceClass) ?
          getCustomFieldSerializer(instanceClass) : null;
      if (customFieldSerializer != null) {
        customFieldSerializer.serializeInstance(streamWriter, instance);
      }
      else if (instanceClass.isArray()) {
        serializeArray(streamWriter, instance, instanceClass);
      }
      else if (instanceClass.isEnum()) {
        streamWriter.writeInt(((Enum<?>)instance).ordinal());
      }
      else {
        // Regular class instance
        serializeClass(streamWriter, instance, instanceClass);
      }
    }

    /**
     * @see ServerSerializationStreamWriter#serializeClass(Object, Class)
     */
    private void serializeClass(SerializationStreamWriter streamWriter, Object instance, Class<?> instanceClass) throws SerializationException {
      Field[] serializableFields = getSerializableFields(instanceClass);
      for (Field declField : serializableFields) {
        Object value;
        try {
          value = ensureAccessible(declField).get(instance);
          serializeValue(streamWriter, value, declField.getType());
        } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new SerializationException(e);
        }
      }
      Class<?> superClass = instanceClass.getSuperclass();
      if (serializationPolicy.shouldSerializeFields(superClass)) {
        // NOTE: this would be false for Object class, which is not Serializable
        serializeImpl(streamWriter, instance, superClass);
      }
    }

    private void serializeArray(SerializationStreamWriter streamWriter, Object instance, Class<?> instanceClass) throws SerializationException {
      assert instanceClass.isArray();
      VectorWriter.forClass(instanceClass).write(streamWriter, instance);
    }

    private void serializeValue(SerializationStreamWriter streamWriter, Object value, Class<?> type) throws SerializationException {
      ValueWriter.forClass(type).write(streamWriter, value);
    }

    private Field[] getSerializableFields(Class<?> cls) {
      return SerializabilityUtil.applyFieldSerializationPolicy(cls, serializationPolicy);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("instanceClass", instanceClass)
          .toString();
    }
  }

  public static Object instantiate(SerializationStreamReader streamReader, Class<?> instanceClass) throws SerializationException {
    if (instanceClass.isArray()) {
      int length = streamReader.readInt();
      return Array.newInstance(instanceClass.getComponentType(), length);
    }
    else if (instanceClass.isEnum()) {
      Enum<?>[] enumConstants = (Enum[])instanceClass.getEnumConstants();
      int ordinal = streamReader.readInt();
      assert (ordinal >= 0 && ordinal < enumConstants.length);
      return enumConstants[ordinal];
    }
    // normal object
    try {
      Constructor<?> constructor = instanceClass.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    }
    catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException
        | ExceptionInInitializerError | IllegalArgumentException | SecurityException e) {
      throw new SerializationException(e);
    }
  }


  /**
   * Enumeration used to provided typed instance writers.
   * <p>
   * Copied from {@link ServerSerializationStreamWriter.ValueWriter}
   */
  public enum ValueWriter {
    BOOLEAN {
      @Override
      void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
        stream.writeBoolean((Boolean)instance);
      }
    },
    BYTE {
      @Override
      void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
        stream.writeByte((Byte)instance);
      }
    },
    CHAR {
      @Override
      void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
        stream.writeChar((Character)instance);
      }
    },
    DOUBLE {
      @Override
      void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
        stream.writeDouble((Double)instance);
      }
    },
    FLOAT {
      @Override
      void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
        stream.writeFloat((Float)instance);
      }
    },
    INT {
      @Override
      void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
        stream.writeInt((Integer)instance);
      }
    },
    LONG {
      @Override
      void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
        stream.writeLong((Long)instance);
      }
    },
    OBJECT {
      @Override
      void write(SerializationStreamWriter stream, Object instance)
          throws SerializationException {
        stream.writeObject(instance);
      }
    },
    SHORT {
      @Override
      void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
        stream.writeShort((Short)instance);
      }
    },
    STRING {
      @Override
      void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
        stream.writeString((String) instance);
      }
    };

    abstract void write(SerializationStreamWriter stream, Object instance) throws SerializationException;
    
    /**
     * Map of {@link Class} objects to {@link ValueWriter}s.
     */
    private static final Map<Class<?>, ValueWriter> CLASS_TO_WRITER = new IdentityHashMap<>();

    static {
      CLASS_TO_WRITER.put(boolean[].class, ValueWriter.BOOLEAN);
      CLASS_TO_WRITER.put(byte[].class, ValueWriter.BYTE);
      CLASS_TO_WRITER.put(char[].class, ValueWriter.CHAR);
      CLASS_TO_WRITER.put(double[].class, ValueWriter.DOUBLE);
      CLASS_TO_WRITER.put(float[].class, ValueWriter.FLOAT);
      CLASS_TO_WRITER.put(int[].class, ValueWriter.INT);
      CLASS_TO_WRITER.put(long[].class, ValueWriter.LONG);
      CLASS_TO_WRITER.put(Object[].class, ValueWriter.OBJECT);
      CLASS_TO_WRITER.put(short[].class, ValueWriter.SHORT);
      CLASS_TO_WRITER.put(String[].class, ValueWriter.STRING);
    }

    /**
     * @return the {@link ValueWriter} instance that can write a value of the given type.
     */
    @Nonnull
    public static ValueWriter forClass(Class<?> cls) {
      if (CLASS_TO_WRITER.containsKey(cls)) {
        return CLASS_TO_WRITER.get(cls);
      }
      return ValueWriter.OBJECT;
    }
  }


  /**
   * Enumeration used to provided typed instance readers.
   * <p>
   * Copied from {@link ServerSerializationStreamReader.ValueReader}
   */
  public enum ValueReader {
    BOOLEAN {
      @Override
      Object read(SerializationStreamReader stream) throws SerializationException {
        return stream.readBoolean();
      }
    },
    BYTE {
      @Override
      Object read(SerializationStreamReader stream) throws SerializationException {
        return stream.readByte();
      }
    },
    CHAR {
      @Override
      Object read(SerializationStreamReader stream) throws SerializationException {
        return stream.readChar();
      }
    },
    DOUBLE {
      @Override
      Object read(SerializationStreamReader stream) throws SerializationException {
        return stream.readDouble();
      }
    },
    FLOAT {
      @Override
      Object read(SerializationStreamReader stream) throws SerializationException {
        return stream.readFloat();
      }
    },
    INT {
      @Override
      Object read(SerializationStreamReader stream) throws SerializationException {
        return stream.readInt();
      }
    },
    LONG {
      @Override
      Object read(SerializationStreamReader stream) throws SerializationException {
        return stream.readLong();
      }
    },
    OBJECT {
      @Override
      Object read(SerializationStreamReader stream) throws SerializationException {
        return stream.readObject();
      }
    },
    SHORT {
      @Override
      Object read(SerializationStreamReader stream) throws SerializationException {
        return stream.readShort();
      }
    },
    STRING {
      @Override
      Object read(SerializationStreamReader stream) throws SerializationException {
        return stream.readString();
      }
    };

    abstract Object read(SerializationStreamReader stream) throws SerializationException;
    
    /**
     * Map of {@link Class} objects to {@link ValueReader}s.
     */
    private static final Map<Class<?>, ValueReader> CLASS_TO_READER = new IdentityHashMap<>();

    static {
      CLASS_TO_READER.put(boolean[].class, ValueReader.BOOLEAN);
      CLASS_TO_READER.put(byte[].class, ValueReader.BYTE);
      CLASS_TO_READER.put(char[].class, ValueReader.CHAR);
      CLASS_TO_READER.put(double[].class, ValueReader.DOUBLE);
      CLASS_TO_READER.put(float[].class, ValueReader.FLOAT);
      CLASS_TO_READER.put(int[].class, ValueReader.INT);
      CLASS_TO_READER.put(long[].class, ValueReader.LONG);
      CLASS_TO_READER.put(Object[].class, ValueReader.OBJECT);
      CLASS_TO_READER.put(short[].class, ValueReader.SHORT);
      CLASS_TO_READER.put(String[].class, ValueReader.STRING);
    }

    /**
     * @return the {@link ValueReader} instance that can read a value of the given type
     */
    @Nonnull
    public static ValueReader forClass(Class<?> cls) {
      if (CLASS_TO_READER.containsKey(cls)) {
        return CLASS_TO_READER.get(cls);
      }
      return ValueReader.OBJECT;
    }
  }


  /**
   * Enumeration used to provided typed vector writers.
   * <p>
   * Copied from {@link ServerSerializationStreamReader.VectorReader}
   */
  private enum VectorReader {
    BOOLEAN_VECTOR {
      @Override
      void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException {
        Array.setBoolean(array, index, reader.readBoolean());
      }
    },
    BYTE_VECTOR {
      @Override
      void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException {
        Array.setByte(array, index, reader.readByte());
      }
    },
    CHAR_VECTOR {
      @Override
      void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException {
        Array.setChar(array, index, reader.readChar());
      }
    },
    DOUBLE_VECTOR {
      @Override
      void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException {
        Array.setDouble(array, index, reader.readDouble());
      }
    },
    FLOAT_VECTOR {
      @Override
      void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException {
        Array.setFloat(array, index, reader.readFloat());
      }
    },
    INT_VECTOR {
      @Override
      void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException {
        Array.setInt(array, index, reader.readInt());
      }
    },
    LONG_VECTOR {
      @Override
      void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException {
        Array.setLong(array, index, reader.readLong());
      }
    },
    OBJECT_VECTOR {
      @Override
      void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException {
        Array.set(array, index, reader.readObject());
      }
    },
    SHORT_VECTOR {
      @Override
      void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException {
        Array.setShort(array, index, reader.readShort());
      }
    },
    STRING_VECTOR {
      @Override
      void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException {
        Array.set(array, index, reader.readString());
      }
    };

    /**
     * Map of {@link Class} objects to {@link VectorReader}s.
     */
    private static final Map<Class<?>, VectorReader> CLASS_TO_VECTOR_READER = new IdentityHashMap<>();

    static {
      CLASS_TO_VECTOR_READER.put(boolean[].class, VectorReader.BOOLEAN_VECTOR);
      CLASS_TO_VECTOR_READER.put(byte[].class, VectorReader.BYTE_VECTOR);
      CLASS_TO_VECTOR_READER.put(char[].class, VectorReader.CHAR_VECTOR);
      CLASS_TO_VECTOR_READER.put(double[].class, VectorReader.DOUBLE_VECTOR);
      CLASS_TO_VECTOR_READER.put(float[].class, VectorReader.FLOAT_VECTOR);
      CLASS_TO_VECTOR_READER.put(int[].class, VectorReader.INT_VECTOR);
      CLASS_TO_VECTOR_READER.put(long[].class, VectorReader.LONG_VECTOR);
      CLASS_TO_VECTOR_READER.put(Object[].class, VectorReader.OBJECT_VECTOR);
      CLASS_TO_VECTOR_READER.put(short[].class, VectorReader.SHORT_VECTOR);
      CLASS_TO_VECTOR_READER.put(String[].class, VectorReader.STRING_VECTOR);
    }

    /**
     * @return the {@link VectorReader} constant that can read an instance of the given array class.
     */
    @Nonnull
    public static VectorReader forClass(Class<?> cls) {
      if (CLASS_TO_VECTOR_READER.containsKey(cls)) {
        return CLASS_TO_VECTOR_READER.get(cls);
      }
      return VectorReader.OBJECT_VECTOR;
    }

    /**
     * Read a value of the appropriate type from the given stream and write it into the given array at the given index.
     */
    abstract void readAndSet(SerializationStreamReader reader, Object array, int index) throws SerializationException;

    /**
     * Reads all the array elements from the given stream and writes them into the given array.
     */
    public void read(SerializationStreamReader reader, Object array) throws SerializationException {
      int length = Array.getLength(array);
      for (int i = 0; i < length; i++) {
        readAndSet(reader, array, i);
      }
    }
  }

}
