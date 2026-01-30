package solutions.trsoftware.commons.server.util.crypto;

import solutions.trsoftware.commons.server.util.crypto.aes.AESConstants;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Same as {@link SecretKeySpec} but avoids cloning the byte array on every invocation of {@link #getEncoded()}
 *
 * @see AESConstants.AESKeySpec
 */
public class SimpleKeySpec implements SecretKey {
  private final byte[] key;
  private final String algorithm;

  public SimpleKeySpec(byte[] key, String algorithm) {
    this.key = key.clone();  // defensive copy to ensure immutability
    this.algorithm = algorithm;
  }

  @Override
  public String getAlgorithm() {
    return algorithm;
  }
  @Override
  public String getFormat() {
    return "RAW";
  }

  @Override
  public byte[] getEncoded() {
    return key;  // NOTE: not making a defensive copy each time (unlike SecretKeySpec#getEncoded)
  }

  @Override
  public boolean equals(Object obj) {
    // Note: this equals implementation is based on SecretKeySpec.equals, thus is a bit more complicated than strictly necessary
    if (this == obj)
      return true;
    if (!(obj instanceof SecretKey))
      return false;
    SecretKey that = (SecretKey)obj;

    return getAlgorithm().equalsIgnoreCase(that.getAlgorithm())
        && MessageDigest.isEqual(this.key, that.getEncoded());
    // Note: SecretKeySpec.equals uses MessageDigest.isEqual instead of Arrays.equals for security reasons (timing attack resistance)

    // TODO: unit test mutual equality with equivalent SecretKeySpec instance (i.e. new SecretKeySpec(key, "AES"))
  }

  @Override
  public int hashCode() {
    // TODO: this might violate the hashCode contract (e.g. since our equals method compares against any arbitrary SecretKey type)
    return Arrays.hashCode(key);
  }
}
