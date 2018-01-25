package solutions.trsoftware.commons.shared.data;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.images.flags.CountryFlagsBundle;

import java.util.List;
import java.util.Locale;

/**
 * @author Alex
 * @since 11/20/2017
 */
public class CountryCodesJavaTest extends TestCase {

  public void testGetCountryName() throws Exception {
    // check that the class contains all the ISO codes available in Java Locales
    boolean complete = true;
    String[] isoCountries = Locale.getISOCountries();
    for (String code : isoCountries) {
      Locale loc = new Locale("", code);
      String jreName = loc.getDisplayName();
      String ourName = CountryCodes.getCountryName(code.toLowerCase());
      System.out.printf("%s: %s [our name = %s] (ru: %s; es: %s)%n", code, jreName, ourName,
          loc.getDisplayName(Locale.forLanguageTag("ru")), loc.getDisplayName(Locale.forLanguageTag("es")));
      if (ourName == null) {
        System.out.printf("WARNING: %s not in %s%n", code, CountryCodes.class.getSimpleName());
        complete = false;
      }
      else if (!ourName.equalsIgnoreCase(jreName)) {
        System.out.printf("WARNING: our name for %s doesn't match the one provided by the JRE%n", code);
        complete = false;
      }
    }
    assertTrue("Our list of countries is incomplete", complete);
  }

  public void testFlagIcons() throws Exception {
    // check whether we have flag icons for all countries
    int missCount = 0;
    List<String> countryCodes = CountryCodes.listAllCodes();
    for (String code : countryCodes) {
      try {
        CountryFlagsBundle.class.getDeclaredMethod(code);
      }
      catch (NoSuchMethodException e) {
        missCount++;
        System.out.printf("No flag image available for %s (%s)%n", code, CountryCodes.getCountryName(code));
      }
    }
    System.out.printf("%s does not have flag images for %d/%d countries%n", CountryFlagsBundle.class.getSimpleName(), missCount, countryCodes.size());
  }

}