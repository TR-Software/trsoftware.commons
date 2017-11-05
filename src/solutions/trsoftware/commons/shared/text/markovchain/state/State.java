package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.shared.text.markovchain.TransitionTable;
import solutions.trsoftware.commons.shared.text.markovchain.dict.CodingDictionary;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A state in a Markov chain representing an N-gram of words, where N
 * is the order of the chain.
 *
 * The primary design goal here is to minimize memory consumption, so for example,
 * the methods take the dictionary as a parameter rather than having it as a field.
 * 
 * Oct 20, 2009
 *
 * V is the data type used to represent a single word token internally
 * (e.g. String, bye[], Short, etc.)
 *
 * @author Alex
 */
public abstract class State<V> {

  private TransitionTable<V> transitionTable;

  public void addTransition(String nextWord, CodingDictionary<V> dict) {
    V encodedWord = dict.encode(nextWord);
    if (transitionTable == null)
      transitionTable = createTransitionTable(encodedWord);
    else
      transitionTable.add(encodedWord);
  }

  protected abstract TransitionTable<V> createTransitionTable(V initalValue);

  /** Randomly selects the next word from the possible transitions */
  public String chooseTransition(Random rnd, CodingDictionary<V> dict) {
    return dict.decode(transitionTable.chooseTransition(rnd));
  }

  public int transitionCount() {
    if (transitionTable == null)
      return 0;
    else
      return transitionTable.size();
  }

  public Map<V, Number> getTransitions() {
    if (transitionTable == null)
      return Collections.emptyMap();
    else
      return transitionTable.asMap();
  }

  public static State createState(CodingDictionary codingDictionary, List<String> words) {
    return createState(codingDictionary, words.toArray(new String[words.size()]));
  }

  /**
   * A factory method that should be used in preference to any of the constructors.
   * Ensures that the same set of words will result in the same State subclass,
   * which will ensure proper equals and hashCode operation.
   * @param dict Only Short-based CodingDictionary implementations are supported at this time.
   */
  public static State createState(CodingDictionary dict, String... words) {
    if (dict.encode("foo") instanceof Short)
      switch (words.length) {
        case 0:
          return new UnigramShortState("", dict);  // the empty string is the canonical starting state
        case 1:
          return new UnigramShortState(words[0], dict);
        case 2:
          return new BigramShortState(words[0], words[1], dict);
        default:
          return new NgramShortState(dict, words);
    }
    else if (dict.encode("foo") instanceof String) {
      return new NgramStringState(dict, words);
    }
    throw new IllegalArgumentException("Only CodingDictionary<String> and CodingDictionary<Short> are suppored at this time");
  }

  /**
   * Returns the individual words that make up this state.
   * @param index a value 0...N (example: if bigram, use either 0 or 1)
   * @param dict the coding dictionary.
   * @return the i-th word in this state.
   * @throws IndexOutOfBoundsException if the given index is not within its bounds
   */
  public abstract String getWord(int index, CodingDictionary<V> dict) throws IndexOutOfBoundsException;

  /** Returns the number of words in this state */
  public abstract int wordCount();

  /** Returns an array of the words in this state */
  public String[] getWords(CodingDictionary<V> dict) {
    String[] result = new String[wordCount()];
    for (int i = 0; i < result.length; i++) {
      result[i] = getWord(i, dict);
    }
    return result;
  }


  @Override
  public boolean equals(Object obj) {
    throw new AbstractMethodError("Subclasses must override equals.");
  }

  public int hashCode() {
    throw new AbstractMethodError("Subclasses must override hashCode.");
  }

}
