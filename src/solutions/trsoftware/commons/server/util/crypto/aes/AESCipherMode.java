package solutions.trsoftware.commons.server.util.crypto.aes;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.BLOCK_SIZE;

/**
 * @author Alex
 * @since 12/5/2025
 */
public abstract class AESCipherMode {

  /**
   * Returns the "transformation" value for {@link Cipher#getInstance(String)}.
   *
   * <blockquote cite="https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html#:~:text=In%20order%20to,CBC/PKCS5Padding%22)%3B">
   *   A <i>transformation</i> is a string that describes the operation (or set of operations)
   *   to be performed on the given input, to produce some output.
   *   A transformation always includes the name of a cryptographic algorithm (e.g., <i>AES</i>),
   *   and may be followed by a feedback mode and padding scheme.
   *   <p>
   *   A transformation is of the form: "<i>algorithm/mode/padding</i>" or "<i>algorithm</i>" (in the latter case,
   *   provider-specific default values for the mode and padding scheme are used).
   *   For example, the following is a valid transformation: "<i>AES/CBC/PKCS5Padding</i>"
   * @return
   */
  protected abstract String getTransformationSpec();

  /**
   * @return {@code true} iff this cipher uses padding for encryption.
   */
  protected boolean isPaddingUsed() {
    return !getTransformationSpec().endsWith("NoPadding");
  }

  protected int getIvLength() {
    // this is the default for all AES variants except GCM, which uses 12 (instead of 16) bytes for the IV
    // see com.sun.crypto.provider.CipherCore.init(int, Key, AlgorithmParameterSpec, SecureRandom) line 557
    // TODO: extract this inline comment to method doc
    return BLOCK_SIZE;
  }

  /**
   * @return a new {@link Cipher} instance created via {@link Cipher#getInstance(String)}
   */
  protected Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
    return Cipher.getInstance(getTransformationSpec());
  }

  /**
   * Estimates the value that would be returned by {@link Cipher#getOutputSize(int)}, which is the minimum required length (in bytes)
   * of an output buffer that can receive the result of the next {@code update} or {@code doFinal} operation, given the input length.
   *
   * @param inputLen the input length (in bytes)
   * @param opmode the operating mode of the cipher (e.g. {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE})
   * @return the minimum length in bytes of the output buffer required for the result of the next
   *   {@code update} or {@code doFinal} operation, based on the given the input length.
   */
  public int getOutputSize(int inputLen, int opmode) {
    /*
      This code is based on com.sun.crypto.provider.CipherCore.getOutputSizeByOperation.
      For all AES modes *excluding GCM*, the formula can be simplified to:
        len = bufferedLen + inputLen
        if (padding && !decrypting)
          padLen = blockSize - (len % blockSize)  // see com.sun.crypto.provider.PKCS5Padding.padLength(len), same for ISO10126Padding
        totalLen = len + padLen
     */
    assert !getTransformationSpec().contains("/GCM") : "GCM should override this method";  // the formula is different for GCM
    int totalLen = inputLen;  // assuming no prior input is buffered (since we're not doing any intermediate update ops before doFinal)
    if (isPaddingUsed() && opmode != Cipher.DECRYPT_MODE) {
      int padLen = BLOCK_SIZE - (totalLen % BLOCK_SIZE);  // see com.sun.crypto.provider.PKCS5Padding.padLength(len), same as ISO10126Padding
      totalLen += padLen;
    }
    return totalLen;
  }

  /**
   * Creates the appropriate parameter object for {@link Cipher#init(int, Key, AlgorithmParameters)} from
   * the given IV bytes.
   *
   * @param iv the buffer with the IV
   */
  protected abstract AlgorithmParameterSpec createAlgorithmParameterSpec(byte[] iv);

  /**
   * Creates the appropriate parameter object for {@link Cipher#init(int, Key, AlgorithmParameters)} from
   * the IV bytes in a sub-range of the given buffer.
   *
   * @param iv the buffer with the IV.
   * @param offset the offset in {@code iv} where the IV starts.
   * @param len the number of IV bytes.
   */
  protected abstract AlgorithmParameterSpec createAlgorithmParameterSpec(byte[] iv, int offset, int len);
}
