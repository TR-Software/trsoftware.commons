package solutions.trsoftware.commons.client.util.text;

/**
 * A way to convert text to tokens and back to text again, which can
 * be implemented depending on source language (e.g. English vs. Chinese).
 *
 * All implementing classes should be stateless (the same instance can be
 * reused and shared between threads)
 *
 * @author Alex
 *
 * TODO: @see java.text.BreakIterator which might be a better choice than this class
 */
public abstract class TextTokenizer {

  public abstract String getDelimiter();

  /** Breaks up the given text into tokens */
  public abstract String[] tokenize(String text);

  /**
   * Reconstructs text from the given tokens by inserting the language-specific
   * word delimiter (e.g. space for English)
   */
  public abstract String join(String[] tokens);
}
