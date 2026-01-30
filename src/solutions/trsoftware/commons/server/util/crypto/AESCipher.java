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

import solutions.trsoftware.commons.server.util.crypto.aes.*;

import javax.crypto.Cipher;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;

/**
 * Uses the Advanced Encryption Standard (AES) algorithm with block size {@value AESConstants#BLOCK_SIZE} (128-bit encryption)
 * in {@link AESConstants.Mode#CBC CBC} mode with {@code PKC5Padding} to encrypt/decrypt strings or byte arrays.
 * <p>
 * <i>Note:</i> this class is immutable and thread-safe, which is achieved by creating a new {@link Cipher} instance for every
 * operation, making it suboptimal for reuse, since {@link Cipher#getInstance(String)} is a costly operation.
 * <p>
 * The recommended alternatives are the new {@link SynchronizedAESCipher} and {@link ConcurrentAESCipher} classes,
 * which can be used as a global singletons shared by multiple threads, and allow different AES {@linkplain AESCipherMode modes},
 * such as {@link AESCipherMode_GCM GCM}.
 *
 * @see solutions.trsoftware.commons.server.util.crypto.aes.AESCipher
 * @see ConcurrentAESCipher
 * @see SynchronizedAESCipher
 * @author Alex, 5/1/2015
 * @deprecated Use one of the newer {@link solutions.trsoftware.commons.server.util.crypto.aes.AESCipher AESCipher}
 *   implementations instead, all of which provide better performance than this original class.
 *   The recommended replacement is {@link ConcurrentAESCipher}.
 */
public class AESCipher extends solutions.trsoftware.commons.server.util.crypto.aes.AESCipher  {

  // TODO(12/15/2025): maybe extend SynchronizedAESCipher, to make this deprecated class at least somewhat useful (without changing the OG functionality)

  /**
   * @param key a 16, 24, or 32-byte secret key;
   * According Google AI, key size has the following implications for the AES/CBC algorithm:
   *  <ul>
   *    <li>128-bit key (16 bytes): Uses 10 rounds.
   *    <li>192-bit key (24 bytes): Uses 12 rounds.
   *    <li>256-bit key (32 bytes): Uses 14 rounds.
   *  </ul>
   * @throws NullPointerException if the argument is null
   * @throws IllegalArgumentException if the argument does not contain the required number of bytes
   */
  public AESCipher(byte[] key) {
    super(key, Mode.CBC);
  }

}
