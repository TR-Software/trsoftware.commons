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

package solutions.trsoftware.commons.client.validation;

/**
 * According to RFC 5321 (http://tools.ietf.org/html/rfc5321) email is formatted
 * as local-part@address. The local-part can contain almost all printable ASCII
 * characters.  Any additional formatting restrictions can be imposed by the
 * domain provider (e.g. Yahoo mail imposes this restriction "ID may consist of
 * a-z, 0-9, underscores, and a single dot (.)") The SMTP protocol doesn't care
 * about the local-part - it is left up to the receiving server to determine how
 * to route the message to a mailbox. For example, the RFC doesn't impose
 * case-insensitivity, while many domain servers do provide it.
 *
 * The informational RFC 3696 (http://tools.ietf.org/html/rfc3696) explains:
 * Without quotes, local-parts may consist of any combination of alphabetic
 * characters, digits, or any of the special characters
 *
 * ! # $ % & ' * + - / = ?  ^ _ ` . { | } ~
 *
 * period (".") may also appear, but may not be used to start or end the local
 * part, nor may two or more consecutive periods appear.  Stated differently,
 * any ASCII graphic (printing) character other than the at-sign ("@"),
 * backslash, double quote, comma, or square brackets may appear without
 * quoting.  If any of that list of excluded characters are to appear, they must
 * be quoted."
 *
 *
 * So, here are the only real restrictions on the local-part: 1) must be at most
 * 64 characters (octets) long. 2) must be either a valid quoted or unqoted
 * form: a) un-quoted form must i) match the regex [\w!\#$%&'*+\-/=?^_`.{|}~]{1,64}
 *                        Java code: Pattern.compile("[\\w!\\#$%&'*+\\-/=?^_`.{|}~]{1,64}")
 * ii) period (".") may may not be used to start or end the local part, nor may
 * two or more consecutive periods appear. b) quoted form must match the regex
 * "[^"]{1,62}"                                            Java code:
 * Pattern.compile("\"[^\"]{1,62}\"")
 *
 * @author Alex Epshteyn
 */
public class EmailAddressValidator extends RegexValidationRule {
  // the following regexes taken from the URL RFC (http://www.ietf.org/rfc/rfc1738.txt)
  //  domainlabel = alphadigit | alphadigit *[ alphadigit | "-" ] alphadigit
  private static final String DOMAIN_NAME_LABEL_RE =
      "(?:[A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])";
  //  toplabel = alpha | alpha *[ alphadigit | "-" ] alphadigit
  private static final String TLD_RE = "[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9]|[A-Za-z]";
  //  hostname = *[ domainlabel "." ] toplabel
  private static final String URL_HOSTNAME_REGEXP =
      "(?:" + DOMAIN_NAME_LABEL_RE + "\\.)+(?:" + TLD_RE + ")";

  /** "email local no period" - the characters allowed in the unqoted part of the email address minus the period character*/
  private static final String ELNP = "[\\w!\\#$%&'*+\\-/=?^_`{|}~]";
  /* Matches an unquoted local-part of a valid email address, but doesn't check the length */
  private static final String EMAIL_LOCAL_UNQUOTED =
      ELNP + "+|" + ELNP + "+(?:." + ELNP + "+)*";
  private static final String EMAIL_LOCAL_QUOTED = "\"[^\"]{1,62}\"";  // matches an unquoted local-part of a valid email address while ensuring it's length is within the 64 allowable characters

  // TODO: handle quoting individual characters using backslash http://tools.ietf.org/html/rfc3696#page-5

  /**
   * The complete regex string that will match only syntactically valid email addresses.
   * Contains two capturing groups: (1) local-part and (2) domain-part
   *
   * Note: this regex doesn't check the email address's length against the
   * allowed limits.
   */
  public static final String EMAIL_REGEX =
      "((?:" + EMAIL_LOCAL_QUOTED + ")|(?:" + EMAIL_LOCAL_UNQUOTED + ")"
          + ")@(" + URL_HOSTNAME_REGEXP + ")";

  /** must surround with ^$ to emulate the String.matches behavior in JS */
  private static final String EMAIL_REGEX_JS = "^" + EMAIL_REGEX + "$";

  /**
   * A regex that enforces the length limits for the local and domain parts
   * of the email address but is loose about syntax.
   */
  public static final String EMAIL_LENGTH_REGEX = ".{1,64}@.{1,255}";

  /** must surround with ^$ to emulate the String.matches behavior in JS */
  private static final String EMAIL_LENGTH_REGEX_JS = "^" + EMAIL_LENGTH_REGEX  + "$";

  public static final int EMAIL_MAX_LENGTH = 317; // 64+1+255

  public EmailAddressValidator(String fieldName, boolean acceptNull, String errorMsg) {
    super(fieldName, errorMsg, acceptNull, EMAIL_REGEX_JS, EMAIL_LENGTH_REGEX_JS);
  }

  public EmailAddressValidator(String fieldName, boolean acceptNull) {
    this(fieldName, acceptNull, "email address is not valid");
  }

  /** This method prints all the validation regexes (useful for porting to other languages) */
  public static void main(String[] args) {
    System.out.println("URL_HOSTNAME_REGEXP = " + URL_HOSTNAME_REGEXP);
    System.out.println("EMAIL_REGEXP = " + EMAIL_REGEX);
    System.out.println("EMAIL_LENGTH_REGEX = " + EMAIL_LENGTH_REGEX);
  }
}