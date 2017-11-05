package solutions.trsoftware.commons.shared.text.markovchain;

/**
 * Oct 20, 2009
*
* @author Alex
*/
public class ShortTransitionTable extends TransitionTable<Short> {
  
  public ShortTransitionTable(Short initalValue) {
    super(initalValue);
  }

  protected Class<Short> getValueClass() {
    return Short.class;
  }
}
