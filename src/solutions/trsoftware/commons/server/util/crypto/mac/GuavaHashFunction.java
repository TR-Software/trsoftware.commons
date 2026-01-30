package solutions.trsoftware.commons.server.util.crypto.mac;

import com.google.common.base.MoreObjects;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import solutions.trsoftware.commons.server.util.crypto.MacFunction;

import java.security.Key;

/**
 * {@link MacFunction} adapter for a Guava {@link HashFunction} instance (e.g. {@link Hashing#hmacSha256(Key)}).
 *
 * @author Alex
 * @since 12/16/2025
 */
public class GuavaHashFunction implements MacFunction {
  private final HashFunction hashFunction;

  public GuavaHashFunction(HashFunction hashFunction) {
    // TODO: maybe verify that the given hashFunction is an instance of com.google.common.hash.MacHashFunction?
    this.hashFunction = hashFunction;
  }

  public HashFunction getHashFunction() {
    return hashFunction;
  }

  @Override
  public byte[] hash(byte[] input, int offset, int len) {
    return hashFunction.hashBytes(input, offset, len).asBytes();
  }

  @Override
  public int getMacLength() {
    return hashFunction.bits() / Byte.SIZE;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .addValue(hashFunction)
        .toString();
  }
}
