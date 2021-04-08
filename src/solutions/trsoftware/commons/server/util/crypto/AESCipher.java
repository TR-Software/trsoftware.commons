/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.server.util.crypto;

import solutions.trsoftware.commons.server.util.ServerStringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Uses the Advanced Encryption Standard (AES) algorithm with block size 16 (128-bit encryption)
 * in CBC mode with PKC5Padding to perform crypto on strings.
 *
 * Reference docs:
   http://www.javamex.com/tutorials/cryptography/symmetric.shtml
 *
 * @author Alex, 5/1/2015
 */
public class AESCipher {

  private static final String ALGORITHM = "AES";
  public static final String TRANSFORMATION_SPEC = ALGORITHM + "/CBC/PKCS5Padding";

  private final SecretKeySpec secretKeySpec;

  /**
   * @param key A 16-byte secret key.
   */
  public AESCipher(byte[] key) {
    secretKeySpec = new SecretKeySpec(key, ALGORITHM);
  }

  /**
   * @param plaintext binary data to be encrypted.
   * @return The result of encrypting the given plaintext: the first 16 bytes will contain the initialization vector (IV),
   * which will be chosen at random, and the rest will contain the ciphertext.
   */
  public byte[] encrypt(byte[] plaintext) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance(TRANSFORMATION_SPEC);
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
    byte[] ciphertext = cipher.doFinal(plaintext);
    byte[] iv = cipher.getIV();
    byte[] ret = new byte[ciphertext.length + iv.length];
    System.arraycopy(iv, 0, ret, 0, iv.length);
    System.arraycopy(ciphertext, 0, ret, iv.length, ciphertext.length);
    return ret;
  }

  /**
   * @param ciphertext: A result of invoking {@link #encrypt(byte[])}:
   * the first 16 bytes contain the initialization vector (IV), and the rest contain the ciphertext.
   * @return The decrypted data.
   */
  public byte[] decrypt(byte[] ciphertext) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance(TRANSFORMATION_SPEC);
    int blockSize = cipher.getBlockSize();
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ciphertext, 0, blockSize));
    return cipher.doFinal(ciphertext, blockSize, ciphertext.length - blockSize);
  }

  /**
   * @param plaintext this string will be converted to UTF-8 bytes prior to encoding.
   * @return The result of encrypting the given plaintext
   */
  public byte[] encryptStringUtf8(String plaintext) throws GeneralSecurityException {
    return encrypt(ServerStringUtils.stringToBytesUtf8(plaintext));
  }

  /**
   * @param ciphertext: A result of invoking {@link #encryptStringUtf8(String)}:
   * the first 16 bytes contain the initialization vector (IV), and the rest contain the ciphertext.
   * @return The decrypted string.
   */
  public String decryptStringUtf8(byte[] ciphertext) throws GeneralSecurityException {
    return ServerStringUtils.bytesToStringUtf8(decrypt(ciphertext));
  }


  /**
   * @return a random key that can be used with this algorithm
   */
  public static byte[] randomKey() throws GeneralSecurityException {
    KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
    SecretKey secretKey = keyGen.generateKey();
    return secretKey.getEncoded();
  }

  /** Generates a random key */
  public static void main(String[] args) throws GeneralSecurityException {
    byte[] key = randomKey();
    System.out.println("Random key:");
    System.out.println("Bytes: " + Arrays.toString(key));
    System.out.println("urlSafeBase64 encoding: " + ServerStringUtils.urlSafeBase64Encode(key));

  }


}
