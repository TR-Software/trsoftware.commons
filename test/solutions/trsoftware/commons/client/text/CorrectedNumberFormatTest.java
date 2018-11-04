/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.text;

import com.google.gwt.i18n.client.NumberFormat;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;

import static solutions.trsoftware.commons.shared.util.MathUtils.EPSILON;

/**
 * @author Alex
 * @since 4/23/2018
 */
public class CorrectedNumberFormatTest extends CommonsGwtTestCase {

  /**
   * Checks that {@link CorrectedNumberFormat#parse(String)} corrects the
   * <a href="https://github.com/gwtproject/gwt/issues/9611">NumberFormat bug when parsing percentages</a>
   */
  public void testPercentages() throws Exception {
    CorrectedNumberFormat fmt = new CorrectedNumberFormat(NumberFormat.getFormat("#.##%"));
    assertEquals("1.23%", fmt.format(.0123));
    assertEquals("1.23%", fmt.format(.0123123));
    assertEquals(.0123, fmt.parse("1.23%"), EPSILON);
    assertEquals("45%", fmt.format(.45));
    assertEquals(fmt.parse("45%"), .45, EPSILON);
  }

}