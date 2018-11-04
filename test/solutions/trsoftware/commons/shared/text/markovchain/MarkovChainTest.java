/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.shared.text.markovchain;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.testutil.TestData;
import solutions.trsoftware.commons.shared.text.markovchain.dict.CodingDictionary;
import solutions.trsoftware.commons.shared.text.markovchain.dict.ShortHashArrayCodingDictionary;
import solutions.trsoftware.commons.shared.text.markovchain.state.State;
import solutions.trsoftware.commons.shared.util.stats.HashCounter;
import solutions.trsoftware.commons.shared.util.text.TextTokenizer;
import solutions.trsoftware.commons.shared.util.text.WhitespaceTokenizer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Date: May 12, 2008 Time: 7:46:45 PM
 *
 * @author Alex
 */
public class MarkovChainTest extends TestCase {

  private MarkovChain mc;

  protected void setUp() throws Exception {
    super.setUp();
    // create a simple bigram chain that's easy to analyze
    // NOTE: don't modify this or the regression test will break
    mc = new MarkovChain(2, new WhitespaceTokenizer(), new ShortHashArrayCodingDictionary(), new Random(0));
    mc.train("This is foo.");
    mc.train("This is bar.");
    mc.train("This is baz.");
    mc.train("Poshel ti na huy.");
    mc.train("Poshel na huy.");
    mc.train("Poshel ti na huy.");
    mc.train("Poshel ti v zhopu.");
    mc.train("Poshel ti v zad.");
    mc.train("Poshel ti v pizdu.");
  }

  public void testMarkovChain() {
    // check the states and make sure that the transition counts are correct
    List<State> states = mc.listStates();  // lists the states in the order they were added
    TextTokenizer tok = mc.getTokenizer();
    CodingDictionary<Short> dict = mc.getCodingDictionary();

    printStateTransitions(mc);

    {
      // the first state is the starting state
      State state = states.get(0);
      assertEquals("", stateToString(mc, state));
      // it should have the following transitions: "This" times 3, and "Poshel" times 6
      Map<Short, Number> transitions = state.getTransitions();
      assertEquals(2, transitions.size());
      assertEquals(3, transitions.get(dict.encode("This")).intValue());
      assertEquals(6, transitions.get(dict.encode("Poshel")).intValue());
    }

    {
      // the second state should be "This"
      State state = states.get(1);
      assertEquals("This", stateToString(mc, state));
      // it should have only one transition: "is" times 3
      Map<Short, Number> transitions = state.getTransitions();
      assertEquals(1, transitions.size());
      assertEquals(3, transitions.get(dict.encode("is")).intValue());
    }

    {
      // the third state should be "This is"
      State state = states.get(2);
      assertEquals("This is", stateToString(mc, state));
      // it should have 3 transitions: "foo.", "bar.", "baz.", each occurring once
      Map<Short, Number> transitions = state.getTransitions();
      assertEquals(3, transitions.size());
      assertEquals(1, transitions.get(dict.encode("foo.")).intValue());
      assertEquals(1, transitions.get(dict.encode("bar.")).intValue());
      assertEquals(1, transitions.get(dict.encode("baz.")).intValue());
    }

    {
      // the next state should be "Poshel" (the sentences starting with "This is" are all done at this point)
      State state = states.get(3);
      assertEquals("Poshel", stateToString(mc, state));
      // it should have 2 transition: "ti" times 5 and "na" times 1
      Map<Short, Number> transitions = state.getTransitions();
      assertEquals(2, transitions.size());
      assertEquals(5, transitions.get(dict.encode("ti")).intValue());
      assertEquals(1, transitions.get(dict.encode("na")).intValue());
    }

    // and so on...
    {
      State state = states.get(4);
      assertEquals("Poshel ti", stateToString(mc, state));
      Map<Short, Number> transitions = state.getTransitions();
      assertEquals(2, transitions.size());
      assertEquals(2, transitions.get(dict.encode("na")).intValue());
      assertEquals(3, transitions.get(dict.encode("v")).intValue());
    }

    {
      State state = states.get(5);
      assertEquals("ti na", stateToString(mc, state));
      Map<Short, Number> transitions = state.getTransitions();
      assertEquals(1, transitions.size());
      assertEquals(2, transitions.get(dict.encode("huy.")).intValue());
    }

    {
      State state = states.get(6);
      assertEquals("Poshel na", stateToString(mc, state));
      Map<Short, Number> transitions = state.getTransitions();
      assertEquals(1, transitions.size());
      assertEquals(1, transitions.get(dict.encode("huy.")).intValue());
    }

    {
      State state = states.get(7);
      assertEquals("ti v", stateToString(mc, state));
      Map<Short, Number> transitions = state.getTransitions();
      assertEquals(3, transitions.size());
      assertEquals(1, transitions.get(dict.encode("zhopu.")).intValue());
      assertEquals(1, transitions.get(dict.encode("zad.")).intValue());
      assertEquals(1, transitions.get(dict.encode("pizdu.")).intValue());
    }

    // and that's it! should be no other states
    assertEquals(8, states.size());

    // now generate a bunch of texts with this chain for manual inspection
    generate10Texts(mc);
  }

  public void testRegression() {
    // this is a regression test to make sure refactorings of MarkovChain don't change its functionality
    assertEquals(
        "Poshel ti v pizdu. Poshel na huy. Poshel ti v pizdu. Poshel na huy. Poshel ti v zhopu. Poshel ti v zhopu. This is baz. This is bar. This is baz. This is baz. Poshel ti na huy. Poshel ti na huy. Poshel ti v zhopu. This is bar. Poshel ti v pizdu. Poshel ti na huy. Poshel ti v pizdu. Poshel ti v zhopu. Poshel ti v zad. This is bar. Poshel na huy. Poshel na huy. Poshel ti v zhopu. This is foo. This is bar. Poshel ti v zad. Poshel ti na huy. Poshel ti na huy. This is baz. Poshel ti v zhopu. Poshel ti",
        mc.generateRandomText(500));
  }

  private void generate10Texts(MarkovChain mc) {
    int len = 500;
    for (int i = 0; i < 10; i++) {
      String result = mc.generateRandomText(len);
      System.out.printf("Generated text of length (expected=%d, actual=%d)%s%n", len, result.length(), result);
    }
  }


  public void testOrder1ChainOnAliceWonderlandCorpus() throws IOException {
    testMarkovChainOnCorpus(TestData.getAliceInWonderlandText(), 1);
  }

  @Slow
  public void testOrder2ChainOnAliceWonderlandCorpus() throws IOException {
    testMarkovChainOnCorpus(TestData.getAliceInWonderlandText(), 2);
  }

  @Slow
  public void testOrder3ChainOnAliceWonderlandCorpus() throws IOException {
    testMarkovChainOnCorpus(TestData.getAliceInWonderlandText(), 3);
  }

  private void testMarkovChainOnCorpus(String corpusFilename, int order) throws IOException {
    MarkovChain mc = new MarkovChain(order, new WhitespaceTokenizer());
    System.out.println("Training Markov Chain of order " + order);
    trainMarkovChainOnCorpus(mc, corpusFilename);
    printStats(mc);
    printStateTransitions(mc);
    generate10Texts(mc);
  }

  /**
   * Trains the given markov chain line-by-line on the given text from the given named resource.
   */
  public static void trainMarkovChainOnCorpus(MarkovChain markovChain, String corpusFileResource) {
    // TODO: cont here: why do this line-by-line instead of the whole text?
    List<String> textLines = TestData.getAliceInWonderlandTextLines();
    for (String textLine : textLines) {
      markovChain.train(textLine);
    }
  }


  public static String stateToString(MarkovChain mc, State state) {
    TextTokenizer tok = mc.getTokenizer();
    return tok.join(state.getWords(mc.getCodingDictionary()));
  }

  public static String transitionsToString(MarkovChain mc, State state) {
    TextTokenizer tok = mc.getTokenizer();
    CodingDictionary<Short> dict = mc.getCodingDictionary();
    StringBuilder str = new StringBuilder();
    str.append("{");
    Map<Short, Number> transitions = state.getTransitions();
    for (Map.Entry<Short, Number> entry : transitions.entrySet()) {
      str.append(dict.decode(entry.getKey())).append("=").append(entry.getValue()).append(", ");
    }
    int lastSeparatorIndex = str.lastIndexOf(", ");
    if (lastSeparatorIndex >= 0)
      str.delete(lastSeparatorIndex, lastSeparatorIndex + 2);
    str.append("}");
    return str.toString();
  }

  /** Print some stats about this markov chain for debugging */
  public static void printStats(MarkovChain mc) {
    System.out.printf("Order %d Markov chain with %d states, of these there are%n", mc.getOrder(), mc.countStates());
    // gather statistics for the number of transitions per state
    {
      HashCounter<Integer> transitionCounter = new HashCounter<Integer>();
      for (State state : mc.listStates()) {
        transitionCounter.increment(state.transitionCount());
      }
      for (Map.Entry<Integer, Integer> count : transitionCounter.entriesSortedByValueDescending()) {
        System.out.printf("  %d states with %d transitions%n", count.getValue(), count.getKey());
      }
    }
  }

  /** Prints the transition table for debugging */
  public static void printStateTransitions(MarkovChain mc) {
    System.out.printf("Transition table for order %d Markov chain with %d states%n", mc.getOrder(), mc.countStates());
    for (State state : mc.listStates()) {
      System.out.printf("  %20s: %s%n", stateToString(mc, state), transitionsToString(mc, state));
    }
  }
}