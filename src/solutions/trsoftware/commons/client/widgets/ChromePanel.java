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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/** A blue-gray gradient panel */
public class ChromePanel extends StyledPanel {

  // TODO(2/17/2016): can use GWT's new DecoratorPanel class to implement this class

  /**
   * @param width The width to use for the panel (in CSS units), or null to omit
   * a width attribute.
   */
  public ChromePanel(String width, Widget wrappedWidget) {
    this(width, HTMLPanel.createUniqueId());
    add(wrappedWidget);
  }

  /**
   *
   * @param width The width to use for the panel (in CSS units), or null to omit
   * a width attribute.
   * @param contentElementId Every instance of this class should have a unique
   * id for its HTML element that holds content (otherwise conflicts arise)
   */
  private ChromePanel(String width, String contentElementId) {
    super(
        "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
            "margin-left:auto; margin-right:auto;\""
            + ((width != null) ? (" width=\"" + width + "\"") : "") + ">\n" +
            "  <tr height=\"5\">\n" +
            "    <td class=\"chrome_tl\" width=\"5\" height=\"5\"><div></div></td>\n" +
            "    <td class=\"chrome_t\" width=\"14\" height=\"5\"><div></div></td>\n" +
            "    <td class=\"chrome_t\" height=\"5\"><div></div></td>\n" +
            "    <td class=\"chrome_t\" width=\"14\" height=\"5\"><div></div></td>\n" +
            "    <td class=\"chrome_tr\" width=\"5\" height=\"5\"><div></div></td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "    <td class=\"chrome_l\" width=\"5\"><div></div></td>\n" +
            "    <td class=\"chrome_m\" width=\"14\"><div></div></td>\n" +
            "    <td id=\"" + contentElementId + "\" class=\"chrome_m\">" +
            /* The actual content will go into this cell */
            "    </td>\n" +
            "    <td class=\"chrome_m\" width=\"14\"><div></div></td>\n" +
            "    <td class=\"chrome_r\" width=\"5\"><div></div></td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "    <td class=\"chrome_bl\" width=\"5\" height=\"5\"><div></div></td>\n" +
            "    <td class=\"chrome_b\" width=\"14\" height=\"5\"><div></div></td>\n" +
            "    <td class=\"chrome_b\" height=\"5\"><div></div></td>\n" +
            "    <td class=\"chrome_b\" width=\"14\" height=\"5\"><div></div></td>\n" +
            "    <td class=\"chrome_br\" width=\"5\" height=\"5\"><div></div></td>\n" +
            "  </tr>\n" +
            "</table>",
        contentElementId);
  }

}
