package solutions.trsoftware.commons.server.util.crypto;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Random;

import static solutions.trsoftware.commons.server.util.ServerStringUtils.urlSafeBase64Decode;
import static solutions.trsoftware.commons.server.util.ServerStringUtils.urlSafeBase64Encode;

public class AESCipherTest extends TestCase {

  private AESCipher cipher;
  private final String plaintextString = "Foo bar baz, пиздец, Foo bar baz";

  public void setUp() throws Exception {
    super.setUp();
    byte[] key = AESCipher.randomKey();
    System.out.println("urlSafeBase64Encode(key) = " + urlSafeBase64Encode(key));
    cipher = new AESCipher(key);
  }

  public void testEncrypt() throws Exception {
    byte[] plaintext = new byte[100];
    new Random().nextBytes(plaintext);
    byte[] result = cipher.encrypt(plaintext);
    assertTrue(Arrays.equals(plaintext, cipher.decrypt(result)));
  }

  public void testEncryptStringUtf8() throws Exception {
    byte[] result = cipher.encryptStringUtf8(plaintextString);
    System.out.println("urlSafeBase64Encode(result) = " + urlSafeBase64Encode(result));
    assertEquals(plaintextString, cipher.decryptStringUtf8(result));
  }

  /** Test version compatibility: that JVM upgrades don't affect the ability to decrypt data encrypted with prior versions. */
  public void testDecryptSpecificExample() throws Exception {
    AESCipher cipher = new AESCipher(urlSafeBase64Decode("-rYWbgDE-MdRsqINoQjOCg**"));
    assertEquals(plaintextString, cipher.decryptStringUtf8(urlSafeBase64Decode("xkCBJFvru779mJK_sdNQwopG6MGPxuSaqUS1JotWdIfJY8FyUFUztsAQO219sgZaWfCvOVsqwSM8TfGFSNG66w**")));
  }

}