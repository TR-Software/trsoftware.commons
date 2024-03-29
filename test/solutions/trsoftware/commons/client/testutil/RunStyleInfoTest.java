/*
 * Copyright 2022 TR Software Inc.
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
 */

package solutions.trsoftware.commons.client.testutil;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.rebind.RunStyleInfoGenerator;

/**
 * Tests the {@link RunStyleInfo} implementation generated with {@link RunStyleInfoGenerator}.
 *
 * @author Alex
 * @since 12/15/2021
 */
public class RunStyleInfoTest extends CommonsGwtTestCase {

  public void testRunStyleInfo() throws Exception {
    RunStyleInfo runStyleInfo = RunStyleInfo.INSTANCE;
    log("RunStyleInfo.INSTANCE = " + runStyleInfo.toDebugString());
    RunStyleValue runStyleValue = runStyleInfo.getRunStyleValue();
    assertNotNull(runStyleValue);
    log("Parsed RunStyleValue: " + runStyleValue.toDebugString());


    // TODO: check some assertions?  maybe implement a servlet that obtains the value of gwt.args?
  }
}
