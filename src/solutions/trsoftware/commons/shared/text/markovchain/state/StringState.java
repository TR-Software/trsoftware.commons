package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.shared.text.markovchain.TransitionTable;

/**
 * A State implementation that uses String instances as word tokens.
 *
 * Oct 26, 2009
 *
 * @author Alex
 */
abstract class StringState extends State<String> {

  @Override
  protected TransitionTable<String> createTransitionTable(String initalValue) {
    return new TransitionTable<String>(initalValue) {
      protected Class<String> getValueClass() {
        return String.class;
      }
    };
  }
  
}
