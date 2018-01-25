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

package solutions.trsoftware.commons.shared.util.text;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * @author Alex, 10/31/2017
 */
public class SharedNumberFormatGwtTest extends CommonsGwtTestCase {

  public void testFormat() throws Exception {
    SharedNumberFormat format = new SharedNumberFormat("#.##");
    assertEquals("0", format.format(0));
    assertEquals("0.12", format.format(.1234));
    assertEquals("0.55", format.format(.55));
    assertEquals("0.56", format.format(.555));
  }

  public void testParse() throws Exception {
    SharedNumberFormat format = new SharedNumberFormat("#.##");
    assertEquals(0d, format.parse("0").doubleValue());
    assertEquals(.1234, format.parse(".1234").doubleValue());
    assertEquals(.55, format.parse(".55").doubleValue());
    assertEquals(.555, format.parse(".555").doubleValue());
  }

  public void testPercentages() throws Exception {
    SharedNumberFormat format = new SharedNumberFormat("#.##%");
    assertEquals("0%", format.format(0));
    assertEquals("12.34%", format.format(.1234));
    assertEquals("12.35%", format.format(.123456789));
    assertEquals("55%", format.format(.55));
    assertEquals("55.5%", format.format(.555));
    assertEquals("55.56%", format.format(.55556));
    assertEquals("100000%", format.format(1000));
    assertEquals(0L, format.parse("0%"));
    assertEquals(.1234, format.parse("12.34%"));
    assertEquals(.1235, format.parse("12.35%"));
    assertEquals(.55, format.parse("55%"));
    assertEquals(.555, format.parse("55.5%"));
    assertEquals(.5556, format.parse("55.56%"));
    assertEquals(1000L, format.parse("100000%"));
  }

}