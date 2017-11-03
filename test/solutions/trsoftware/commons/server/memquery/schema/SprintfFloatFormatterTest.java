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

package solutions.trsoftware.commons.server.memquery.schema;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.server.memquery.schema.SprintfFloatFormatter.getFormatSpec;


public class SprintfFloatFormatterTest extends TestCase {

  SprintfFloatFormatter formatter;

  public void setUp() throws Exception {
    super.setUp();
    formatter = new SprintfFloatFormatter(false, null, 2, true);
  }

  public void testFormat() throws Exception {
    assertEquals("3.46", formatter.format(3.456d));
    assertEquals("3.46", formatter.format(3.456f));
    assertEquals("3", formatter.format(3.0d));
    assertEquals("3", formatter.format(3.0f));
    assertEquals(Integer.toString(Integer.MAX_VALUE), formatter.format((double)Integer.MAX_VALUE));
    assertEquals(Long.toString(340282L), formatter.format(340282f));
    assertEquals(Integer.toString(Integer.MAX_VALUE) + ".12", formatter.format((double)Integer.MAX_VALUE + .12345));
    assertEquals(Long.toString(340282L) + ".13", formatter.format(340282f + .135f));
    assertEquals("477982", formatter.format(477981.99999999825d));

    System.err.println("Testing arguments that should throw an exception (because they're neither of type Float nor Double):");
    assertThrows(IllegalArgumentException.class, (Runnable)() -> formatter.format(5));

    assertThrows(IllegalArgumentException.class, (Runnable)() -> formatter.format(5L));
  }

  public void testGetFormatSpec() throws Exception {
    assertEquals("%f", getFormatSpec(false, null, null));
    assertEquals("%.2f", getFormatSpec(false, null, 2));
    assertEquals("%,.2f", getFormatSpec(true, null, 2));
    assertEquals("%,5.2f", getFormatSpec(true, 5, 2));
    assertEquals("%,5f", getFormatSpec(true, 5, null));

  }
}