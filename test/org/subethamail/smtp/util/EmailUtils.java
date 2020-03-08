package org.subethamail.smtp.util;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/*
 NOTE(alex): we're overwriting this class from subethasmtp-3.1.7 to work around a bug in gappengine's dev_appserver,
 which incorrectly escapes sender email addresses that contain a nickname part (e.g. "TypeRacer <support@typeracer.com>"),
 which causes it to send the SMTP command "FROM:<TypeRacer <support@typeracer.com>>", which is invalid, because
 nested angle brackets are not allowed here, and the subetha smtp server correctly rejects this command.

 @see the following:
   - https://stackoverflow.com/a/27451673/
   - https://github.com/voodoodyne/subethasmtp/issues/97
 We're patching the extractEmailAddress method here to allow nested angle brackets.

 TODO: my patch is now in https://github.com/davidmoten/subethasmtp, so should replace dependency on voodoodyne/subethasmtp with davidmoten/subethasmtp, which is better maintained
*/

/**
 * @author Jeff Schnitzer
 */
public class EmailUtils {
  /**
   * @return true if the string is a valid email address
   */
  public static boolean isValidEmailAddress(String address) {
    // MAIL FROM: <>
    if (address.length() == 0)
      return true;

    boolean result = false;
    try {
      InternetAddress[] ia = InternetAddress.parse(address, true);
      if (ia.length == 0)
        result = false;
      else
        result = true;
    }
    catch (AddressException ae) {
      result = false;
    }
    return result;
  }

  /**
   * Extracts the email address within a <> after a specified offset.
   */
  public static String extractEmailAddress(String args, int offset) {
    String address = args.substring(offset).trim();
    if (address.indexOf('<') == 0) {
      // address = address.substring(1, address.indexOf('>'));
      /*
        **NOTE(alex): this patch allows nested angle brackets in address (e.g. "<Foo Bar <foobar@example.com>>"):**
        (see https://stackoverflow.com/a/27451673/)
      */
      address = address.substring(1, address.lastIndexOf('>'));
      // spaces within the <> are also possible, Postfix apparently
      // trims these away:
      return address.trim();
    }

    // find space (e.g. SIZE argument)
    int nextarg = address.indexOf(" ");
    if (nextarg > -1) {
      address = address.substring(0, nextarg).trim();
    }
    return address;
  }

  /**
   * Normalize the domain-part to lowercase.  If email address is missing
   * an '@' the email is returned as-is.
   */
  public static String normalizeEmail(String email) {
    int atIndex = email.indexOf('@');
    if (atIndex < 0)
      return email;
    else
      return email.substring(0, atIndex) + email.substring(atIndex).toLowerCase();
  }
}
