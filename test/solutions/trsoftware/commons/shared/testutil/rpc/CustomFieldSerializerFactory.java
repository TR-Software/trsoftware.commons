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

import com.google.common.collect.ImmutableMap;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import solutions.trsoftware.commons.shared.testutil.MockSerializationStreamWriter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Can be used with {@link MockSerializationStreamWriter} to provide custom serializers for non-primitive types.
 *
 * @author Alex
 * @since 1/13/2023
 */
public class CustomFieldSerializerFactory {

  protected final Map<Class<?>, CustomFieldSerializer<Object>> serializerCache = new ConcurrentHashMap<>();


  public CustomFieldSerializer<Object> getCustomFieldSerializer(Class<?> instanceClass) throws SerializationException {
    return serializerCache.get(instanceClass);
  }

  /**
   * @return the previous serializer associated with this class
   */
  public CustomFieldSerializer<Object> setCustomFieldSerializer(Class<?> instanceClass, CustomFieldSerializer<Object> serializer) {
    return serializerCache.put(instanceClass, serializer);
  }

  public Map<Class<?>, CustomFieldSerializer<Object>> getSerializers() {
    return ImmutableMap.copyOf(serializerCache);
  }
}
