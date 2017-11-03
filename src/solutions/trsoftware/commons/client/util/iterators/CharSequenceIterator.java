package solutions.trsoftware.commons.client.util.iterators;

/**
* @author Alex, 4/27/2016
*/
public class CharSequenceIterator extends IndexedIterator<Character> {

  private final CharSequence str;

  public CharSequenceIterator(CharSequence str) {
    super(str.length());
    this.str = str;
  }

  public CharSequenceIterator(CharSequence str, int start) {
    super(start, str.length());
    this.str = str;
    get(start); // trigger IndexOutOfBoundsException if the starting index isn't valid
  }

  @Override
  protected Character get(int idx) {
    return str.charAt(idx);
  }

}
