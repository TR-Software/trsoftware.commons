package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;

import java.util.Locale;

/**
 * @author Alex
 * @since 11/20/2017
 */
public class LocaleUtilsTest extends TestCase {

  public void testCountries() throws Exception {
    String[] isoCountries = Locale.getISOCountries();
    for (String code : isoCountries) {
      Locale loc = new Locale("", code);
      System.out.printf("%s: %s%n", code, loc.getDisplayName());

    }
    fail("TODO"); // TODO
  }

}