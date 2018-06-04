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

/**
 *
 * Date: Jul 7, 2008
 * Time: 2:03:21 PM
 * @author Alex
 */
public class RegexValidationRuleGwtTest extends RegexValidationRuleGwtTestCase {

  public void testRegexValidation() throws Exception {
    // 1) test with a single regex
    {
      RegexValidationRule rule = new RegexValidationRule("test", false, "[a-z]{2,4}");
      assertValidity(rule, "ASDF", false);
      assertValidity(rule, "a", false);
      assertValidity(rule, "as", true);
      assertValidity(rule, "as", true);
      assertValidity(rule, "asd", true);
      assertValidity(rule, "asdf", true);
      assertValidity(rule, "asdfe", false);
    }
    // 2) test with multiple regexes (all must pass)
    {
      RegexValidationRule rule = new RegexValidationRule("test", false, "[a-z]{2,4}", "[asdf]*");
      assertValidity(rule, "ASDF", false);
      assertValidity(rule, "a", false);  // too short
      assertValidity(rule, "as", true);
      assertValidity(rule, "xx", false); // doesn't match [asdf]*
      assertValidity(rule, "as", true);
      assertValidity(rule, "asd", true);
      assertValidity(rule, "asdf", true);
      assertValidity(rule, "asde", false);  // doesn't match [asdf]*
      assertValidity(rule, "asdfe", false);  // too long
    }
  }


}