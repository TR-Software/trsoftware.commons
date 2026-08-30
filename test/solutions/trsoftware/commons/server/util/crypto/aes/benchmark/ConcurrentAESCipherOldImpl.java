package solutions.trsoftware.commons.server.util.crypto.aes.benchmark;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.server.util.crypto.aes.AESCipherImpl;
import solutions.trsoftware.commons.shared.annotations.ThreadSafe;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;

/**
 * Uses cached {@link Cipher} instances pooled in a {@link ConcurrentLinkedQueue} for better
 * concurrency than {@link SynchronizedAESCipher} at the expense of using a bit more memory.
 * <p>
 * Concurrency is achieved by creating as many {@link Cipher} instances as the number of threads
 * concurrently using this instance.
 * <p>
 * The only limiting factor of concurrency is {@link SecureRandom#nextBytes(byte[])}, which is a {@code synchronized}
 * method used to generate initialization vectors for the {@link #encrypt} operations.
 * For best performance, we recommend passing a {@linkplain IvSupplier concurrent IV supplier} to the
 * {@link #ConcurrentAESCipher(byte[], Mode, IvSupplier)} constructor.
 *
 * @see ThreadLocalIvSupplier
 * @see PooledIvSupplier
 *
 * @author Alex
 * @since 12/5/2025
 */
@ThreadSafe
public class ConcurrentAESCipherOldImpl extends AESCipherImpl {

  // TODO: this is a temp copy of the OG ConcurrentAESCipher impl, preserved for CryptoCipherBenchmark2

  /** Pool of cached Cipher instances (like org.apache.catalina.util.SessionIdGeneratorBase.randoms) */
  // TODO(8/18/2026): ref using the new ResourcePool class
  protected final Queue<Cipher> ciphers = new ConcurrentLinkedQueue<>();

  protected IntFunction<byte[]> ivSupplier;

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @throws NullPointerException     if any argument is null
   * @throws IllegalArgumentException if the {@code key} does not contain the required number of bytes
   * @throws RuntimeException         if {@link Cipher#getInstance(String)} threw an exception
   *                                  (i.e. if the current platform doesn't support the given AES mode)
   */
  public ConcurrentAESCipherOldImpl(byte[] key, Mode mode) {
    this(key, mode, null);
  }

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @param ivSupplier a function that generates a given number of bytes to use as the IV during encryption
   *   (see {@link ThreadLocalIvSupplier} and {@link PooledIvSupplier})
   * @throws NullPointerException     if {@code key} or {@code mode} is null
   * @throws IllegalArgumentException if the {@code key} does not contain the required number of bytes
   * @throws RuntimeException         if {@link Cipher#getInstance(String)} threw an exception
   *                                  (i.e. if the current platform doesn't support the given AES mode)
   */
  public ConcurrentAESCipherOldImpl(byte[] key, Mode mode, @Nullable IvSupplier ivSupplier) {
    super(key, mode);
    // TODO: maybe default to PooledIvSupplier if the ivSupplier arg is null?
    this.ivSupplier = ivSupplier;
    // seed the ciphers pool with at least 1 instance, to ensure no exception will be thrown by future Cipher.getInstance invocations
    try {
      ciphers.add(createCipher());
    }
    catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected byte[] initAndDoFinal(int mode, AlgorithmParameterSpec params, byte[] input, int inputOffset, int inputLen) throws GeneralSecurityException {
    Cipher cipher = ciphers.poll();
    if (cipher == null) {
      cipher = createCipher();
    }
    byte[] ret = initAndDoFinal(cipher, mode, params, input, inputOffset, inputLen);
    ciphers.offer(cipher);  // done with this cipher; return it to the pool
    return ret;
  }

  @Override
  protected int initAndDoFinal(int mode, AlgorithmParameterSpec params, byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws GeneralSecurityException {
    Cipher cipher = ciphers.poll();
    if (cipher == null) {
      cipher = createCipher();
    }
    int ret = initAndDoFinal(cipher, mode, params, input, inputOffset, inputLen, output, outputOffset);
    ciphers.offer(cipher);  // done with this cipher; return it to the pool
    return ret;
  }

  @Override
  public byte[] secureRandomBytes(int n) {
    if (ivSupplier != null)
      return ivSupplier.apply(n);
    return super.secureRandomBytes(n);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("ivSupplier", ivSupplier);
  }

  /**
   * A function used for generating a new initialization vector (IV) of the required length.
   *
   * @see SecureRandom#nextBytes(byte[])
   * @see ThreadLocalIvSupplier
   * @see PooledIvSupplier
   */
  @FunctionalInterface
  public interface IvSupplier extends IntFunction<byte[]> {
    byte[] generateIV(int ivLength);

    @Override
    default byte[] apply(int value) {
      return generateIV(value);
    }
  }

  /**
   * Uses {@link SecureRandom} instances pooled in a {@linkplain ConcurrentLinkedQueue concurrent queue},
   * creating as many {@link SecureRandom} instances as the number of threads concurrently accessing
   * the {@link ConcurrentAESCipherOldImpl} instance.
   *
   * @see ConcurrentAESCipherOldImpl#ConcurrentAESCipher(byte[], Mode, IvSupplier)
   * @see ThreadLocalIvSupplier
   */
  public static class PooledIvSupplier implements IvSupplier {
    /** Pool of cached SecureRandom instances (like org.apache.catalina.util.SessionIdGeneratorBase.randoms) */
    // TODO(8/18/2026): ref using the new ResourcePool class
    private final Queue<SecureRandom> randoms = new ConcurrentLinkedQueue<>();
    private final Supplier<SecureRandom> secureRandomSupplier;

    /**
     * Uses the default {@link SecureRandom#SecureRandom()} constructor when a new {@link SecureRandom} instance
     * is needed
     */
    public PooledIvSupplier() {
      this(SecureRandom::new);
    }

    /**
     * Can be used instead of the default constructor if the application needs to use an
     * alternative {@link SecureRandom} {@linkplain SecureRandom#getInstance(String, String) algorithm or provider}.
     *
     * @param secureRandomSupplier supplies a new {@link SecureRandom} instance
     */
    public PooledIvSupplier(Supplier<SecureRandom> secureRandomSupplier) {
      this.secureRandomSupplier = requireNonNull(secureRandomSupplier, "secureRandomSupplier");
    }

    @Override
    public byte[] generateIV(int ivLength) {
      SecureRandom random = randoms.poll();
      if (random == null)
        random = secureRandomSupplier.get();
      byte[] iv = RandomUtils.randBytes(random, ivLength);
      randoms.offer(random);
      return iv;
    }

    @Override
    public String toString() {
      return "SecureRandomPool";
    }
  }

  /**
   * Uses a {@code ThreadLocal<SecureRandom>} to achieve maximum concurrency for IV generation.
   *
   * @see ConcurrentAESCipherOldImpl#ConcurrentAESCipher(byte[], Mode, IvSupplier)
   * @see PooledIvSupplier
   */
  public static class ThreadLocalIvSupplier implements IvSupplier {
    private final ThreadLocal<SecureRandom> threadLocalSecureRandom;

    /**
     * Creates a new {@code ThreadLocal<SecureRandom>} to be used for {@link #generateIV(int)}.
     * <p>
     * This constructor should be used only if the application does not already have a global
     * {@code ThreadLocal<SecureRandom>} for other uses.
     * Otherwise, it makes sense to pass that global instance to
     * the {@link ThreadLocalIvSupplier#ThreadLocalIvSupplier(ThreadLocal)} constructor.
     */
    public ThreadLocalIvSupplier() {
      this(ThreadLocal.withInitial(SecureRandom::new));
    }

    /**
     * Creates a supplier using an existing {@code ThreadLocal<SecureRandom>}, which should be
     * able to provide a {@link ThreadLocal#initialValue()} (e.g. {@code ThreadLocal.withInitial(SecureRandom::new)}).
     * <p>
     * This constructor should be used if the application already has a global {@code ThreadLocal<SecureRandom>}
     * or needs to use an alternative RNG {@linkplain SecureRandom#getInstance(String, String) algorithm or provider}.
     *
     * @param threadLocalSecureRandom a pre-existing {@code ThreadLocal<SecureRandom>} instance whose
     *   {@link ThreadLocal#get() get()} method never returns {@code null}
     *   (i.e. was constructed using {@link ThreadLocal#withInitial(Supplier)} or overrides {@link ThreadLocal#initialValue()})
     * @throws NullPointerException if the argument is null
     */
    public ThreadLocalIvSupplier(ThreadLocal<SecureRandom> threadLocalSecureRandom) {
      this.threadLocalSecureRandom = requireNonNull(threadLocalSecureRandom, "threadLocalSecureRandom");
    }

    @Override
    public byte[] generateIV(int ivLength) {
      SecureRandom random = threadLocalSecureRandom.get();
      if (random == null) {
        // ThreadLocal passed to constructor probably wasn't configured with an initialValue
        // (Note: the constructor can't verify the given ThreadLocal.get doesn't return null for all threads)
        random = new SecureRandom();
        threadLocalSecureRandom.set(random);
      }
      return RandomUtils.randBytes(random, ivLength);
    }

    @Override
    public String toString() {
      return "ThreadLocalSecureRandom";
    }
  }
}
