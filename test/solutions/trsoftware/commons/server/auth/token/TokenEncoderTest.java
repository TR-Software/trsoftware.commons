package solutions.trsoftware.commons.server.auth.token;

import com.google.common.io.BaseEncoding;
import solutions.trsoftware.commons.server.util.crypto.CryptoCipher;
import solutions.trsoftware.commons.server.util.crypto.MacFunction;
import solutions.trsoftware.commons.server.util.crypto.aes.AESCipher;
import solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;
import solutions.trsoftware.commons.server.util.crypto.aes.LocalAESCipher;
import solutions.trsoftware.commons.server.util.crypto.mac.MacFunctionPrototype;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.io.TablePrinter;
import solutions.trsoftware.commons.shared.testutil.TestUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.*;

/**
 * @author Alex
 * @since 10/30/2025
 */
public class TokenEncoderTest extends BaseTestCase {

  private static final byte[] SECRET_KEY_BYTES = Base64.getDecoder().decode("+kDz51P1n+M/MNaR");
  private static final MacFunctionPrototype hmacSHA256 = new MacFunctionPrototype("HmacSHA256", SECRET_KEY_BYTES);
  private static final AESCipher AES_CIPHER;

  static {
    try {
      byte[] key = AESCipher.randomKey();
      AES_CIPHER = new LocalAESCipher(key, Mode.CBC);
    }
    catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a new instance of {@code HmacSHA256} initialized with {@link #SECRET_KEY_BYTES} as the key
   */
  public static MacFunction getHmacSha256() {
    return hmacSHA256;
  }

  /**
   * Tests all {@link TokenEncoder} implementations
   */
  public void testAllEncoders() throws Exception {
    // Note: using a constant timestamp value to allow comparing output between consecutive test runs
    long timestamp = Instant.parse("2025-12-12T03:33:08.332Z").toEpochMilli();

    testEncoders(new IntToken(1234, timestamp),
        Arrays.asList(new IntTokenStringEncoder(), new IntTokenByteBufferEncoder(), new IntTokenEncryptedEncoder()));
    testEncoders(new ByteArrayToken(RandomUtils.randBytes(12), timestamp),
        Arrays.asList(new ByteArrayTokenStringEncoder(), new SignedByteArrayTokenEncoder(), new ByteArrayTokenEncryptedEncoder()));
  }


  private <T extends AuthToken<P>, P> Map<TokenEncoder<T, P>, String> testEncoders(T token, Collection<TokenEncoder<T, P>> tokenEncoders) {
    LinkedHashMap<TokenEncoder<T, P>, String> encodedTokens = new LinkedHashMap<>();

    TestUtils.printSectionHeader(token.toString());
    TablePrinter tp = new TablePrinter();
    for (TokenEncoder<T, P> encoder : tokenEncoders) {
      String tokenString = encoder.encode(token);
//      System.out.printf("%25s(%8s): %s%n", encoder.getClass().getSimpleName(), StringUtils.valueToString(token.getPayload()), tokenString);
      encodedTokens.put(encoder, tokenString);
      tp.newRow()
          .addCol("Encoder", encoder.getClass().getSimpleName())
//          .addCol("payload", StringUtils.valueToString(token.getPayload()))
          .addCol("tokenString", tokenString)
          .addCol("len", tokenString.length())
      ;

      T parsedToken = encoder.decode(tokenString);
      assertEquals(token, parsedToken);
      /*assertEquals(token.getPayload(), parsedToken.getPayload());
      assertEquals(token.getExpiration(), parsedToken.getExpiration());*/
    }
    tp.setColAlignment("tokenString", TablePrinter.TextAlignment.LEFT);
    tp.printTable();

    // test static AuthToken.parse method
    encodedTokens.forEach((encoder, s) -> {
      T parsedAndValidated = AuthToken.parse(s, encoder);
      System.out.println("AuthToken.parse = " + parsedAndValidated);
    });
    // TODO: test encode/decode exceptions and authentication failures

    return encodedTokens;
  }


  static class IntToken extends AuthToken<Integer> {

    /**
     * Creates a new token and computes its {@link #signature} based on the specified args
     *
     * @param payload the token payload (will be converted to a string via {@link #encodePayload()})
     * @param expiration datetime (in epoch millis) when the token expires
     * @throws NullPointerException if {@code payload} is {@code null} or {@link #computeSignature(String)} returns {@code
     *                              null}
     */
    public IntToken(int payload, long expiration) {
      super(payload, expiration);
    }
  }

  static class IntTokenStringEncoder extends SignedStringEncoder<IntToken, Integer> {
    private static final String TYPE_PREFIX = "IntT";

    @Override
    public IntToken createToken(@Nonnull Integer payload, long timestamp) {
      return new IntToken(payload, timestamp);
    }

    @Nonnull
    @Override
    protected Integer parsePayload(String payload) {
      return new Integer(payload);
    }

    @Override
    public String getTypePrefix() {
      return TYPE_PREFIX;
    }

    @Override
    protected MacFunction getMacFunction() {
      return getHmacSha256();
    }
  }

  static class IntTokenByteBufferEncoder extends SignedByteBufferEncoder<IntToken, Integer> {
    private static final byte[] TYPE_PREFIX = Base64.getUrlDecoder().decode("IntT");

    @Override
    public IntToken createToken(@Nonnull Integer payload, long timestamp) {
      return new IntToken(payload, timestamp);
    }

    @Override
    public int getPayloadSize(IntToken token) {
      return Integer.BYTES;
    }

    @Override
    protected void writePayload(Integer payload, ByteBuffer byteBuffer) {
      byteBuffer.putInt(payload);
    }

    @Nonnull
    @Override
    protected Integer readPayload(ByteBuffer byteBuffer) {
      return byteBuffer.getInt();
    }

    @Override
    public byte[] getTypePrefix() {
      return TYPE_PREFIX;
    }

    @Override
    protected MacFunction getMacFunction() {
      return getHmacSha256();
    }
  }

  static class IntTokenEncryptedEncoder extends EncryptedTokenEncoder<IntToken, Integer> {
    private static final byte[] TYPE_PREFIX = Base64.getUrlDecoder().decode("IntT");

    @Override
    public IntToken createToken(@Nonnull Integer payload, long timestamp) {
      return new IntToken(payload, timestamp);
    }

    @Override
    public int getPayloadSize(IntToken token) {
      return Integer.BYTES;
    }

    @Override
    protected void writePayload(Integer payload, ByteBuffer byteBuffer) {
      byteBuffer.putInt(payload);
    }

    @Nonnull
    @Override
    protected Integer readPayload(ByteBuffer byteBuffer) {
      return byteBuffer.getInt();
    }

    @Override
    public byte[] getTypePrefix() {
      return TYPE_PREFIX;
    }

    @Override
    protected CryptoCipher getCipher() {
      return AES_CIPHER;
    }
  }


  static class ByteArrayToken extends AuthToken<byte[]> {
    /**
     * Creates a new token and computes its {@link #signature} based on the specified args
     *
     * @param payload the token payload (will be converted to a string via {@link #encodePayload()})
     * @param expiration datetime (in epoch millis) when the token expires
     * @throws NullPointerException if {@code payload} is {@code null} or {@link #computeSignature(String)} returns {@code
     *                              null}
     */
    public ByteArrayToken(@Nonnull byte[] payload, long expiration) {
      super(payload, expiration);
    }

    // have to override equals and hashCode b/c payload is an array

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      ByteArrayToken that = (ByteArrayToken)o;
      return timestamp == that.timestamp &&
          Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
      int result = Arrays.hashCode(payload);
      result = 31 * result + (int)(timestamp ^ (timestamp >>> 32));
      return result;
    }
  }

  static class ByteArrayTokenStringEncoder extends SignedStringEncoder<ByteArrayToken, byte[]> {
    private static final String TYPE_PREFIX = "BytT";
    private static final String PAYLOAD_PREFIX = "@";

    @Override
    public ByteArrayToken createToken(@Nonnull byte[] payload, long timestamp) {
      return new ByteArrayToken(payload, timestamp);
    }

    @Nonnull
    @Override
    protected String encodePayload(byte[] payload) {
      return PAYLOAD_PREFIX + BaseEncoding.base64Url().encode(payload);  // imitates TS GuestToken
    }

    @Nonnull
    @Override
    protected byte[] parsePayload(String payload) {
      assert payload.startsWith(PAYLOAD_PREFIX);
      return BaseEncoding.base64Url().decode(payload.substring(PAYLOAD_PREFIX.length()));
    }

    @Override
    public String getTypePrefix() {
      return TYPE_PREFIX;
    }

    @Override
    protected MacFunction getMacFunction() {
      return getHmacSha256();
    }
  }

  static class SignedByteArrayTokenEncoder extends SignedByteBufferEncoder<ByteArrayToken, byte[]> {
    private static final byte[] TYPE_PREFIX = Base64.getUrlDecoder().decode("BytT");

    @Override
    public ByteArrayToken createToken(@Nonnull byte[] payload, long timestamp) {
      return new ByteArrayToken(payload, timestamp);
    }

    @Override
    public int getPayloadSize(ByteArrayToken token) {
      return token.getPayload().length;
    }

    @Override
    protected void writePayload(byte[] payload, ByteBuffer byteBuffer) {
      byteBuffer.put(payload);
    }

    @Nonnull
    @Override
    protected byte[] readPayload(ByteBuffer byteBuffer) {
      byte[] payload = new byte[byteBuffer.remaining()];
      byteBuffer.get(payload);
      return payload;
    }

    @Override
    public byte[] getTypePrefix() {
      return TYPE_PREFIX;
    }

    @Override
    protected MacFunction getMacFunction() {
      return getHmacSha256();
    }
  }

  static class ByteArrayTokenEncryptedEncoder extends EncryptedTokenEncoder<ByteArrayToken, byte[]> {
    private static final byte[] TYPE_PREFIX = Base64.getUrlDecoder().decode("BytT");

    @Override
    public ByteArrayToken createToken(@Nonnull byte[] payload, long timestamp) {
      return new ByteArrayToken(payload, timestamp);
    }

    @Override
    public int getPayloadSize(ByteArrayToken token) {
      return token.getPayload().length;
    }

    @Override
    protected void writePayload(byte[] payload, ByteBuffer byteBuffer) {
      byteBuffer.put(payload);
    }

    @Nonnull
    @Override
    protected byte[] readPayload(ByteBuffer byteBuffer) {
      byte[] payload = new byte[byteBuffer.remaining()];
      byteBuffer.get(payload);
      return payload;
    }

    @Override
    public byte[] getTypePrefix() {
      return TYPE_PREFIX;
    }

    @Override
    protected CryptoCipher getCipher() {
      return AES_CIPHER;
    }
  }


}