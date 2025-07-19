/*
 * Copyright 2025 TR Software Inc.
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

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.Base64Utils;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.SerializabilityUtil;
import com.google.gwt.user.server.rpc.impl.TypeNameObfuscator;
import solutions.trsoftware.commons.shared.testutil.rpc.CustomFieldSerializerFactory;
import solutions.trsoftware.commons.shared.testutil.rpc.MockSerializationStreamWriter;

/**
 * @author Alex
 * @since 5/24/2025
 */
public class MockServerSerializationStreamWriter extends MockSerializationStreamWriter {

  private final SerializationPolicy serializationPolicy;

  public MockServerSerializationStreamWriter(SerializationPolicy serializationPolicy) {
    this.serializationPolicy = serializationPolicy;
  }

  public MockServerSerializationStreamWriter(SerializationPolicy serializationPolicy, CustomFieldSerializerFactory serializerFactory) {
    super(serializerFactory);
    this.serializationPolicy = serializationPolicy;
  }

  @Override
  protected String getObjectTypeSignature(Object instance) throws SerializationException {
    // copied from com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter.getObjectTypeSignature
    assert (instance != null);

    Class<?> clazz = getClassForSerialization(instance);
    if (hasFlags(FLAG_ELIDE_TYPE_NAMES)) {
      if (serializationPolicy instanceof TypeNameObfuscator) {
        return ((TypeNameObfuscator) serializationPolicy).getTypeIdForClass(clazz);
      }

      throw new SerializationException("The GWT module was compiled with RPC "
          + "type name elision enabled, but "
          + serializationPolicy.getClass().getName() + " does not implement "
          + TypeNameObfuscator.class.getName());
    } else {
      return SerializabilityUtil.encodeSerializedInstanceReference(clazz, serializationPolicy);
    }
  }

  @Override
  public void writeLong(long value) {
    // copied from com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter.writeLong
    if (getVersion() == SERIALIZATION_STREAM_MIN_VERSION) {
      // Write longs as a pair of doubles for backwards compatibility
      double[] parts = getAsDoubleArray(value);
      assert parts.length == 2;
      writeDouble(parts[0]);
      writeDouble(parts[1]);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append('"');
      sb.append(Base64Utils.toBase64(value));
      sb.append('"');
      append(sb.toString());
    }
  }
}
