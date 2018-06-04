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

package solutions.trsoftware;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import solutions.trsoftware.commons.shared.annotations.ExcludeFromSuite;
import solutions.trsoftware.junit.TestSuiteBuilder;

/**
 * A test suite for all unit tests that inherit from {@link GWTTestCase}
 *
 * @author Alex
 * @since 4/18/2018
 */
public class GwtTests {

  // TODO: consider extracting the BaseGwtTestCase functionality to here

  /**
   * @return a {@link GWTTestSuite} containing all the {@link GWTTestCase} subclasses in this package
   */
  public static Test suite() throws Exception {
    /* TODO:
     Ensure that gwt.args contains a -testMethodTimeout option set to a low-enough value (e.g. 2 minutes),
     because the default (5 minutes) can really delay the whole suite if one test hangs for whatever reason
     */
    return suiteBuilder()
        .addContentRoot(GwtTests.class)
        .buildSuite(GwtTests.class.getName());
  }

  /**
   * @return a new {@link TestSuiteBuilder} instance, pre-configured with the settings required for a suite of
   * GWT test cases.
   */
  public static TestSuiteBuilder suiteBuilder() {
    return new TestSuiteBuilder()
        .includeOnlySubclassesOf(GWTTestCase.class)
        .excludeTestsAnnotatedWith(ExcludeFromSuite.class)
        .setUseGwtTestSuite(true)
        ;
  }

}
