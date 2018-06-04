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

package solutions.trsoftware.commons.shared.validation;

import com.google.gwt.core.shared.GWT;

/**
 * Apr 6, 2011
 *
 * @author Alex
 */
public abstract class RegexValidationRuleGwtTestCase extends ValidationRuleGwtTestCase {

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    if (!GWT.isScript()) {
      System.err.println("WARNING: This test must be run in \"web\" mode to use Javascript's (not Java's) regex facilities (use -Dgwt.args=\"-web\" on the command line");
    }
  }
}
