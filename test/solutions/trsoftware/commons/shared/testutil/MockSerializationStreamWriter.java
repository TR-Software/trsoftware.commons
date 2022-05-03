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

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

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
  private final Map<Class<?>, CustomFieldSerializer<Object>> classSerializers = new LinkedHashMap<>();
  /**
   * We save the class instances so that {@link MockSerializationStreamReader} can
   * {@linkplain MockSerializationStreamReader#deserialize(String) deserialize} them when running in
   * a GWT clientside environment, which doesn't support {@link Class#forName(String)}.
   */
  private final Map<String, Class<?>> classesByTypeSignature = new LinkedHashMap<>();

  public <T> MockSerializationStreamWriter setClassSerializer(Class<T> key, CustomFieldSerializer<T> serializer) {
    classSerializers.put(key, (CustomFieldSerializer<Object>)serializer);
    return this;
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
    if (instanceClass.isArray()) {
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

  private void serializeClass(Object instance, Class<?> instanceClass) throws SerializationException {
    /*
     * In order to make this work both client-side and server-side, we require a CustomFieldSerializer for any
     * non-array and non-enum class.
     */
    CustomFieldSerializer<Object> serializer = Objects.requireNonNull(classSerializers.get(instanceClass),
        () -> Strings.lenientFormat("%s requires a %s for %s", getClass().getSimpleName(), CustomFieldSerializer.class.getSimpleName(), instanceClass.getName()));
    serializer.serializeInstance(this, instance);
  }

  private void serializeArray(Object instance, Class<?> instanceClass) {
    // TODO: impl this
    throw new UnsupportedOperationException("TODO");
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
    out.add(String.valueOf(getFlags()));
    out.add(String.valueOf(getVersion()));
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
  public List<String> getStringTable() {
    // overriding to make public
    return super.getStringTable();
  }

  /**
   * @return the tokens emitted by this writer
   */
  public ArrayList<String> getTokenList() {
    return tokenList;
  }

  /**
   * @return the last token emitted by this writer
   */
  public String getLastToken() {
    if (tokenList.isEmpty())
      throw new IllegalStateException("No tokens written yet");
    return ListUtils.last(tokenList);
  }

  public Map<Class<?>, CustomFieldSerializer<Object>> getClassSerializers() {
    return classSerializers;
  }

  public Map<String, Class<?>> getClassesByTypeSignature() {
    return classesByTypeSignature;
  }
}
