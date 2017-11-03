package solutions.trsoftware.commons.server.util.codec;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Jan 30, 2009
 *
 * @author Alex
 */
public class BigIntRadixCodecTest extends TestCase {


  public void testAllCodecs() throws Exception {
    for (BigIntRadixCodec codec : BigIntRadixCodec.values()) {
      System.out.println("Testing " + codec);
      testCodec(codec);
    }

  }

  private void testCodec(BigIntRadixCodec codec) throws Exception {
    // checking the entire integer range for mutual convertibility to-from base64
    // will take too long, so we just check a few of the corner cases and a lot
    // of values selected at random
    Random rnd = new Random();
    List<BigInteger> toBeTested = new ArrayList<BigInteger>();
    // check all integers between -1000 and 1000
    for (int i = -1000; i <= 1000; i++)
      toBeTested.add(BigInteger.valueOf(i));
    toBeTested.add(BigInteger.valueOf(Integer.MIN_VALUE));
    toBeTested.add(BigInteger.valueOf(Integer.MAX_VALUE));
    // some random ints
    for (int i = 0; i < 100; i++)
      toBeTested.add(BigInteger.valueOf(rnd.nextInt()));
    // now on to longs
    toBeTested.add(BigInteger.valueOf(Long.MIN_VALUE));
    toBeTested.add(BigInteger.valueOf(Long.MAX_VALUE));
    // some random longs
    for (int i = 0; i < 100; i++)
      toBeTested.add(BigInteger.valueOf(rnd.nextLong()));
    // now some random huge ints with up to 1000 bits
    for (int i = 0; i < 100; i++)
      toBeTested.add(new BigInteger(rnd.nextInt(1000), rnd));

    // now test all these - making sure decode(encode(n)) == n
    int i = 0;
    for (BigInteger bigInt : toBeTested) {
      String encoded = codec.encode(bigInt);
      BigInteger decoded = codec.decode(encoded);
      if (++i % 100 == 0)
        System.out.println(bigInt + " -> " + encoded + " -> " + decoded);  // print only one out of 100 results, to save time and console space
      assertEquals(bigInt, decoded);
    }
  }
}