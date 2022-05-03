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

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStream;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import com.google.gwt.user.server.rpc.impl.TypeNameObfuscator;

import java.io.Serializable;

/**
 * @author Alex
 * @since 4/23/2022
 */
public class SerializationTestUtils {

  public static void printPayloadSize(Serializable payload, SerializationPolicy serializationPolicy) throws SerializationException {
    System.out.printf("%n%s:%n", payload);
    for (int flags : new int[]{AbstractSerializationStream.DEFAULT_FLAGS, AbstractSerializationStream.FLAG_ELIDE_TYPE_NAMES}) {
      String responseType = serializationPolicy.getClass().getSimpleName();
      if (flags == AbstractSerializationStream.FLAG_ELIDE_TYPE_NAMES) {
        if (!(serializationPolicy instanceof TypeNameObfuscator))
          continue;  // this policy doesn't support type name elision (will throw an exception when using this flag)
        responseType += " with name elision";
      }
      String encPayload = serialize(payload, serializationPolicy, flags);
      int payloadSize = encPayload.length();
      System.out.printf("  Encoded using %s (flags=%d, length=%d):%n  %s%n", responseType, flags, payloadSize, encPayload);
    }
  }

  /**
   * Serializes the given object as a GWT-RPC payload.
   *
   * @param obj the object to serialize
   * @param flags flags for {@link ServerSerializationStreamWriter}, e.g. {@link AbstractSerializationStream#FLAG_ELIDE_TYPE_NAMES}
   * @return the given object serialized using the GWT RPC format
   */
  public static String serialize(Serializable obj, SerializationPolicy serializationPolicy, int flags) throws SerializationException {
    ServerSerializationStreamWriter stream = new ServerSerializationStreamWriter(serializationPolicy);
    stream.setFlags(flags);
    stream.prepareToWrite();
    stream.serializeValue(obj, obj.getClass());
    return stream.toString();
  }
}
