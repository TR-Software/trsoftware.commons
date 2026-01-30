package solutions.trsoftware.commons.server.util.crypto.mac;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import solutions.trsoftware.commons.server.util.crypto.MacFunction;
import solutions.trsoftware.commons.server.util.crypto.MacFunctionTestCase;

/**
 * @author Alex
 * @since 1/13/2026
 */
public class GuavaHashFunctionTest extends MacFunctionTestCase {

  protected MacFunction createMacFunction(String algorithm, byte[] keyBytes) {
    // look up the static method in com.google.common.hash.Hashing that corresponds to the given algorithm
//    String methodName = Character.toLowerCase(algorithm.charAt(0)) + algorithm.substring(1);
    return new GuavaHashFunction(getHashFunction(algorithm, keyBytes));
  }

  /*private HashFunction getHashFunction(String algorithm, byte[] keyBytes) {
    HashFunction hashFunction;
    //    String methodName = Character.toLowerCase(algorithm.charAt(0)) + algorithm.substring(1);
    String methodName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, algorithm);
    try {
      Method method = Hashing.class.getMethod(methodName, byte[].class);
      hashFunction = (HashFunction)method.invoke(null, keyBytes);
    }
    catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
    return hashFunction;
  }*/

  /**
   * Invokes the static method in {@link Hashing} that returns the function with the specified algorithm
   * @param algorithm
   * @param keyBytes
   * @return
   */
  private HashFunction getHashFunction(String algorithm, byte[] keyBytes) {
    switch (algorithm) {
      // these are the only MAC algorithms supported by com.google.common.hash.Hashing:
      case "HmacSHA1":
        return Hashing.hmacSha1(keyBytes);
      case "HmacSHA256":
        return Hashing.hmacSha256(keyBytes);
      case "HmacSHA512":
        return Hashing.hmacSha512(keyBytes);
      case "HmacMD5":
        return Hashing.hmacMd5(keyBytes);
    }
    throw new IllegalArgumentException(String.format("%s does not support the \"%s\" algorithm",
        Hashing.class.getName(), algorithm));
  }
}