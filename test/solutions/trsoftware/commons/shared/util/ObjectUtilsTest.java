package solutions.trsoftware.commons.shared.util;

import solutions.trsoftware.commons.shared.BaseTestCase;

import java.util.Base64;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.shared.util.ObjectUtils.*;

/**
 * @author Alex
 * @since 7/29/2025
 */
public class ObjectUtilsTest extends BaseTestCase {

  public void testCopySerializable() throws Exception {
    // try copying a java.util.Random and verify that the 2 instances generate the same sequence (see https://stackoverflow.com/a/54156572)
    Random rnd = newRandom();
    Random rnd2 = copySerializable(rnd);
    assertEquals(rnd, rnd2);
  }

  public void testSerializeAndDeserialize() throws Exception {
    Random rnd = newRandom();
    byte[] bytes = serialize(rnd);
    String base64 = serializeBase64(rnd);
    assertEquals(Base64.getEncoder().encodeToString(bytes), base64);
    System.out.printf("Serialized %s (%d bytes as Base64):%n%s%n", rnd, bytes.length, base64);
    Random rnd2 = deserializeBase64(base64);
    assertEquals(rnd, rnd2);

  }

  private Random newRandom() {
    Random rnd = new Random();
    // advance the random sequence a bit from its initial seed to make sure its full internal state will be copied
    for (int i = 0; i < 10; i++) {
      rnd.nextInt(); rnd.nextGaussian();
    }
    return rnd;
  }

  /**
   * Verifies that the two instances generate the same sequence of random values
   */
  private void assertEquals(Random rnd, Random rnd2) {
    assertArraysEqual(
        IntStream.generate(rnd::nextInt).limit(20).toArray(),
        IntStream.generate(rnd2::nextInt).limit(20).toArray()
    );
    // Random uses different internal state variables for nextGaussian, so let's test that too
    assertArraysEqual(
        DoubleStream.generate(rnd::nextGaussian).limit(20).toArray(),
        DoubleStream.generate(rnd2::nextGaussian).limit(20).toArray()
    );
  }
}