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

package solutions.trsoftware.commons.shared.testutil.rpc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A limited implementation of {@link SerializationStreamWriter} that can be used to test
 * {@link CustomFieldSerializer} implementations.
 * <p>
 * GWT provides 2 asymmetric serializers for its RPC protocol: {@link ClientSerializationStreamWriter}
 * and {@link ServerSerializationStreamWriter}, which use different encoding formats.
 * Our mock implementation produces strings that more-closely resemble those produced by
 * {@link ServerSerializationStreamWriter}, but are not fully compatible with either.
 * It's designed to work both in clientside and serverside unit tests, and therefore requires
 * a {@link CustomFieldSerializer} to be {@linkplain #setClassSerializer(Class, CustomFieldSerializer)  provided}
 * in order to serialize any user-defined classes (except enums).
 *
 * @see MockSerializationStreamReader
 * @author Alex
 * @since 4/23/2022
 */
public class MockSerializationStreamWriter extends AbstractSerializationStreamWriter {

  private final ArrayList<String> tokenList = new ArrayList<>();
  private final ArrayList<String> header = new ArrayList<>();
  /**
   * We save the class instances so that {@link MockSerializationStreamReader} can
   * {@linkplain MockSerializationStreamReader#deserialize(String) deserialize} them when running in
   * a GWT clientside environment, which doesn't support {@link Class#forName(String)}.
   */
  private final Map<String, Class<?>> classesByTypeSignature = new LinkedHashMap<>();

  private final CustomFieldSerializerFactory serializerFactory;

  public MockSerializationStreamWriter() {
    this(new CustomFieldSerializerFactory());
  }

  public MockSerializationStreamWriter(CustomFieldSerializerFactory serializerFactory) {
    this.serializerFactory = serializerFactory;
  }

  @SuppressWarnings("unchecked")
  public <T> MockSerializationStreamWriter setClassSerializer(Class<T> key, CustomFieldSerializer<T> serializer) {
    serializerFactory.setCustomFieldSerializer(key, (CustomFieldSerializer<Object>)serializer);
    return this;
  }

  @Override
  public void prepareToWrite() {
    super.prepareToWrite();
    tokenList.clear();
    header.clear();
    classesByTypeSignature.clear();
    header.add(String.valueOf(getFlags()));
    header.add(String.valueOf(getVersion()));
  }

  @Override
  public void writeLong(long value) {
    // NOTE: GWT's implementation uses base64 for longs, but we're just using decimal, for simplicity
    append(Long.toString(value));
  }

  @Override
  protected void append(String token) {
    tokenList.add(token);
  }

  @Override
  protected String getObjectTypeSignature(Object instance) throws SerializationException {
    Class<?> cls = getClassForSerialization(instance);
    String typeSignature = cls.getName(); // abridged, for simplicity
    classesByTypeSignature.put(typeSignature, cls);
    return typeSignature;  // abridged, for simplicity
  }

  @Override
  protected void serialize(Object instance, String typeSignature) throws SerializationException {
    // NOTE: this code is based on com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter.serializeImpl
    Class<?> instanceClass = getClassForSerialization(instance);
    serializeImpl(instance, instanceClass);
  }

  /**
   * @see ServerSerializationStreamWriter#serializeImpl(java.lang.Object, java.lang.Class)
   */
  protected void serializeImpl(Object instance, Class<?> instanceClass) throws SerializationException {
    assert instance != null;
    CustomFieldSerializer<Object> customFieldSerializer = getCustomFieldSerializer(instanceClass);
    if (customFieldSerializer != null) {
      customFieldSerializer.serializeInstance(this, instance);
    }
    else if (instanceClass.isArray()) {
      serializeArray(instance, instanceClass);
    }
    else if (instanceClass.isEnum()) {
      writeInt(((Enum<?>)instance).ordinal());
    }
    else {
      // Regular class instance
      serializeClass(instance, instanceClass);
    }
  }

  @Nullable
  protected CustomFieldSerializer<Object> getCustomFieldSerializer(Class<?> instanceClass) throws SerializationException {
    return serializerFactory.getCustomFieldSerializer(instanceClass);
  }

  /**
   * Serializes an instance that doesn't have a {@link CustomFieldSerializer} and is neither an array nor enum.
   */
  protected void serializeClass(Object instance, Class<?> instanceClass) throws SerializationException {
    /*
     * In order to make this work both client-side and server-side, we require a CustomFieldSerializer for any
     * non-array and non-enum class.
     */
    throw new UnsupportedOperationException(Strings.lenientFormat("%s requires a %s for %s",
        getClass().getSimpleName(), CustomFieldSerializer.class.getSimpleName(), instanceClass.getName()));
  }

  private void serializeArray(Object instance, Class<?> instanceClass) throws SerializationException {
    assert (instanceClass.isArray());
    VectorWriter.forClass(instanceClass).write(this, instance);
  }

  /**
   * Returns the {@link Class} instance to use for serialization. Enumerations
   * are serialized as their declaring class while all others are serialized
   * using their true class instance.
   *
   * @see ServerSerializationStreamWriter#getClassForSerialization(Object)
   */
  private static Class<?> getClassForSerialization(Object instance) {
    assert (instance != null);

    if (instance instanceof Enum<?>) {
      Enum<?> e = (Enum<?>)instance;
      return e.getDeclaringClass();
    }
    else {
      return instance.getClass();
    }
  }

  @Override
  public String toString() {
    // NOTE: this code is roughly based on ServerSerializationStreamWriter.toString
    StringJoiner out = new StringJoiner(",", "[", "]");
    // 1) payload (the tokenList in reversed order)
    out.add(String.join(",", Lists.reverse(tokenList)));  // payload
    // 2) string table
    out.add(stringTableToString());
    // 3) header
    header.forEach(out::add);
    return out.toString();
  }

  private String stringTableToString() {
    return getStringTable().stream().map(StringUtils::quote).collect(Collectors.joining(",", "[", "]"));
  }

  public String toDebugString() {
    return MoreObjects.toStringHelper(this)
        .add("tokenList", tokenList)
        .add("stringTable", stringTableToString())
        .toString();
  }

  @Override
  public ImmutableList<String> getStringTable() {
    // overriding to make public
    return ImmutableList.copyOf(super.getStringTable());
  }

  /**
   * @return the tokens emitted by this writer
   */
  public ImmutableList<String> getTokenList() {
    return ImmutableList.copyOf(tokenList);
  }

  /**
   * @return the tokens emitted by this writer
   */
  public ImmutableList<String> getHeader() {
    return ImmutableList.copyOf(header);
  }

  /**
   * @return the last token emitted by this writer
   */
  public String getLastToken() {
    if (tokenList.isEmpty())
      throw new NoSuchElementException("No tokens written yet");
    return ListUtils.last(tokenList);
  }

  public ImmutableMap<Class<?>, CustomFieldSerializer<Object>> getClassSerializers() {
    return ImmutableMap.copyOf(serializerFactory.getSerializers());
  }

  public ImmutableMap<String, Class<?>> getClassesByTypeSignature() {
    return ImmutableMap.copyOf(classesByTypeSignature);
  }

}
