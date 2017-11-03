/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.io;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.server.io.csv.CSVReader;

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