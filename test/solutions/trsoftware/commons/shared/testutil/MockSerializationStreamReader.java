/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.testutil;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * A limited implementation of {@link SerializationStreamWriter} that can be used together with
 * {@link MockSerializationStreamWriter} to test {@link CustomFieldSerializer} implementations.
 *
 * @author Alex
 * @since 4/23/2022
 */
public class MockSerializationStreamReader extends AbstractSerializationStreamReader {
  /*
    TODO: make sure this supports back-references (objects already deserialized); see
      - com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamReader.rememberDecodedObject
   */

  private final List<String> tokenList;
  private final List<String> header;
  private Iterator<String> tokenIterator;
  private final List<String> stringTable;
  private final Map<Class<?>, CustomFieldSerializer<Object>> classSerializers;
  private final Map<String, Class<?>> classesByTypeSignature;

  public MockSerializationStreamReader() {
    this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
  }

  public MockSerializationStreamReader(List<String> tokenList, List<String> header, List<String> stringTable, Map<Class<?>, CustomFieldSerializer<Object>> classSerializers, Map<String, Class<?>> classesByTypeSignature) {
    this.tokenList = Objects.requireNonNull(tokenList, "tokenList");
    this.header = Objects.requireNonNull(header, "header");
    this.stringTable = Objects.requireNonNull(stringTable, "stringTable");
    this.classSerializers = Objects.requireNonNull(classSerializers, "classSerializers");
    this.classesByTypeSignature = Objects.requireNonNull(classesByTypeSignature, "classesByTypeSignature");
    tokenIterator = Iterables.concat(header, tokenList).iterator();
  }

  public MockSerializationStreamReader(MockSerializationStreamWriter writer) {
    this(
        writer.getTokenList(),
        writer.getHeader(),
        writer.getStringTable(),
        writer.getClassSerializers(),
        writer.getClassesByTypeSignature());
  }

  @Override
  public void prepareToRead(String encoded) throws SerializationException {
    // TODO: maybe support parsing the data structures from the given string
    tokenIterator = Iterables.concat(header, tokenList).iterator();
    super.prepareToRead(encoded);
  }

  @Override
  protected Object deserialize(String typeSignature) throws SerializationException {
    Class<?> instanceClass = Objects.requireNonNull(classesByTypeSignature.get(typeSignature), () ->
        Strings.lenientFormat("Unrecognized type signature '%s'", typeSignature));
    return deserializeImpl(instanceClass);
  }

  private Object deserializeImpl(Class<?> instanceClass) throws SerializationException {
    CustomFieldSerializer<Object> customFieldSerializer = classSerializers.get(instanceClass);
    if (customFieldSerializer != null) {
      Object instance = customFieldSerializer.instantiateInstance(this);
      customFieldSerializer.deserializeInstance(this, instance);
      return instance;
    }
    else if (instanceClass.isArray())
      return deserializeArray(instanceClass);
    else if (instanceClass.isEnum()) {
      Enum<?>[] enumConstants = (Enum[])instanceClass.getEnumConstants();
      int ordinal = readInt();
      assert (ordinal >= 0 && ordinal < enumConstants.length);
      return enumConstants[ordinal];
    } else {
      // Regular class
      return deserializeClass(instanceClass);
    }
  }

  private Object deserializeClass(Class<?> instanceClass) throws SerializationException {
    /*
     * In order to make this work both client-side and server-side, we require a CustomFieldSerializer for any
     * non-primitive array and non-enum class.
     */
    CustomFieldSerializer<Object> customFieldSerializer = Objects.requireNonNull(classSerializers.get(instanceClass), () ->
        getUnsupportedClassMessage(instanceClass));
    Object instance = customFieldSerializer.instantiateInstance(this);
    customFieldSerializer.deserializeInstance(this, instance);
    return instance;
  }

  @Nonnull
  private String getUnsupportedClassMessage(Class<?> instanceClass) {
    return Strings.lenientFormat("%s requires a %s for %s", getClass().getSimpleName(), CustomFieldSerializer.class.getSimpleName(), instanceClass.getName());
  }

  private Object deserializeArray(Class<?> arrayClass) throws SerializationException  {
    VectorReader vectorReader = VectorReader.forClass(arrayClass);
    if (vectorReader != null) {
      int length = readInt();
      return vectorReader.read(this, arrayClass.getComponentType(), length);
    }
    // can't deserialize a generic Object array b/c Array.newInstance isn't emulated in GWT
    // TODO: maybe just read it as new Object[]?  Won't be typesafe, but maybe that's okay
    throw new UnsupportedOperationException(getUnsupportedClassMessage(arrayClass));
  }

  @Override
  protected String getString(int index) {
    // index is 1-based
    return index > 0 ? stringTable.get(index - 1) : null;
  }

  /**
   * @return the next token
   * @see ServerSerializationStreamReader#extract()
   */
  private String next() throws SerializationException {
    try {
      return tokenIterator.next();
    }
    catch (NoSuchElementException e) {
      throw new SerializationException("Too few tokens in RPC request", e);
    }
  }

  @Override
  public boolean readBoolean() throws SerializationException {
    return !"0".equals(next());
  }

  @Override
  public byte readByte() throws SerializationException {
    return Byte.parseByte(next());
  }

  @Override
  public char readChar() throws SerializationException {
    return (char)readInt();
  }

  @Override
  public double readDouble() throws SerializationException {
    return Double.parseDouble(next());
  }

  @Override
  public float readFloat() throws SerializationException {
    return (float)readDouble();
  }

  @Override
  public int readInt() throws SerializationException {
    return Integer.parseInt(next());
  }

  @Override
  public long readLong() throws SerializationException {
    // NOTE: this matches MockSerializationStreamWriter, which writes longs as decimal ints instead of base64
    return Long.parseLong(next());
  }

  @Override
  public short readShort() throws SerializationException {
    return Short.parseShort(next());
  }

  @Override
  public String readString() throws SerializationException {
    return getString(readInt());
  }

  /**
   * Enumeration used to provided typed vector writers.
   * <p>
   * Copied from {@link ServerSerializationStreamReader.VectorReader}
   */
  private enum VectorReader {
    BOOLEAN_VECTOR {
      @Override
      Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException {
        boolean[] arr = new boolean[length];
        for (int i = 0; i < arr.length; i++) {
          arr[i] = reader.readBoolean();
        }
        return arr;
      }
    },
    BYTE_VECTOR {
      @Override
      Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException {
        byte[] arr = new byte[length];
        for (int i = 0; i < arr.length; i++) {
          arr[i] = reader.readByte();
        }
        return arr;
      }
    },
    CHAR_VECTOR {
      @Override
      Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException {
        char[] arr = new char[length];
        for (int i = 0; i < arr.length; i++) {
          arr[i] = reader.readChar();
        }
        return arr;
      }
    },
    DOUBLE_VECTOR {
      @Override
      Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException {
        double[] arr = new double[length];
        for (int i = 0; i < arr.length; i++) {
          arr[i] = reader.readDouble();
        }
        return arr;
      }
    },
    FLOAT_VECTOR {
      @Override
      Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException {
        float[] arr = new float[length];
        for (int i = 0; i < arr.length; i++) {
          arr[i] = reader.readFloat();
        }
        return arr;
      }
    },
    INT_VECTOR {
      @Override
      Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException {
        int[] arr = new int[length];
        for (int i = 0; i < arr.length; i++) {
          arr[i] = reader.readInt();
        }
        return arr;
      }
    },
    LONG_VECTOR {
      @Override
      Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException {
        long[] arr = new long[length];
        for (int i = 0; i < arr.length; i++) {
          arr[i] = reader.readLong();
        }
        return arr;
      }
    },
    SHORT_VECTOR {
      @Override
      Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException {
        short[] arr = new short[length];
        for (int i = 0; i < arr.length; i++) {
          arr[i] = reader.readShort();
        }
        return arr;
      }
    },
    STRING_VECTOR {
      @Override
      Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException {
        String[] arr = new String[length];
        for (int i = 0; i < arr.length; i++) {
          arr[i] = reader.readString();
        }
        return arr;
      }
    },
    OBJECT_VECTOR {
      @Override
      Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException {
        if (componentType != Object.class) {
          throw new UnsupportedOperationException("Reading a generic Object array requires Array.newInstance, which is not emulated in GWT");
        }
        Object[] arr = new Object[length];
        for (int i = 0; i < arr.length; i++) {
          arr[i] = reader.readObject();
        }
        return arr;
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
    @Nullable
    public static VectorReader forClass(Class<?> cls) {
      return CLASS_TO_VECTOR_READER.get(cls);
    }

    abstract Object read(SerializationStreamReader reader, Class<?> componentType, int length) throws SerializationException;
  }

}
