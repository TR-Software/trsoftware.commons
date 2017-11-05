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

package solutions.trsoftware.commons.shared.util.template;

import junit.framework.TestCase;

/**
 * This class uses StringTemplateParserGwtTest as a delegate (which provides a
 * way to call the same test methods from both Java and GWT test contexts).
 *
 * The one restriction is that StringTemplateParserGwtTest must not use any set
 * up code (must not override the gwtSetUp() method.
 *
 * @author Alex
 */
public class SimpleTemplateParserJavaTest extends TestCase {
  SimpleTemplateParserGwtTest delegate = new SimpleTemplateParserGwtTest();

  public void testStringTemplate() throws Exception {
    delegate.testStringTemplate();
  }
}