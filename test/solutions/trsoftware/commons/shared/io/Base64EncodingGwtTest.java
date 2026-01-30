package solutions.trsoftware.commons.shared.io;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;

/**
 * @author Alex
 * @since 12/20/2025
 */
public class Base64EncodingGwtTest extends CommonsGwtTestCase {
  // tests the emulated version of Base64EncodingImpl (src/solutions/trsoftware/commons/translatable/solutions/trsoftware/commons/shared/io/Base64EncodingImpl.java)
  // TODO: maybe extract dup code from Base64EncodingTest

  private byte[] input;
  private String[][] expected;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
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
  protected void gwtTearDown() throws Exception {
    input = null;
    expected = null;
    super.gwtTearDown();
  }

  public void testImplementations() {
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        boolean url = booleanValue(i), withoutPadding = booleanValue(j);
        Base64Encoding encoding = Base64Encoding.getInstance(url, withoutPadding);
        String encoded = encoding.encode(input);
        System.out.println(encoding + ": " + encoded);
        assertEquals(expected[i][j], encoded);
        assertArraysEqual(input, encoding.decode(encoded));
      }
    }
  }

  private static int intValue(boolean bool) {
    return bool ? 1 : 0;
  }

  private static boolean booleanValue(int i) {
    return i != 0;
  }


}