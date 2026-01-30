package solutions.trsoftware.commons.server.util.crypto;

import solutions.trsoftware.commons.server.testutil.TestUtils;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.lang.reflect.Constructor;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;

/**
 * @author Alex
 * @since 12/5/2025
 */
public abstract class AESCipherTestCase extends BaseTestCase {

  protected byte[] secretKeyBytes;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    secretKeyBytes = AESCipher.randomKey();
  }

  @Override
  protected void tearDown() throws Exception {
    secretKeyBytes = null;
    super.tearDown();
  }

  /**
   * Tests encryption and decryption using the given cipher instance.
   *
   * @param cipher the cipher instance to test
   * @param plaintext argument for {@link CryptoCipher#encrypt(byte[])}
   * @return the return value of {@link CryptoCipher#encrypt(byte[])} for the given bytes
   * @throws GeneralSecurityException if thrown by cipher instance
   */
  protected byte[] testEncryption(CryptoCipher cipher, byte[] plaintext) throws GeneralSecurityException {
    byte[] encrypted = cipher.encrypt(plaintext);
    assertFalse(Arrays.equals(plaintext, encrypted));
    System.out.printf("%s.encrypt(byte[%d]=0x[%s]) ->%n  byte[%d]=0x[%s]%n", cipher.getClass().getSimpleName(),
        plaintext.length, StringUtils.byteArrayToHex(plaintext, 4),
        encrypted.length, StringUtils.byteArrayToHex(encrypted, 4));
    byte[] decrypted = cipher.decrypt(encrypted);
    assertArraysEqual(plaintext, decrypted);
    assertFalse(Arrays.equals(plaintext, encrypted));

    // test the overloaded encryption methods: e.g. encrypt(byte[], Base64.Encoder), encryptStringUtf8(String)
    assertArraysEqual(plaintext, cipher.decrypt(cipher.encrypt(plaintext, 0, plaintext.length)));
    String base64 = cipher.encrypt(plaintext, Base64.getUrlEncoder());
    System.out.printf("   b64[%d]=0x[%s]%n", base64.length(), base64);
    assertArraysEqual(plaintext, cipher.decrypt(base64, Base64.getUrlDecoder()));
    assertEquals("foobar", cipher.decryptStringUtf8(cipher.encryptStringUtf8("foobar")));

    return encrypted;
  }

  protected <T extends CryptoCipher> List<T> testAllImplementationsOf(Class<T> baseClass) throws Exception {
    List<Class<T>> implClasses = TestUtils.findSubClassesOf(baseClass);
    byte[] input = RandomUtils.randBytes(17);  // Note: using array size different from AES block size (16), to test padding logic (if any)
    List<T> ciphers = new ArrayList<>();
    for (Class<T> cls : implClasses) {
      TestUtils.printSectionHeader("Testing " + cls.getSimpleName());
      Constructor<T> constructor = cls.getConstructor(byte[].class);
      T cipher = constructor.newInstance((Object)secretKeyBytes);
      ciphers.add(cipher);
      testEncryption(cipher, input);
    }
    return ciphers;
    // TODO: test that all impls produce the same result when given the same IV
    // make sure all impls produce the same output for the same IV
    /*byte[] iv = RandomUtils.randBytes(CryptoCipher.IV_LENGTH);
    Set<String> results = new LinkedHashSet<>();
    String expectedResult = null;
    for (CryptoCipher cipher : ciphers) {
      String clsName = cipher.getClass().getSimpleName();
      if (clsName.contains("_NoIV")) {
        // the "_NoIV" impls don't use an IV array for Cipher.init (they rely on the default IV gen behavior of the cipher class),
        // so we can't expect them to produce the same result each time
        continue;
      }
      String result = new ForwardingAESCipherGCM_FixedIV(cipher, iv)
          .encrypt(input, BaseEncoding.base64Url());
      // TODO: cont here: ForwardingAESCipherGCM_FixedIV doesn't work b/c delegate invokes its own generateIV method
      if (expectedResult == null)
        expectedResult = result;
      else {
        // this result should be the same as all preceding results
        assertEquals(clsName, expectedResult, result);
      }
    }*/
  }
}
