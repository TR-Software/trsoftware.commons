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

package solutions.trsoftware.commons.server.bridge;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.bridge.NumberFormatterTestBridge;
import solutions.trsoftware.commons.server.bridge.text.NumberFormatterJavaImpl;

/**
 * This class uses NumberFormatterTestBridge as a delegate (which provides a way
 * to call the same test methods from both Java and GWT test contexts).
 *
 * @author Alex
 */
public class NumberFormatterJavaTest extends TestCase {
  NumberFormatterTestBridge delegate = new NumberFormatterTestBridge(){};

  @Override
  public void setUp() throws Exception {
    super.setUp();
    delegate.setUp();
  }

  public void testCorrectInstanceUsed() throws Exception {
    delegate.testCorrectInstanceUsed(NumberFormatterJavaImpl.class);
  }

  public void testFormattingDeterministically() {
    delegate.testFormattingDeterministically();
  }
}