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

package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.i18n.client.DateTimeFormat;
import solutions.trsoftware.commons.client.util.TimeUnit;

import java.util.Date;

/**
 * A combo box which enumerates all the months of the year in the local language,
 * and uses internal values "1"-"12", returned by getText().
 *
 * Date: Jul 3, 2008 Time: 6:10:15 PM
 *
 * @author Alex
 */
public class MonthSelector extends DirtyComboBox {
  /** Names of the 12 months based on the i18n locale of the user */
  private static String[] monthNames;
  static {
    // init the month names
    DateTimeFormat monthFormatter = DateTimeFormat.getFormat("MMMM");
    monthNames = new String[12];
    for (int i = 0; i < monthNames.length; i++)
      monthNames[i] = monthFormatter.format(new Date((long)(TimeUnit.MONTHS.toMillis(i) + TimeUnit.DAYS.toMillis(5))));  // add 5 days to be sure we're somewhere in the middle of a month
  }
  
  public MonthSelector() {
    this("", "");
  }

  public MonthSelector(String promptRowText) {
    this(promptRowText, "");
  }

  /**
   * @param promptRowText The label to use for the first row, which represents an unspecified value
   * @param initialValue A string "1".."12" to select that particular month, or any other value, like "0" or null to select the empty row.
   */
  public MonthSelector(String promptRowText, String initialValue) {
    super(createItems(promptRowText), initialValue);
  }

  /** Creates a 13 x 2 array of name-value pairs, with the first row being the empty value (prompt) and then 1 row for each month */
  private static String[][] createItems(String promptRowText) {
    String[][] items = new String[13][];
    items[0] = new String[]{promptRowText, ""};
    for (int i = 1; i < items.length; i++) {
      items[i] = new String[]{monthNames[i-1], ""+i};
    }
    return items;
  }

}
