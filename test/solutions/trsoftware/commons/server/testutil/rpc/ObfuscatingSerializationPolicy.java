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

package solutions.trsoftware.commons.server.testutil.rpc;

import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStream;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.TypeNameObfuscator;
import solutions.trsoftware.commons.server.util.rpc.SimpleSerializationPolicy;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A mock {@link SerializationPolicy} that supports type name obfuscation, which can be enabled with the
 * {@link AbstractSerializationStream#FLAG_ELIDE_TYPE_NAMES} flag.
 * @author Alex
 * @since 4/23/2022
 */
public class ObfuscatingSerializationPolicy extends SimpleSerializationPolicy implements TypeNameObfuscator {
  @Nonnull
  private final BiMap<Class<?>, String> classToTypeId;
  private int nextId = 1;

  public ObfuscatingSerializationPolicy() {
    classToTypeId = HashBiMap.create();
  }

  public ObfuscatingSerializationPolicy(@Nonnull BiMap<Class<?>, String> obfuscatedTypeName) {
    classToTypeId = Objects.requireNonNull(obfuscatedTypeName);
  }

  @Override
  public String getClassNameForTypeId(String id) throws SerializationException {
    /*
     * NOTE: this method is only called for deserialization,
     * so it's okay if the mapping is missing if we're only testing serialization
     */
    BiMap<String, Class<?>> typeIdToClass = classToTypeId.inverse();
    if (!typeIdToClass.containsKey(id))
      throw new SerializationException(String.format("No typeId to class mapping for '%s'", id));
    return typeIdToClass.get(id).getName();
  }

  @Override
  public String getTypeIdForClass(Class<?> cls) throws SerializationException {
    ensureTypeId(cls);
    return classToTypeId.get(cls);
  }

  /**
   * Creates a new typeId mapping for the given class if needed
   */
  private void ensureTypeId(Class<?> cls) {
    // create a new mapping if needed
    if (!classToTypeId.containsKey(cls)) {
      synchronized (this) {
        if (!classToTypeId.containsKey(cls)) {
          String id = generateTypeId(cls);
          classToTypeId.put(cls, id);
        }
      }
    }
  }

  @Nonnull
  protected String generateTypeId(Class<?> cls) {  // subclasses can override if desired
    String id;
    do {
      id = Integer.toString(nextId++, 36);  // same number encoding as GWT uses to generate .gwt.rpc policy files
    }
    while (classToTypeId.containsValue(id));
    return id;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("classToTypeId", classToTypeId)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ObfuscatingSerializationPolicy that = (ObfuscatingSerializationPolicy)o;

    if (nextId != that.nextId)
      return false;
    return classToTypeId.equals(that.classToTypeId);
  }

  @Override
  public int hashCode() {
    int result = classToTypeId.hashCode();
    result = 31 * result + nextId;
    return result;
  }
}
