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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStream;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.TypeNameObfuscator;
import solutions.trsoftware.commons.server.util.rpc.SimpleSerializationPolicy;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.Objects;

/**
 * A mock {@link SerializationPolicy} that supports type name obfuscation, which can be enabled with the
 * {@link AbstractSerializationStream#FLAG_ELIDE_TYPE_NAMES} flag.
 * @author Alex
 * @since 4/23/2022
 */
public class ObfuscatingSerializationPolicy extends SimpleSerializationPolicy implements TypeNameObfuscator {
  private final BiMap<Class<?>, String> classToTypeId;

  public ObfuscatingSerializationPolicy() {
    classToTypeId = HashBiMap.create();
  }

  public ObfuscatingSerializationPolicy(BiMap<Class<?>, String> obfuscatedTypeName) {
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
      throw new IllegalStateException(String.format("No typeId to class mapping for '%s'", id));
    return typeIdToClass.get(id).getName();
  }

  @Override
  public String getTypeIdForClass(Class<?> clazz) throws SerializationException {
    ensureTypeId(clazz);
    return classToTypeId.get(clazz);
  }

  /**
   * Creates a new typeId mapping for the given class if needed
   */
  private void ensureTypeId(Class<?> clazz) {
    // create a new mapping if needed
    if (!classToTypeId.containsKey(clazz)) {
      synchronized (this) {
        if (!classToTypeId.containsKey(clazz)) {
          String randId;
          do {
            randId = RandomUtils.randString(2, StringUtils.ASCII_LETTERS_AND_NUMBERS);
          }
          while (classToTypeId.containsValue(randId));
          classToTypeId.put(clazz, randId);
        }
      }
    }
  }
}
