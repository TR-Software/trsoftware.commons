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

import solutions.trsoftware.commons.shared.text.markovchain.dict.CodingDictionary;
import solutions.trsoftware.commons.shared.text.markovchain.dict.ShortArrayCodingDictionary;
import solutions.trsoftware.commons.shared.text.markovchain.dict.ShortHashArrayCodingDictionary;
import solutions.trsoftware.commons.shared.text.markovchain.state.State;
import solutions.trsoftware.commons.shared.util.text.TextTokenizer;

import java.io.Serializable;
import java.util.*;

/**
 * A Markov chain for text.  Can be trained to generate random text as
 * described at http://www.cs.umd.edu/class/spring2007/cmsc132/Projects/p6/p6.html
 *
 * Memory overhead was an important consideration in developing this package,
 * therefore all the supporting classes in here try to limit their
 * memory consumption to a minimum.
 */
public class MarkovChain implements Serializable {

  /**
   * The states of the Markov chain, each state is a combination of up to
   * n=order words and contains a weighted transition table giving the possible
   * next words.
   *
   * Uses a Map instead of Set to enable a canonicalized mapping (Set has not get method).
   * The values of this map are all going to be the same as their respective keys.
   */
  private Map<State, State> states = new LinkedHashMap<State, State>();

  // TODO: to save memory, replace keys with a State class, containing an integer

  private int order;

  /**
   * We need an instance of Tokenizer to be able to join strings as well as tokenize them.
   * (If we didn't need to join strings according to the tokenization
   * policy for various languages, the train method could just accept
   * an array of tokens instead of using a Tokenizer)
   */
  private TextTokenizer tokenizer;

  /** Only Short-based implementations are supported at this time. */
  private final CodingDictionary dict;

  private Random rnd;

  /** This version of the constructor can be used to produce deterministic behavior for testing */
  public MarkovChain(int order, TextTokenizer tokenizer) {
    this(order, tokenizer, new ShortHashArrayCodingDictionary(), new Random());
  }

  /**
   * This version of the constructor can be used to provide a custom dictionary coding strategy.
   * <p>
   * A simple benchmark showed that {@link ShortArrayCodingDictionary} is best when trying to reduce
   * memory overhead and {@link ShortHashArrayCodingDictionary} is best when trying to
   * reduce construction speed:
   * <pre>
   *  Training order 2 MarkovChains on aliceInWonderlandCorpus.txt with various CodingDictionary implementations:
   *    ShortHashArrayCodingDictionary used up 784.859 KB of memory and 17.81 ms avg. time
   *    ShortHashArrayCodingDictionaryUtf8 used up 811.594 KB of memory and 26.74 ms avg. time
   *    ShortArrayCodingDictionary used up 645.547 KB of memory and 221.80 ms avg. time
   *    ShortArrayCodingDictionaryUtf8 used up 719.922 KB of memory and 265.50 ms avg. time
   * </pre>
   *
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO(10/14/2019): can use {@link java.lang.instrument.Instrumentation#getObjectSize(Object)} to perform better
   *   memory benchmarking of the various {@link CodingDictionary} implementations.
   *   See <a href="https://www.baeldung.com/java-size-of-object">this article</a> for an example.
   *   <br>
   *   Additional resources:
   *   <br>* <a href="https://github.com/jbellis/jamm">jamm library</a> (javaagent able to estimate "deep" object size based on the above)
   *   <br>* <a href="https://stackoverflow.com/questions/9368764/calculate-size-of-object-in-java">related questions on StackOverflow</a>
   * </p>
   *
   * @param dict An instance of the desired dictionary implementation depending
   * on the desired memory usage vs.construction speed tradeoff.
   *
   */
  public MarkovChain(int order, TextTokenizer tokenizer, CodingDictionary dict, Random rnd) {
    this.order = order;
    this.tokenizer = tokenizer;
    this.dict = dict;
    this.rnd = rnd;
  }

  public void train(String str) {
    String[] tokens = tokenizer.tokenize(str);
    // create states from every possible N-gram of these tokens (N=order)
    for (int i = 0; i < tokens.length; i++) {
      ArrayList<String> stateTokens = new ArrayList<String>();
      for (int j = 0; j < order; j++) {
        if ((i - j - 1) >= 0) {
          stateTokens.add(0, tokens[i - j - 1]);
        }
      }
      addOrUpdateState(
          State.createState(dict, stateTokens),
          tokens[i]);
    }
  }

  private void addOrUpdateState(State state, String next) {
    if (states.containsKey(state))
      state = states.get(state);  // canonicalize the state
    else {
      states.put(state, state);
    }
    state.addTransition(next, dict);
  }

  public String generateRandomText(int maxLength) {
    ArrayList<String> generatedWords = new ArrayList<String>(maxLength * 5 * 2);  // the generated words
    String generatedText = "";
    while (generatedText.length() < maxLength) {
      // create a state consisting of the last (at most) N words
      List<String> stateTokens;
      if (generatedWords.size() == 0)
        stateTokens = Arrays.asList("");
      else {
        stateTokens = generatedWords.subList(
            Math.max(0, generatedWords.size() - order),
            Math.max(0, generatedWords.size() - 1) + 1);
      }
      
      State state = State.createState(dict, stateTokens);
      while (!states.containsKey(state)) {
        // narrow the scope of the search (# words in the state) until a match is found
        if (stateTokens.size() == 1)
          state = State.createState(dict);  // go back to the empty (starting) state
        else {
          stateTokens = stateTokens.subList(1, stateTokens.size());   // remove the first word from the state
          state = State.createState(dict, stateTokens);
        }
      }
      // canonicalize the state
      state = states.get(state);

      generatedWords.add(state.chooseTransition(rnd, dict));
      generatedText = tokenizer.join(generatedWords.toArray(new String[generatedWords.size()]));
    }
    return generatedText;
  }


  public int getOrder() {
    return order;
  }

  public int countStates() {
    return states.size();
  }

  /** Returns a (defensive) copy of the states, in the order they were created */
  public List<State> listStates() {
    return new ArrayList<State>(states.keySet());
  }

  /** Exposed with package visibility for unit testing */
  CodingDictionary<Short> getCodingDictionary() {
    return dict;
  }

  public TextTokenizer getTokenizer() {
    return tokenizer;
  }
}
