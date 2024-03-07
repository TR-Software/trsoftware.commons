/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.shared.validation;

import com.google.gwt.core.shared.GwtIncompatible;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Uses an elaborate regular expression to validate the <em>syntax</em> of email addresses
 * according to relevant RFCs (5321, 5322, 1738).
 * <p>
 * <b>Note:</b>
 * Although this class is well-tested and perfectly sufficient for detecting most malformatted addresses,
 * it may be too strict in its validation of the "local-part" (before the @ symbol) of the address,
 * which could lead to false positives in some very rare cases.
 * Either way, a regular expression cannot guarantee the validity of an email address because the actual
 * interpretation of the "local-part" (i.e. mailbox name) is the responsibility of the receiving mail host
 * and isn't specified by the SMTP protocol.
 * It should also be noted that most web resources on the topic of email address validation discourage
 * the use of regular expressions.
 * <p>
 * Ultimately, the only way to be sure an address is valid is to actually send mail to it and verify that
 * it was actually received.
 * <p>
 * That being said, however, this validator has been used in a production environment (on typeracer.com) for many years
 * with no reported issues, and any email address it rejects is very unlikely to belong to an ordinary person,
 * especially since most consumer email providers (like Gmail, Yahoo, Hotmail, etc.) impose even tougher restriction than
 * the RFCs on the chars allowed in usernames.
 *
 * @author Alex Epshteyn
 * @see <a href="https://en.wikipedia.org/wiki/Email_address#Syntax">Email address syntax (Wikipedia)</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc5321#section-4.1.2">RFC 5321</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1738#section-5">RFC 1738</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3696#section-3">RFC 3696</a>
 * 
 */
public class EmailAddressValidator extends RegexValidationRule {

  /*
  ================================================================================
  1). Patterns for the domain part
  ================================================================================
  The HOSTNAME regex is based on the URL RFC (https://datatracker.ietf.org/doc/html/rfc1738#section-5)
  The IP address regexes are based on the SMPT RFC (https://datatracker.ietf.org/doc/html/rfc5321#section-4.1.3)
  */

  //  domainlabel = alphadigit | alphadigit *[ alphadigit | "-" ] alphadigit
  private static final String DOMAIN_LABEL =
      "(?:[A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])";
  //  toplabel = alpha | alpha *[ alphadigit | "-" ] alphadigit
  private static final String TLD = "[A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9]";
  //  hostname = *[ domainlabel "." ] toplabel
  static final String HOSTNAME_DOMAIN =
      "(?:" + DOMAIN_LABEL + "\\.)*(?:" + TLD + ")";

  static final String IP4_DOMAIN = "\\[(?:\\d+\\.){3}\\d+\\]";  // e.g. jsmith@[192.168.2.1]
  static final String IP6_DOMAIN = "\\[IPv6:[0-9a-fA-F:]+\\]";  // e.g. jsmith@[IPv6:2001:db8::1]

  static final String DOMAIN = either(HOSTNAME_DOMAIN, IP4_DOMAIN, IP6_DOMAIN);

  /*
  ================================================================================
  2). Patterns for the local-part
  ================================================================================
  According to <a href="http://tools.ietf.org/html/rfc5321">RFC 5321</a> an email address is formatted
  as local-part@address. The local-part can contain almost all printable ASCII
  characters.  Any additional formatting restrictions can be imposed by the
  domain provider (e.g. Yahoo mail imposes this restriction "ID may consist of
  a-z, 0-9, underscores, and a single dot (.)") The SMTP protocol doesn't care
  about the local-part - it is left up to the receiving server to determine how
  to route the message to a mailbox. For example, the RFC doesn't impose
  case-insensitivity, while many domain servers do provide it.
 
  The informational RFC 3696 (http://tools.ietf.org/html/rfc3696) explains:
  Without quotes, local-parts may consist of any combination of alphabetic
  characters, digits, or any of the special characters
 
  ! # $ % & ' * + - / = ?  ^ _ ` . { | } ~
 
  A period (".") may also appear, but may not be used to start or end the local
  part, nor may two or more consecutive periods appear.  Stated differently,
  any ASCII graphic (printing) character other than the at-sign ("@"),
  backslash, double quote, comma, or square brackets may appear without
  quoting.  If any of that list of excluded characters are to appear, they must
  be quoted."
  */

  /** "Email local no period" - the characters allowed in the unquoted part of the email address minus the period character*/
  private static final String ELNP = "[\\w!#$%&'*+\\-/=?\\^_`{|}~]";
  /**
   * Matches an unquoted local-part of a valid email address, but doesn't check the length.
   * This regex ensures that it doesn't start/end with a dot, and that dots don't appear consecutively.
   */
  private static final String LOCAL_UNQUOTED =
      ELNP + "+|" + ELNP + "+(?:\\." + ELNP + "+)*";
  /**
   * Matches a quoted local-part of a valid email address while ensuring it's length is within the 64 allowable characters
   */
  private static final String LOCAL_QUOTED = "\"" +
      "(?:" +
      // 1-62 occurrences of a non-quote character or a \" pair
      "[^\"]" + "|" + "(?:(?<=\\\\)\")" +  // ((?<=\\)") matches an escaped quote (\") using positive lookbehind (see https://www.regular-expressions.info/lookaround.html#lookbehind)
      "){1,62}\"";

  /* TODO: maybe handle the following (rare) special cases:

     1. Backslash-escaped chars in unquoted local-part (e.g  Abc\@def@example.com)
        see: http://tools.ietf.org/html/rfc3696#page-5 and https://haacked.com/archive/2007/08/21/i-knew-how-to-validate-an-email-address-until-i.aspx/

     3. Backslash-escaped chars in quoted local-part
        Citing https://en.wikipedia.org/wiki/Email_address#Syntax:
        Space and special characters "(),:;<>@[\] are allowed with restrictions (they are only allowed inside a quoted string, as described in the paragraph below, and in that quoted string, any backslash or double-quote must be preceded once by a backslash);

        *Note: our EMAIL_LOCAL_QUOTED regex already enforces that quotes must be escaped
          (we're using the ((?<=\\)") positive lookbehind expression for this).
          We're not currently enforcing that backslashes must also be escaped, but that's okay, since it's better to
          be too permissive than too strict (since it's ultimately up to the SMTP receiving server to accept or reject)

     2. Comments (see https://en.wikipedia.org/wiki/Email_address#Syntax):
        Comments are allowed with parentheses at either end of the local-part;
          e.g., john.smith(comment)@example.com and (comment)john.smith@example.com are both equivalent to john.smith@example.com
        Comments are allowed in the domain as well as in the local-part;
          e.g., john.smith@(comment)example.com and john.smith@example.com(comment) are equivalent to john.smith@example.com.

     Probably not worth the effort, though since these rare case are very unlikely to occur in practice.
   */

  /**
   * The complete regex string that will match only syntactically valid email addresses.
   * Contains two capturing groups: (1) local-part and (2) domain-part
   *
   * Note: this regex doesn't check the email address's length against the
   * allowed limits.
   */
  public static final String EMAIL_REGEX =
      "(" + either(LOCAL_QUOTED, LOCAL_UNQUOTED) + ")@(" + DOMAIN + ")";

  /** must surround with ^$ to emulate the String.matches behavior in JS */
  private static final String EMAIL_REGEX_JS = "^" + EMAIL_REGEX + "$";

  /**
   * A regex that enforces the length limits for the local and domain parts
   * of the email address but is loose about syntax.
   */
  public static final String EMAIL_LENGTH_REGEX = ".{1,64}@[^@]{1,255}";

  /** must surround with ^$ to emulate the String.matches behavior in JS */
  private static final String EMAIL_LENGTH_REGEX_JS = "^" + EMAIL_LENGTH_REGEX  + "$";

  public static final int EMAIL_MAX_LENGTH = 317; // 64+1+255

  /**
   * Constructs a regex disjunction expression consisting of the given parts joined with the {@code |} operator.
   * Each expression will appear as a non-capturing group in the result.
   *
   * @return a disjunction expression consisting of the given parts
   */
  private static String either(String... expressions) {
    // TODO: extract method to a util class?
    return Arrays.stream(expressions)
        .map(s -> "(?:" + s + ")")  // enclose in a non-capturing group
        .collect(Collectors.joining("|"));

  }

  public EmailAddressValidator(String fieldName, boolean acceptNull, String errorMsg) {
    super(fieldName, errorMsg, acceptNull, EMAIL_REGEX_JS, EMAIL_LENGTH_REGEX_JS);
  }

  public EmailAddressValidator(String fieldName, boolean acceptNull) {
    this(fieldName, acceptNull, "email address is not valid");
  }

  /** This method prints all the validation regexes (useful for debugging or porting to other languages) */
  @GwtIncompatible
  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  public static void main(String[] args) throws IllegalAccessException {
    for (Field field : EmailAddressValidator.class.getDeclaredFields()) {
      int staticFinalMod = Modifier.STATIC | Modifier.FINAL;
      int mod = field.getModifiers();
      if ((mod & staticFinalMod) != 0 && field.getType() == String.class) {  // include only static final String fields
        System.out.printf("%24s: %s%n", field.getName(), field.get(EmailAddressValidator.class));
      }
    }
  }

}