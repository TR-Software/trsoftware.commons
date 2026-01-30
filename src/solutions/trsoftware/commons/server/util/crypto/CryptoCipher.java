package solutions.trsoftware.commons.server.util.crypto;

import solutions.trsoftware.commons.server.util.ServerStringUtils;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 * Provides a simple high-level interface for encryption and decryption, hiding the low-level details of
 * working directly with {@link Cipher javax.crypto.Cipher} instances, managing initialization vectors, etc.
 *
 * @author Alex
 * @since 11/11/2025
 */
public interface CryptoCipher {

  /**
   * Encrypts the given data by invoking {@link Cipher#doFinal(byte[])}
   *
   * @param input binary data to be encrypted.
   * @return an array with the leading bytes containing a randomly-chosen initialization vector (IV), if applicable,
   *     and the rest containing the encrypted data; this array can be passed to {@link #decrypt(byte[])}
   */
  default byte[] encrypt(byte[] input) throws GeneralSecurityException {
    return encrypt(input, 0, input.length);
  }

  /**
   * Encrypts the given data by invoking {@link Cipher#doFinal(byte[], int, int)}
   *
   * @param input the input buffer to be encrypted
   * @param inputOffset the offset in {@code input} where the input starts
   * @param inputLen the input length
   * @return an array with the leading bytes containing a randomly-chosen initialization vector (IV), if applicable,
   *   and the rest containing the encrypted data; this array can be passed to {@link #decrypt(byte[])}
   */
  byte[] encrypt(byte[] input, int inputOffset, int inputLen) throws GeneralSecurityException;


  /**
   * Encrypts the given data by invoking {@link Cipher#doFinal(byte[])}
   * and returns a base64-encoded string representing the encrypted bytes.
   *
   * @param input binary data to be encrypted
   * @param outputEncoder base64 encoder for the output string (e.g. {@link Base64#getEncoder()} or {@link Base64#getUrlEncoder()})
   * @return a base64-encoded string representing the ciphertext bytes
   * @see #decrypt(String, Base64.Decoder)
   */
  default String encrypt(byte[] input, Base64.Encoder outputEncoder) throws GeneralSecurityException {
    return encrypt(input, 0, input.length, outputEncoder);
  }

  /**
   * Encrypts the given data by invoking {@link Cipher#doFinal(byte[], int, int)}
   * and returns a base64-encoded string representing the encrypted bytes.
   *
   * @param input the input buffer to be encrypted
   * @param inputOffset the offset in {@code input} where the input starts
   * @param inputLen the input length
   * @param outputEncoder base64 encoder for the output string (e.g. {@link Base64#getEncoder()} or {@link Base64#getUrlEncoder()})
   * @return a base64-encoded string representing the ciphertext bytes
   * @see #decrypt(String, Base64.Decoder)
   */
  default String encrypt(byte[] input, int inputOffset, int inputLen, Base64.Encoder outputEncoder) throws GeneralSecurityException {
    return outputEncoder.encodeToString(encrypt(input, inputOffset, inputLen));
  }

  /**
   * Reverses the encryption performed by {@link #encrypt(byte[])} or {@link #encrypt(byte[], int, int)}
   *
   * @param ciphertext: an array where the leading bytes contain the IV (if applicable), and the rest containing the
   *   encrypted data
   * @return The decrypted data
   */
  byte[] decrypt(byte[] ciphertext) throws GeneralSecurityException;

  /**
   * Reverses the encryption performed by {@link #encrypt(byte[], Base64.Encoder)} or {@link #encrypt(byte[], int, int, Base64.Encoder)}.
   *
   * @param ciphertext base64-encoded string representing a ciphertext produced by this cipher
   * @return the decrypted data
   */
  default byte[] decrypt(String ciphertext, Base64.Decoder decoder) throws GeneralSecurityException {
    return decrypt(decoder.decode(ciphertext));
  }

  /**
   * Returns the name of the encryption algorithm implemented by this {@link CryptoCipher}.
   * <p>
   * This is the same name that would be passed to {@link Cipher#getInstance(String)}.
   * See the Cipher section in the <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Cipher">
   * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
   * for information about standard transformation names.
   *
   * @return the algorithm name of the {@link Cipher} objects used by this class.
   * @see Cipher#getAlgorithm()
   */
  String getAlgorithm();

  // TODO: maybe add decrypt overloads that write output to a given array (e.g. to facilitate writing to a ByteBuffer)

  // TODO(12/16/2025): maybe add a getAlgorithm method (see javax.crypto.Cipher.getAlgorithm)

  /**
   * @param plaintext this string will be converted to UTF-8 bytes prior to encoding.
   * @return The result of encrypting the given plaintext
   */
  default byte[] encryptStringUtf8(String plaintext) throws GeneralSecurityException {
    return encrypt(ServerStringUtils.stringToBytesUtf8(plaintext));
  }

  /**
   * @param ciphertext: a result of produced by {@link #encryptStringUtf8(String)}:
   * the first 16 bytes contain the initialization vector (IV), and the rest contain the ciphertext.
   * @return The decrypted string.
   */
  default String decryptStringUtf8(byte[] ciphertext) throws GeneralSecurityException {
    return ServerStringUtils.bytesToStringUtf8(decrypt(ciphertext));
  }


}
