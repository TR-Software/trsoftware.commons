package solutions.trsoftware.commons.client.util.text;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertArraysEqual;
import junit.framework.TestCase;

/**
 * Oct 19, 2009
 *
 * @author Alex
 */
public class WhitespaceTokenizerTest extends TestCase {

  private WhitespaceTokenizer tokenizer;
  private String[] inputs;
  private String[] tokens;

  protected void setUp() throws Exception {
    super.setUp();
    tokenizer = new WhitespaceTokenizer();
    // create a bunch of input strings that should all tokenize to the same thing
    // try a few different combinations of delimiters (defined by java.util.StringTokenizer as " \t\n\r\f")
    inputs = new String[]{
        "A long time ago, in a galaxy far, far away.",
        "\nA long time ago, in a galaxy far, far away.",
        "A long time      ago, in a galaxy far, far away.\n",
        "A long time ago, \rin a galaxy far, far away.\n",
        "A long time ago, \f\fin a galaxy far, far away.",
        "A long  \t \n \r \f  time ago, in a galaxy far, far away.",
        "A long  \t\n\r\f  time ago, in a galaxy far, far away.",
        "A long \t\n\r\ftime ago, in a galaxy \nfar, far away.",
        "A long time \n ago, in a   galaxy far, far away.",
        "A\nlong\ttime\rago,  \n in a galaxy\ffar,\f far\naway.",
    };
    tokens = new String[]{"A", "long", "time", "ago,", "in", "a", "galaxy", "far,", "far", "away."};
  }

  public void testTokenize() throws Exception {
    for (String input : inputs) {
      System.out.println("Tokenizing \"" + input + "\"");
      assertArraysEqual(tokens, tokenizer.tokenize(input));
    }
  }

  public void testJoin() throws Exception {
    assertEquals(inputs[0], tokenizer.join(tokens));  // there is only 1 way to join the tokens
  }
}