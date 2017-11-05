package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.shared.text.markovchain.ShortTransitionTable;
import solutions.trsoftware.commons.shared.text.markovchain.TransitionTable;

/**
 * A state in a Markov chain that uses Shorts to represent words.
 *
 * Oct 20, 2009
 *
 * @author Alex
 */
abstract class ShortState extends State<Short> {

  protected TransitionTable<Short> createTransitionTable(Short initalValue) {
    return new ShortTransitionTable(initalValue);
  }

}
