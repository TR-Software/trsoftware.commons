package solutions.trsoftware.commons.server.io;

import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.server.io.csv.CSVReader;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;

/**
 * Sep 24, 2009
 *
 * @author Alex
 */
public class MultiReaderTest extends TestCase {

  /**
   * Makes sure that BufferedReader works properly with MultiReader as the
   * input
   */
  public void testUsingBufferedReader() throws Exception {
    MultiReader mr = new MultiReader(Arrays.asList(
        new StringReader("foo"),
        new StringReader("bar"),
        new StringReader("b\naz")
    ));

    BufferedReader br = new BufferedReader(mr);
    assertEquals("foobarb", br.readLine());
    assertEquals("az", br.readLine());
    assertNull(br.readLine());
  }

  /** Makes sure that CSVReader works properly with MultiReader as the input */
  public void testUsingCSVReader() throws Exception {
    MultiReader mr = new MultiReader(Arrays.asList(
        new StringReader("1,25,"),
        new StringReader("\"foo bar\","),
        new StringReader(",b\na,z")
    ));

    CSVReader cr = new CSVReader(mr);
    {
      Object[] expectedLine = {"1", "25", "foo bar", "", "b"};
      Object[] actualLine = cr.readNext();
      assertTrue(
          AssertUtils.comparisonFailedMessage("", Arrays.toString(expectedLine), Arrays.toString(actualLine)),
          Arrays.equals(expectedLine, actualLine));
    }
    {
      Object[] expectedLine = {"a", "z"};
      Object[] actualLine = cr.readNext();
      assertTrue(
          AssertUtils.comparisonFailedMessage("", Arrays.toString(expectedLine), Arrays.toString(actualLine)),
          Arrays.equals(expectedLine, actualLine));
    }
    assertNull(cr.readNext());
  }

}