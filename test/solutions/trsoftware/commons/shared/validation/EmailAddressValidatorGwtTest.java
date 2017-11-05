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

package solutions.trsoftware.commons.shared.validation;

import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.Map;

/**
 * Date: Jul 7, 2008
 * Time: 2:03:21 PM
 * @author Alex
 */
public class EmailAddressValidatorGwtTest extends RegexValidationRuleGwtTestCase {
  private EmailAddressValidator validator;

  public void testEmailValidation() throws Exception {
    validator = new EmailAddressValidator("email", false);
    assertValidity("a", false);
    assertValidity("asdf", false);
    assertValidity("as@df", false);
    assertValidity("a@s", false);
    assertValidity("a@s.df", true);  // this is the shortest email address that should pass validation
    // test the diffiernt TLD variations
    Map<String, Boolean> tlds = MapUtils.linkedHashMap(
        "s.d-f", true,
        "s.df-", false,
        "s.df-8", true,
        "s.df-q8", true,
        "s.df-79--873--q", true,
        "s.df-79--8*3--q", false);
    // test multiple TLDs
    for (String tld1 : tlds.keySet()) {
      assertValidity("a@s." + tld1, tlds.get(tld1));
      for (String tld2 : tlds.keySet()) {
        assertValidity("a@s." + (tld1 + "." + tld2), tlds.get(tld1) && tlds.get(tld2));
      }
    }

    assertValidity("asdf@asdf.com", true);
    assertValidity("asdf@asdf.com", true);
    assertValidity("  alexander.epshteyn@gmail.com ", false);  // spaces around input not allowed
    // dots in username are ok
    assertValidity("alexander.epshteyn@gmail.com", true);
    assertValidity("a.lexanderepshteyn@gmail.com", true);
    assertValidity("al.exanderepshteyn@gmail.com", true);
    assertValidity("ale.xanderepshteyn@gmail.com", true);
    assertValidity("alex.anderepshteyn@gmail.com", true);
    assertValidity("alexanderepshtey.n@gmail.com", true);
    assertValidity("alexanderepshte.yn@gmail.com", true);
    assertValidity("alexanderepsht.eyn@gmail.com", true);
    assertValidity("alexander.epsh.teyn@gmail.foo.bar.us", true);
    assertValidity("alexander.epsh..teyn@gmail.foo.bar.us", false);  // two dots back to back aren't allowed
    assertValidity(".alexander.epshteyn@gmail.foo.bar.us", false);  // local part can't start with dot
    assertValidity("alexander.epshteyn.@gmail.foo.bar.us", false);  // local part can't end with dot
    assertValidity("alexander.epsh.@gmail.foo.bar.us", false);
    assertValidity("alexander.epsh.as@gmail.foo.bar.us", true);
    assertValidity("alexander.ep.s.h.as@gmail.foo.bar.us", true);
    assertValidity("alexander.epsh.a@gmail.foo.bar.us", true);
    assertValidity("alexander.e@gmail.foo.bar.us", true);
    assertValidity("a.l.e.xander.e@gmail.foo.bar.us", true);
    assertValidity("a@pidar.ru", true);
    assertValidity("ab@pidar.ru", true);
    assertValidity("a123a@pi--dar.ru", true);
    assertValidity("a123a@pi-dar.ru", true);
    assertValidity("a123a@pidar.ru", true);
    assertValidity("a123a@pid190013ar.net", true);
    assertValidity("a123@pid190013ar.net", true);
    assertValidity("a_123@pid190013ar.net", true);
    assertValidity("a_123_@pid190013ar.net", true);
    assertValidity("123@pid190013ar.net", true); // email addresses that start with numbers should pass validation
    assertValidity("123asdf@pid190013ar.net", true); // email addresses that start with numbers should pass validation
    assertValidity("123.asdf@pid190013ar.net", true); // email addresses that start with numbers should pass validation
    assertValidity("123_asdf@pid190013ar.net", true); // email addresses that start with numbers should pass validation
    assertValidity("asdf@asdf.com", true);
    assertValidity("@asdf.com", false);
    assertValidity("asdf@asdf", false);
    assertValidity("asd@@sf.foo", false);
    assertValidity("as_d@sfnet.foo", true);
    assertValidity("as_d@sf-net.foo", true);
    assertValidity("as_d@sf_net.foo", false);
    assertValidity("as_dtest@sf_net..foo", false);
    assertValidity("as..d@sf_net.foo", false);
    assertValidity("as.d@ sf_net.foo", false);
    assertValidity("joe@q.com", true);
    assertValidity("the-stone@gmx.at", true);
    assertValidity("-the-stone@gmx.at", true);
    assertValidity("-the-st*(&$one@gmx.at", true);
    assertValidity("-the-st*(@&$one@gmx.at", false);  // can't have 2 @ symbols
    assertValidity("the-stone-@gmx.at", true);
    assertValidity("the--stone@gmx.at", true);
    assertValidity("the-st-one@gmx.at", true);
    assertValidity("the-st-o-ne@gmx.at", true);
    assertValidity("typeracer@matthewmoore.org.uk", true);
    assertValidity("ljosa-typeracer@ljosa.com", true);

    // pretty much anything goes when the local part is quoted
    assertValidity("\"asdf\"@example.com", true);
    assertValidity("\"as df\"@example.com", true);
    assertValidity("\"as @@(*!&@# df\"@example.com", true);

    // try some examples from the RFC (http://tools.ietf.org/html/rfc3696#page-5)

    assertValidity("\"Abc@def\"@example.com", true);
    assertValidity("\"Fred Bloggs\"@example.com", true);
    assertValidity("user+mailbox@example.com", true);
    assertValidity("customer/department=shipping@example.com", true);
    assertValidity("$A12345@example.com", true);
    assertValidity("!def!xyz%abc@example.com", true);
    assertValidity("_somename@example.com", true);
  }

  private void assertValidity(String testString, boolean valid) throws Exception {
    super.assertValidity(validator, testString, valid);
  }

  

}