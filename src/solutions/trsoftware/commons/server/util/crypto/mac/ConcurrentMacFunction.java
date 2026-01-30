package solutions.trsoftware.commons.server.util.crypto.mac;

import solutions.trsoftware.commons.server.util.crypto.MacFunction;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Uses a concurrent pool of cached {@link Mac} instances initialized with a specified key,
 * potentially creating as many {@link Mac} objects as the number of threads concurrently using this {@link MacFunction}.
 * This class can be used as a global singleton in a high-concurrency multithreaded environment.
 * <p>
 * The runtime speed of the hashing operations is comparable to Guava's {@link com.google.common.hash.MacHashFunction},
 * but uses much less memory because it creates just the minimum number of {@link Mac} instances required for full concurrency.
 *
 * @author Alex
 * @since 12/16/2025
 */
public class ConcurrentMacFunction extends MacFunctionPrototype {

  protected final Queue<Mac> macPool = new ConcurrentLinkedQueue<>();

  /**
   * @param algorithm the standard name of the requested MAC algorithm (e.g. "HmacSHA256")
   *   See the Mac section in the <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Mac">
   *   Java Cryptography Architecture Standard Algorithm Name Documentation</a>
   *   for information about standard algorithm names.
   * @param key the key material of the secret key
   * @throws RuntimeException if the algorithm is not supported or the key is inappropriate for initializing this MAC
   */
  public ConcurrentMacFunction(String algorithm, byte[] key) {
    super(algorithm, key);
  }

  /**
   * @param algorithm the standard name of the requested MAC algorithm (e.g. "HmacSHA256")
   *   See the Mac section in the <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Mac">
   *   Java Cryptography Architecture Standard Algorithm Name Documentation</a>
   *   for information about standard algorithm names.
   * @param key the secret key for {@link Mac#init(Key)}
   * @throws RuntimeException if the algorithm is not supported or the key is inappropriate for initializing this MAC
   */
  public ConcurrentMacFunction(String algorithm, SecretKey key) {
    super(algorithm, key);
  }

  @Override
  public byte[] hash(byte[] input, int offset, int len) {
    Mac mac = macPool.poll();
    if (mac == null)
      mac = getMac();
    byte[] ret = MacFunction.hash(mac, input, offset, len);
    macPool.offer(mac);
    return ret;
  }

}
