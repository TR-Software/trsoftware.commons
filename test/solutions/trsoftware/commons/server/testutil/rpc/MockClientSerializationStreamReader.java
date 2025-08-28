package solutions.trsoftware.commons.server.testutil.rpc;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.Base64Utils;
import solutions.trsoftware.commons.shared.testutil.rpc.MockSerializationStreamReader;
import solutions.trsoftware.commons.shared.testutil.rpc.MockSerializationStreamWriter;

import java.util.List;
import java.util.Map;

/**
 * Reads objects serialized using a {@link MockServerSerializationStreamWriter}.
 *
 * @author Alex
 * @since 8/5/2025
 */
public class MockClientSerializationStreamReader extends MockSerializationStreamReader {

  public MockClientSerializationStreamReader() {
  }

  public MockClientSerializationStreamReader(List<String> tokenList, List<String> header, List<String> stringTable, Map<Class<?>, CustomFieldSerializer<Object>> classSerializers, Map<String, Class<?>> classesByTypeSignature) {
    super(tokenList, header, stringTable, classSerializers, classesByTypeSignature);
  }

  public MockClientSerializationStreamReader(MockSerializationStreamWriter writer) {
    super(writer);
  }

  @Override
  public long readLong() throws SerializationException {
    // NOTE: this matches MockServerSerializationStreamWriter, which writes longs the same as GWT's ServerSerializationStreamWriter
    if (getVersion() == SERIALIZATION_STREAM_MIN_VERSION) {
      return (long) readDouble() + (long) readDouble();
    } else {
      String token = next();
      // unquote the string token (in GWT's ClientSerializationStreamReader this happens automatically b/c the token is parsed as a JsStringLiteral)
      if (!token.startsWith("\"") || !token.endsWith("\""))
        throw new SerializationException("Unquoted token: " + token);
      String value = token.substring(1, token.length() - 1);
      return Base64Utils.longFromBase64(value);
    }
  }

  /* TODO: might need to also handle object back-refs (negative indices):
      @see
        - com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamWriter.getIndexForObject / saveIndexForObject
        - com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamReader.rememberDecodedObject / reserveDecodedObjectIndex / getDecodedObject
          (usage example: com.google.gwt.user.client.rpc.impl.ClientSerializationStreamReader.deserialize)
  */
}
