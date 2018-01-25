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

import junit.framework.TestCase;

/**
 * @author Alex, 10/31/2017
 */
public class SharedNumberFormatJavaTest extends TestCase {

  SharedNumberFormatGwtTest delegate = new SharedNumberFormatGwtTest();

  public void testFormat() throws Exception {
    delegate.testFormat();
  }

  public void testParse() throws Exception {
    delegate.testParse();
  }

  public void testPercentages() throws Exception {
    delegate.testPercentages();
  }
}