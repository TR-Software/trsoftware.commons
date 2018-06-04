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

package solutions.trsoftware.commons.client;

import com.google.gwt.core.shared.GWT;

/**
 * Dec 4, 2008
 *
 * @author Alex
 */
public abstract class CommonsGwtWebTestCase extends CommonsGwtTestCase {

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    assertTrue(
        "This test needs to be run in web mode to properly exercise" +
            " emulated classes like solutions.trsoftware.commons.bridge.BridgeTypeFactory" +
            " and the JRE emulation library.  Give the -Dgwt.args=\"-web\" VM parameter" +
            " to the test runner to run the test in web mode.",
        GWT.isScript());
  }
}
