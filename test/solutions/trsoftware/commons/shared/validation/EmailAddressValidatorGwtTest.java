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
    assertNotValid("a");
    assertNotValid("asdf");
    assertValid("as@df");
    assertValid("a@s");
    assertValid("a@s.df");
    // test the different TLD variations
    Map<String, Boolean> tlds = MapUtils.linkedHashMap(
        "s.d-f", true,
        "s.df-", false,
        "s.df-8", true,
        "s.df-q8", true,
        "s.df-79--873--q", true,
        "s.df-79--8*3--q", false);
    // test multiple TLDs
    for (String tld1 : tlds.keySet()) {
      assertValidity(validator, "a@s." + tld1, tlds.get(tld1));
      for (String tld2 : tlds.keySet()) {
        assertValidity(validator, "a@s." + (tld1 + "." + tld2), tlds.get(tld1) && tlds.get(tld2));
      }
    }

    assertValid("asdf@asdf.com");
    assertValid("asdf@asdf.com");
    assertNotValid("  alexander.epshteyn@gmail.com ");
    // dots in username are ok
    assertValid("alexander.epshteyn@gmail.com");
    assertValid("a.lexanderepshteyn@gmail.com");
    assertValid("al.exanderepshteyn@gmail.com");
    assertValid("ale.xanderepshteyn@gmail.com");
    assertValid("alex.anderepshteyn@gmail.com");
    assertValid("alexanderepshtey.n@gmail.com");
    assertValid("alexanderepshte.yn@gmail.com");
    assertValid("alexanderepsht.eyn@gmail.com");
    assertValid("alexander.epsh.teyn@gmail.foo.bar.us");
    assertNotValid("alexander.epsh..teyn@gmail.foo.bar.us");
    assertNotValid(".alexander.epshteyn@gmail.foo.bar.us");
    assertNotValid("alexander.epshteyn.@gmail.foo.bar.us");
    assertNotValid("alexander.epsh.@gmail.foo.bar.us");
    assertValid("alexander.epsh.as@gmail.foo.bar.us");
    assertValid("alexander.ep.s.h.as@gmail.foo.bar.us");
    assertValid("alexander.epsh.a@gmail.foo.bar.us");
    assertValid("alexander.e@gmail.foo.bar.us");
    assertValid("a.l.e.xander.e@gmail.foo.bar.us");
    assertValid("a@pidar.ru");
    assertValid("ab@pidar.ru");
    assertValid("a123a@pi--dar.ru");
    assertValid("a123a@pi-dar.ru");
    assertValid("a123a@pidar.ru");
    assertValid("a123a@pid190013ar.net");
    assertValid("a123@pid190013ar.net");
    assertValid("a_123@pid190013ar.net");
    assertValid("a_123_@pid190013ar.net");
    assertValid("123@pid190013ar.net");
    assertValid("123asdf@pid190013ar.net");
    assertValid("123.asdf@pid190013ar.net");
    assertValid("123_asdf@pid190013ar.net");
    assertValid("asdf@asdf.com");
    assertNotValid("@asdf.com");
    assertValid("asdf@asdf");
    assertNotValid("asd@@sf.foo");
    assertValid("as_d@sfnet.foo");
    assertValid("as_d@sf-net.foo");
    assertNotValid("as_d@sf_net.foo");
    assertNotValid("as_dtest@sf_net..foo");
    assertNotValid("as..d@sf_net.foo");
    assertNotValid("as.d@ sf_net.foo");
    assertValid("joe@q.com");
    assertValid("the-stone@gmx.at");
    assertValid("-the-stone@gmx.at");
    assertNotValid("-the-st*(&$one@gmx.at");  // '(' not allowed in unquoted local part
    assertNotValid("-the-st*@&$one@gmx.at");  // '@' not allowed in unquoted local part
    assertValid("the-stone-@gmx.at");
    assertValid("the--stone@gmx.at");
    assertValid("the-st-one@gmx.at");
    assertValid("the-st-o-ne@gmx.at");
    assertValid("typeracer@matthewmoore.org.uk");
    assertValid("ljosa-typeracer@ljosa.com");

    // pretty much anything goes when the local part is quoted
    assertValid("\"asdf\"@example.com");
    assertValid("\"as df\"@example.com");
    assertValid("\"as @@(*!&@# df\"@example.com");

    // try some examples from the RFC (http://tools.ietf.org/html/rfc3696#page-5)

    assertValid("\"Abc@def\"@example.com");
    assertValid("\"Fred Bloggs\"@example.com");
    assertValid("user+mailbox@example.com");
    assertValid("customer/department=shipping@example.com");
    assertValid("$A12345@example.com");
    assertValid("!def!xyz%abc@example.com");
    assertValid("_somename@example.com");
  }

  public void testExamplesFromWikipedia() throws Exception {
    validator = new EmailAddressValidator("email", false);

    // examples from Wikipedia (https://en.wikipedia.org/wiki/Email_address#Examples on 1/19/2024)

    // Valid email addresses:
    assertValid("simple@example.com");
    assertValid("very.common@example.com");
    assertValid("x@example.com");  // one-letter local-part
    assertValid("long.email-address-with-hyphens@and.subdomains.example.com");
    assertValid("user.name+tag+sorting@example.com");  // may be routed to user.name@example.com inbox depending on mail server
    assertValid("name/surname@example.com");  // slashes are a printable character, and allowed
    assertValid("admin@example");  // local domain name with no TLD, although ICANN highly discourages dotless email addresses
    assertValid("example@s.example");  // see https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains
    assertValid("\" \"@example.org");  // space between the quotes
    assertValid("\"john..doe\"@example.org");  // quoted double dot
    assertValid("mailhost!username@example.org");  // bangified host route used for uucp mailers
    assertValid("user%example.com@example.org");  // % escaped mail route to user@example.com via example.org
    assertValid("user-@example.org");  // local-part ending with non-alphanumeric character from the list of allowed printable characters
    assertValid("postmaster@[123.123.123.123]");  // IP addresses are allowed instead of domains when in square brackets, but strongly discouraged
    assertValid("postmaster@[IPv6:2001:0db8:85a3:0000:0000:8a2e:0370:7334]");  // IPv6 uses a different syntax
    assertValid("_test@[IPv6:2001:0db8:85a3:0000:0000:8a2e:0370:7334]");  // begin with underscore different syntax
    assertValid("\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\ \\\"very\\\".unusual\"@strange.example.com");  // include non-letters character AND multiple at sign, the first one being double quoted

    /* TODO(1/19/2024): this test fails; do we want to update the regexes in EmailAddressValidator to be more lenient?
     * Note: using regexes might not be the best way of validating email addresses; see:
     *   - https://stackoverflow.com/questions/2049502/what-characters-are-allowed-in-an-email-address
     *   - https://www.regular-expressions.info/email.html
     */

    // TODO: test the invalid address examples given in the Wikipedia article too
    assertNotValid("abc.example.com");  // no @ character
    assertNotValid("a@b@c@example.com");  // only one @ is allowed outside quotation marks
    assertNotValid("a\"b(c)d,e:f;g<h>i[j\\k]l@example.com");  // none of the special characters in this local-part are allowed outside quotation marks
    assertNotValid("just\"not\"right@example.com");  // quoted strings must be dot separated or be the only element making up the local-part
    assertNotValid("this is\"not\\allowed@example.com");  // spaces, quotes, and backslashes may only exist when within quoted strings and preceded by a backslash
    assertNotValid("this\\ still\\\"not\\\\allowed@example.com");  // even if escaped (preceded by a backslash), spaces, quotes, and backslashes must still be contained by quotes
    assertNotValid("1234567890123456789012345678901234567890123456789012345678901234+x@example.com");  // local-part is longer than 64 characters
    assertNotValid("i.like.underscores@but_they_are_not_allowed_in_this_part");  // underscore is not allowed in domain part
  }

  protected void assertValid(String testString) throws Exception {
    super.assertValidity(validator, testString, true);
  }

  protected void assertNotValid(String testString) throws Exception {
    super.assertValidity(validator, testString, false);
  }


}