package solutions.trsoftware.commons.shared.io;

import solutions.trsoftware.commons.shared.BaseTestCase;

import static solutions.trsoftware.commons.shared.io.Base64Encoding.*;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;

/**
 * @author Alex
 * @since 12/20/2025
 */
public class Base64EncodingTest extends BaseTestCase {


  private byte[] input;
  private String[][] expected;

  public void setUp() throws Exception {
    super.setUp();
    input = new byte[]{(byte)0xf7, (byte)0xf6, (byte)0xf5, (byte)0xf4, (byte)0x73, (byte)0x72, (byte)0x71, (byte)0x70};
    // expected Base64-encoding of the above input bytes, indexed by "url" and "withoutPadding" param values
    expected = new String[][]{
        { // url=false
            "9/b19HNycXA=", // withoutPadding=false
            "9/b19HNycXA",  // withoutPadding=true
        },
        { // url=true
            "9_b19HNycXA=", // withoutPadding=false
            "9_b19HNycXA",  // withoutPadding=true
        },
    };
  }

  @Override
  protected void tearDown() throws Exception {
    input = null;
    expected = null;
    super.tearDown();
  }

  public void testImplementations() {
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        boolean url = booleanValue(i), withoutPadding = booleanValue(j);
        Base64Encoding encoding = getInstance(url, withoutPadding);
        String encoded = encoding.encode(input);
        System.out.println(encoding + ": " + encoded);
        assertEquals(expected[i][j], encoded);
        assertArraysEqual(input, encoding.decode(encoded));
      }
    }
  }

  public void testGetInstance() {
    // test that getInstance always returns the same instance when given the same args
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        boolean url = booleanValue(i), withoutPadding = booleanValue(j);
        Base64Encoding a = getInstance(url, withoutPadding),
            b = getInstance(url, withoutPadding);
        assertSame(a, b);
        assertEquals(a, b);
      }
    }
    // same with the verbose getters
    assertSame(base64(), base64());
    assertSame(base64NoPadding(), base64NoPadding());
    assertSame(base64Url(), base64Url());
    assertSame(base64UrlNoPadding(), base64UrlNoPadding());
    // test equivalence between getInstance and verbose getters
    // TODO: for now, can't use assertSame b/c getInstance uses atomic array and the getters use holder classes
    assertEquals(base64(), getInstance());
    assertEquals(base64(), getInstance(false, false));
    assertEquals(base64NoPadding(), getInstance(false, true));
    assertEquals(base64Url(), getInstance(true, false));
    assertEquals(base64UrlNoPadding(), getInstance(true, true));
  }

  private static int intValue(boolean bool) {
    return bool ? 1 : 0;
  }

  private static boolean booleanValue(int i) {
    return i != 0;
  }


}