package solutions.trsoftware.commons.server.io.csv;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertArraysEqual;
import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import java.io.IOException;
import java.io.StringReader;

/**
 * Sep 28, 2009
 *
 * @author Alex
 */
public class CSVReaderTest extends TestCase {


  public void testCSVReader() throws Exception {
    // try a very simple line
    checkReading(new String[]{"foo", "1", "bar"}, "foo,1,bar\n");
    // the last two elements need to be quoted and the last needs the quote chars escaped
    checkReading(new String[]{"foo", "1", "2,3,4", "Joe said, \"bar this fool\""}, "foo,1,\"2,3,4\",\"Joe said, \"\"bar this fool\"\"\"\n");
    // now check quoted text that contains the escape char that isn't a recognized escape sequence
    checkReading(new String[]{"foo", "1", "2,3,4", "qwer\\asdf"}, "foo,1,\"2,3,4\",\"qwer\\asdf\"\n");
  }

  private void checkReading(String[] expectedResult, String input) throws IOException {
    CSVReader csvReader = new CSVReader(new StringReader(input));
    String[] result = csvReader.readNext();
    AssertUtils.assertArraysEqual(expectedResult, result);
  }


  public void testUnicode() throws Exception {
    CSVReader csvReader = new CSVReader(new StringReader("movie,\"Lock, Stock, and Two Smoking Barrels (Lock, Stock, dhe Dy Pirja fu�i)\",Guy Ritchie,albanian,B000GGSMC6,1,5,149,\"Problemi �sht�, Willie, �sht� se Charles dhe vet� nuk jan� t� shpejt� t� Cats n� t� mir� t� koh�s. Pra, vet�m b�j si them dhe mbaj kafaz i mbyllur!\",,,,1010022"));
    String[] line = csvReader.readNext();
    assertEquals("movie", line[0]);
    assertEquals("Lock, Stock, and Two Smoking Barrels (Lock, Stock, dhe Dy Pirja fu�i)", line[1]);
    assertEquals("Guy Ritchie", line[2]);
  }

  public void testAcceptsQuotedLineBreaks() throws Exception {
    // line breaks inside a cell should be okay as long as the cell is quoted
    String input = "A,\"B\nC\nD\",E\n\"1\n23\n4\",5,6";
    CSVReader csvReader = new CSVReader(new StringReader(input));
    assertArraysEqual(new String[]{"A", "B\nC\nD", "E"}, csvReader.readNext());
    assertArraysEqual(new String[]{"1\n23\n4", "5", "6"}, csvReader.readNext());
  }
  
}