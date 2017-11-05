/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.util.iterators;

import junit.framework.TestCase;
import solutions.trsoftware.commons.Slow;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GeneratorTest extends TestCase {

  public void testIterator() throws Exception {
    List<Integer> expectedNaturalNumbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    List<Character> expectedChars = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');
    List<Integer> expectedEvenNumbers = Arrays.asList(null, 2, null, 4, null, 6, null, 8, null, 10);

    try {
      for (int nElements : Arrays.asList(10, 5, 2, 1)) {
        for (int maxBufferSize : Arrays.asList(1, 2, 5, 10, 20, 1000)) {
          checkGenerator(new NaturalNumbersGenerator(maxBufferSize, nElements), expectedNaturalNumbers.subList(0, nElements));
          checkGenerator(new AlphabetGenerator(maxBufferSize, nElements), expectedChars.subList(0, nElements));
          checkGenerator(new EvenNumbersGenerator(maxBufferSize, nElements), expectedEvenNumbers.subList(0, nElements));
          System.out.println();
        }
        System.out.println("--------------------");
      }
    }
    finally {
      Thread.sleep(1);  // allow stdout to catch up
    }
  }

  /**
   * Since our implementation of {@link Generator} uses the {@link Generator#hasNext()} to produce the next value,
   * we want to make sure it works without an explicit call to {@link Generator#hasNext()} before each iteration.
   */
  public void testIterationWithoutCallingHasNext() throws Exception {
    NaturalNumbersGenerator gen = new NaturalNumbersGenerator(10, 10);
    for (int i = 1; i <= 10; i++) {
      assertEquals(i, (int)gen.next());
    }
    assertFalse(gen.hasNext());
  }

  protected <T> void checkGenerator(Generator<T> generator, List<T> expectedSequence) throws InterruptedException {
    Duration duration = new Duration(String.format("%s returned %s", generator, expectedSequence), "in");
    assertGeneratedSequenceEquals(expectedSequence, generator);
    System.out.println(duration);
    Thread.sleep(1);  // allow stdout to catch up
  }

  /** Asserts that the given list of items is equivalent to the elements produced by the given generator */
  protected <T> void assertGeneratedSequenceEquals(List<T> expectedSequence, Generator<T> generator) {
    assertEquals(generator, generator.iterator());
    assertEquals(expectedSequence, CollectionUtils.asList((Iterable<T>)generator));
    assertFalse(generator.hasNext());
    // now try resetting the generator and make sure its new iterator still generates the same sequence
    generator.reset();
    assertEquals(expectedSequence, CollectionUtils.asList((Iterable<T>)generator));
    assertFalse(generator.hasNext());
  }

  @Slow
  public void testPerformance() throws Exception {
    // see how long it takes to generate a million numbers with various buffer sizes
    for (int maxBufferSize : Arrays.asList(10, 20, 50, 100, 1000, 10000, 100000, 1000000)) {
      for (int nElements : Arrays.asList(10, 100, 1000, 10000, 100000, 1000000)) {
        NaturalNumbersGenerator generator = new NaturalNumbersGenerator(maxBufferSize, nElements);
        Duration duration = new Duration(generator.toString());
        Iterator<Integer> iterator = generator.iterator();
        int i;
        for (i = 1; iterator.hasNext(); i++) {
          int nextValue = iterator.next();
          assert nextValue == i;  // using the assert keyword instead of the assertTrue method because we are measuring performance of the generator only
        }
        System.out.println(duration);
      }
      System.out.println();
    }
  }

  // define some generators to be tested:

  /**
   * Base class for all generators in this test.
   */
  public static abstract class TestGenerator<T> extends Generator<T> {
    protected int nElements;

    public TestGenerator(int maxBufferSize, int nElements) {
      super(maxBufferSize);
      this.nElements = nElements;
    }

    @Override
    public String toString() {
      return String.format("%s(nElements=%,d, bufferSize=%,d)", getClass().getSimpleName(), nElements, maxBufferSize);
    }
  }

  private static class NaturalNumbersGenerator extends TestGenerator<Integer> {
    private NaturalNumbersGenerator(int maxBufferSize, int nElements) {
      super(maxBufferSize, nElements);
    }

    @Override
    protected void generate() {
      for (int i = 1; i <= nElements; i++) {
        yield(i);
      }
    }
  }

  /** Tests null values in the stream by substituting {@code null} for odd numbers in the sequence of natural numbers */
  private static class EvenNumbersGenerator extends TestGenerator<Integer> {
    private EvenNumbersGenerator(int maxBufferSize, int nElements) {
      super(maxBufferSize, nElements);
    }

    @Override
    protected void generate() {
      for (int i = 1; i <= nElements; i++) {
        if (i % 2 == 0)
          yield(i);
        else
          yield(null);
      }
    }
  }

  private static class AlphabetGenerator extends TestGenerator<Character> {
    private AlphabetGenerator(int maxBufferSize, int nElements) {
        super(maxBufferSize, nElements);
    }

    @Override
    protected void generate() {
      for (int i = 0; i < nElements; i++) {
        yield((char)('a' + (i % 26)));
      }
    }
  }

}