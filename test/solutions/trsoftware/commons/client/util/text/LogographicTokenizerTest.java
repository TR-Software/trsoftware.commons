package solutions.trsoftware.commons.client.util.text;

import solutions.trsoftware.commons.client.testutil.AssertUtils;
import junit.framework.TestCase;

/**
 * Oct 19, 2009
 *
 * @author Alex
 */
public class LogographicTokenizerTest extends TestCase {

  private LogographicTokenizer tokenizer;
  private String input;
  private String[] tokens;

  protected void setUp() throws Exception {
    super.setUp();
    tokenizer = new LogographicTokenizer();
    input = "A long time ago";
    tokens = new String[]{"A", " ", "l", "o", "n", "g", " ", "t", "i", "m", "e", " ", "a", "g", "o"};
  }

  public void testTokenize() throws Exception {
    AssertUtils.assertArraysEqual(tokens, tokenizer.tokenize(input));
  }

  public void testJoin() throws Exception {
    assertEquals(input, tokenizer.join(tokens));
  }
}