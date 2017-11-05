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

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

/**
 * Sep 14, 2009
 *
 * @author Alex
 */
public class ReadOnlyFlagTest extends TestCase {

  public void testReadOnlyFlag() throws Exception {
    ReadOnlyFlag flag = new ReadOnlyFlag();
    assertFalse(flag.isSet());
    assertTrue(flag.set());
    assertTrue(flag.isSet());
    assertFalse(flag.set());  // cannot be set again
    assertTrue(flag.isSet());  // already set
  }
}