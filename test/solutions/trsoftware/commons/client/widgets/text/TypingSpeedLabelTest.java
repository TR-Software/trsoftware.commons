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

package solutions.trsoftware.commons.client.widgets.text;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.client.util.StringUtils;
import solutions.trsoftware.commons.client.util.callables.Function1_t;
import solutions.trsoftware.commons.shared.text.Language;
import solutions.trsoftware.commons.shared.text.TypingSpeed;

import java.util.List;

/**
 * @author Alex, 10/31/2017
 */
public class TypingSpeedLabelTest extends CommonsGwtTestCase {
  // TODO: cont here: unable to use BaseGwtTestCase due to some kind of bug - find the problem

  private List<TypingSpeedLabel> labels;  // will store all the created widgets so they can be removed on tear down

  private static void withLabel(final TypingSpeed.Unit unit, final int maxFractionalDigits, Function1_t<TypingSpeedLabel, Exception> test) throws Exception {
    TypingSpeedLabel label = new TypingSpeedLabel(unit, maxFractionalDigits) {
      @Override
      protected void onUnload() {
        super.onUnload();
        System.out.println(StringUtils.methodCallToString("TypingSpeedLabelTest.onUnload", unit, maxFractionalDigits));
      }
    };
    RootPanel.get().add(label);
    assertLabelEquals(label, "");
    try {
      test.call(label);
    }
    finally {
      label.removeFromParent();
    }
  }


  private static void assertLabelEquals(TypingSpeedLabel label, String expected) {
    Element element = label.getElement();
    AssertUtils.assertElementTextEquals(element, expected);
  }

  public void testSetValue() throws Exception {
    withLabel(TypingSpeed.Unit.WPM, 2, new Function1_t<TypingSpeedLabel, Exception>() {
      @Override
      public void call(TypingSpeedLabel label) throws Exception {
        TypingSpeed speed = new TypingSpeed(25d, TypingSpeed.Unit.WPM, Language.ENGLISH);
        label.setValue(speed);
        assertEquals(speed, label.getValue());
        assertLabelEquals(label, "25 WPM");
        assertEquals("25 WPM", label.getTitle());
        speed = new TypingSpeed(25.123, TypingSpeed.Unit.WPM, Language.ENGLISH);
        label.setValue(speed);
        assertEquals(speed, label.getValue());
        assertLabelEquals(label, "25.12 WPM");
        assertEquals("25.123 WPM", label.getTitle());
      }
    });
    withLabel(TypingSpeed.Unit.WPM, 1, new Function1_t<TypingSpeedLabel, Exception>() {
      @Override
      public void call(TypingSpeedLabel label) throws Exception {
        TypingSpeed speed = new TypingSpeed(25.153, TypingSpeed.Unit.WPM, Language.ENGLISH);
        label.setValue(speed);
        assertEquals(speed, label.getValue());
        assertLabelEquals(label, "25.2 WPM");
        assertEquals("25.153 WPM", label.getTitle());
      }
    });
  }

  public void testSetUnit() throws Exception {
    withLabel(TypingSpeed.Unit.WPM, 2, new Function1_t<TypingSpeedLabel, Exception>() {
      @Override
      public void call(TypingSpeedLabel label) throws Exception {
        double wpm = 25.123;
        double cpm = 125.615;  // as calculated by Unit.WPM.to(Unit.CPM, wpm, Language.ENGLISH)
        TypingSpeed speed = new TypingSpeed(wpm, TypingSpeed.Unit.WPM, Language.ENGLISH);
        assertEquals(TypingSpeed.Unit.WPM, label.getUnit());
        label.setValue(speed);
        assertEquals(TypingSpeed.Unit.WPM, label.getUnit());
        assertEquals(speed, label.getValue());
        assertLabelEquals(label, "25.12 WPM");
        assertEquals("25.123 WPM", label.getTitle());
        label.setUnit(TypingSpeed.Unit.CPM);
        assertEquals(TypingSpeed.Unit.CPM, label.getUnit());
        // the text should have been re-rendered using the new unit
        assertLabelEquals(label, "125.62 CPM");
        assertEquals("125.615 CPM", label.getTitle());
      }
    });
  }

}