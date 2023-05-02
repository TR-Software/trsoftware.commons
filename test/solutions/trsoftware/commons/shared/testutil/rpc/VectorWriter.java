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

package solutions.trsoftware.commons.shared.testutil.rpc;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import solutions.trsoftware.commons.server.testutil.rpc.CustomFieldSerializerFactoryByReflection;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Enumeration used to provide typed vector writers.
 * <p>
 * Copied from {@link ServerSerializationStreamWriter.VectorWriter}
 *
 * @see MockSerializationStreamWriter
 * @see CustomFieldSerializerFactoryByReflection
 */
public enum VectorWriter {
  BOOLEAN_VECTOR {
    @Override
    public void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
      boolean[] vector = (boolean[])instance;
      stream.writeInt(vector.length);
      for (boolean b : vector) {
        stream.writeBoolean(b);
      }
    }
  },
  BYTE_VECTOR {
    @Override
    public void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
      byte[] vector = (byte[])instance;
      stream.writeInt(vector.length);
      for (byte b : vector) {
        stream.writeByte(b);
      }
    }
  },
  CHAR_VECTOR {
    @Override
    public void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
      char[] vector = (char[])instance;
      stream.writeInt(vector.length);
      for (char c : vector) {
        stream.writeChar(c);
      }
    }
  },
  DOUBLE_VECTOR {
    @Override
    public void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
      double[] vector = (double[])instance;
      stream.writeInt(vector.length);
      for (double v : vector) {
        stream.writeDouble(v);
      }
    }
  },
  FLOAT_VECTOR {
    @Override
    public void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
      float[] vector = (float[])instance;
      stream.writeInt(vector.length);
      for (float v : vector) {
        stream.writeFloat(v);
      }
    }
  },
  INT_VECTOR {
    @Override
    public void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
      int[] vector = (int[])instance;
      stream.writeInt(vector.length);
      for (int value : vector) {
        stream.writeInt(value);
      }
    }
  },
  LONG_VECTOR {
    @Override
    public void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
      long[] vector = (long[])instance;
      stream.writeInt(vector.length);
      for (long l : vector) {
        stream.writeLong(l);
      }
    }
  },
  OBJECT_VECTOR {
    @Override
    public void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
      Object[] vector = (Object[])instance;
      stream.writeInt(vector.length);
      for (Object o : vector) {
        stream.writeObject(o);
      }
    }
  },
  SHORT_VECTOR {
    @Override
    public void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
      short[] vector = (short[])instance;
      stream.writeInt(vector.length);
      for (short value : vector) {
        stream.writeShort(value);
      }
    }
  },
  STRING_VECTOR {
    @Override
    public void write(SerializationStreamWriter stream, Object instance) throws SerializationException {
      String[] vector = (String[])instance;
      stream.writeInt(vector.length);
      for (String s : vector) {
        stream.writeString(s);
      }
    }
  };

  public abstract void write(SerializationStreamWriter stream, Object instance) throws SerializationException;

  /**
   * Map of {@link Class} objects to {@link VectorWriter}s.
   */
  private static final Map<Class<?>, VectorWriter> CLASS_TO_VECTOR_WRITER = new IdentityHashMap<>();

  static {
    CLASS_TO_VECTOR_WRITER.put(boolean[].class, VectorWriter.BOOLEAN_VECTOR);
    CLASS_TO_VECTOR_WRITER.put(byte[].class, VectorWriter.BYTE_VECTOR);
    CLASS_TO_VECTOR_WRITER.put(char[].class, VectorWriter.CHAR_VECTOR);
    CLASS_TO_VECTOR_WRITER.put(double[].class, VectorWriter.DOUBLE_VECTOR);
    CLASS_TO_VECTOR_WRITER.put(float[].class, VectorWriter.FLOAT_VECTOR);
    CLASS_TO_VECTOR_WRITER.put(int[].class, VectorWriter.INT_VECTOR);
    CLASS_TO_VECTOR_WRITER.put(long[].class, VectorWriter.LONG_VECTOR);
    CLASS_TO_VECTOR_WRITER.put(Object[].class, VectorWriter.OBJECT_VECTOR);
    CLASS_TO_VECTOR_WRITER.put(short[].class, VectorWriter.SHORT_VECTOR);
    CLASS_TO_VECTOR_WRITER.put(String[].class, VectorWriter.STRING_VECTOR);
  }

  /**
   * @return the {@link VectorWriter} constant that can write an instance of the given array class.
   */
  @Nonnull
  public static VectorWriter forClass(Class<?> cls) {
    if (CLASS_TO_VECTOR_WRITER.containsKey(cls)) {
      return CLASS_TO_VECTOR_WRITER.get(cls);
    }
    return VectorWriter.OBJECT_VECTOR;
  }
}
