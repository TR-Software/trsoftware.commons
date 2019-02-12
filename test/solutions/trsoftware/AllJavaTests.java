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
import junit.framework.Test;
import solutions.trsoftware.commons.shared.annotations.ExcludeFromSuite;
import solutions.trsoftware.junit.TestSuiteBuilder;
import solutions.trsoftware.junit.TestTimeBoxDecorator;

/**
 * A test suite for all unit tests that don't extend {@link GWTTestCase}
 *
 * @author Alex
 * @since 4/18/2018
 */
public class AllJavaTests {

  public static Test suite() throws Exception {
    return suiteBuilder()
        .addContentRoot(AllJavaTests.class)
        .buildSuite();
  }

  /**
   * @return a new {@link TestSuiteBuilder} instance, pre-configured with the settings required for a suite of
   * all non-GWT test cases, without any {@link TestTimeBoxDecorator}s applied.
   */
  public static TestSuiteBuilder suiteBuilder() {
    return new TestSuiteBuilder()
        .excludeSubclassesOf(GWTTestCase.class)
        .excludeTestsAnnotatedWith(ExcludeFromSuite.class)
        ;
  }

}
