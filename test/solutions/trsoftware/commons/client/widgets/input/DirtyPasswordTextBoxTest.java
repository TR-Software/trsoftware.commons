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

package solutions.trsoftware.commons.client.widgets.input;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * Jan 18, 2010
 *
 * @author Alex
 */
public class DirtyPasswordTextBoxTest extends CommonsGwtTestCase {

  public void testIsDirty() throws Exception {
    checkIsDirty("foo");
    checkIsDirty("");
  }

  private void checkIsDirty(String initialValue) {
    DirtyPasswordTextBox box = new DirtyPasswordTextBox(initialValue);
    assertFalse(box.isDirty());
    String newValue = "bar";
    assertFalse(newValue.equals(initialValue));
    box.setText(newValue);
    assertTrue(box.isDirty());
    box.setText(initialValue);
    assertFalse(box.isDirty());
  }
}