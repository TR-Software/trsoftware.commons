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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;

import java.util.*;

/**
 * A limited implementation of {@link SerializationStreamWriter} that can be used together with
 * {@link MockSerializationStreamWriter} to test {@link CustomFieldSerializer} implementations.
 *
 * @author Alex
 * @since 4/23/2022
 */
public class MockSerializationStreamReader extends AbstractSerializationStreamReader {
  private final List<String> tokenList;
  private int tokenListIndex;
  private final List<String> stringTable;
  private final Map<Class<?>, CustomFieldSerializer<Object>> classSerializers;
  private final Map<String, Class<?>> classesByTypeSignature;

  public MockSerializationStreamReader() {
    tokenList = new ArrayList<>();
    stringTable = new ArrayList<>();
    classSerializers = new LinkedHashMap<>();
    classesByTypeSignature = new LinkedHashMap<>();
  }

  public MockSerializationStreamReader(List<String> tokenList, List<String> stringTable, Map<Class<?>, CustomFieldSerializer<Object>> classSerializers, Map<String, Class<?>> classesByTypeSignature) {
    this.tokenList = tokenList;
    this.stringTable = stringTable;
    this.classSerializers = classSerializers;
    this.classesByTypeSignature = classesByTypeSignature;
  }

  public MockSerializationStreamReader(MockSerializationStreamWriter writer) {
    this(
        ImmutableList.copyOf(writer.getTokenList()),
        ImmutableList.copyOf(writer.getStringTable()),
        ImmutableMap.copyOf(writer.getClassSerializers()),
        ImmutableMap.copyOf(writer.getClassesByTypeSignature()));
  }

  @Override
  protected Object deserialize(String typeSignature) throws SerializationException {
    Class<?> instanceClass = Objects.requireNonNull(classesByTypeSignature.get(typeSignature), () ->
        Strings.lenientFormat("Unrecognized type signature '%s'", typeSignature));
    if (instanceClass.isArray())
      return deserializeArray(instanceClass.getComponentType());
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
     * non-array and non-enum class.
     */
    CustomFieldSerializer<Object> serializer = Objects.requireNonNull(classSerializers.get(instanceClass), () ->
        Strings.lenientFormat("%s requires a %s for %s", getClass().getSimpleName(), CustomFieldSerializer.class.getSimpleName(), instanceClass.getName()));
    Object instance = serializer.instantiateInstance(this);
    serializer.deserializeInstance(this, instance);
    return instance;
  }

  private Object deserializeArray(Class<?> componentType) throws SerializationException  {
    throw new UnsupportedOperationException("Method .readArray has not been fully implemented yet.");
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
      return tokenList.get(tokenListIndex++);
    }
    catch (IndexOutOfBoundsException e) {
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
}
