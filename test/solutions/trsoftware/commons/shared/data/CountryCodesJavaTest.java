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

package solutions.trsoftware.commons.shared.data;

import com.google.gwt.core.shared.GwtIncompatible;
import junit.framework.TestCase;
import solutions.trsoftware.commons.client.images.flags.CountryFlagsBundle;
import solutions.trsoftware.commons.shared.util.SetUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

//import solutions.trsoftware.commons.client.images.flags_svg.CountryFlagsBundle2;

/**
 * @author Alex
 * @since 11/20/2017
 */
@GwtIncompatible
public class CountryCodesJavaTest extends TestCase {

  public void testGetCountryName() throws Exception {
    // check that the class contains all the ISO codes available in Java Locales
    String[] isoCountries = Locale.getISOCountries();
    LinkedHashMap<String, String> mismatchedNames = new LinkedHashMap<>();
    LinkedHashMap<String, String> expectedNames = new LinkedHashMap<>();
    /* Treat the following countries a special case:
       1) Hong Kong (JRE wants to call it "Hong Kong SAR China")
       1) Macao (JRE wants to call it "Macao SAR China")
     */
    LinkedHashSet<String> exclusions = SetUtils.newSet("HK", "MO");
    for (String code : isoCountries) {
      Locale loc = new Locale("", code);
      String jreName = loc.getDisplayName();
      String ourName = CountryCodes.getCountryName(code.toLowerCase());
      System.out.printf("%s: %s [our name = %s] (ru: %s; es: %s)%n", code, jreName, ourName,
          loc.getDisplayName(Locale.forLanguageTag("ru")), loc.getDisplayName(Locale.forLanguageTag("es")));
      if (ourName == null) {
        System.out.printf("WARNING: %s not in %s%n", code, CountryCodes.class.getSimpleName());
        mismatchedNames.put(code, ourName);
        expectedNames.put(code, jreName);
      }
      else if (!ourName.equalsIgnoreCase(jreName)) {
        System.out.printf("WARNING: our name for %s doesn't match the one provided by the JRE%n", code);
        if (!exclusions.contains(code)) {
          mismatchedNames.put(code, ourName);
          expectedNames.put(code, jreName);
        }
      }
    }
    assertTrue(String.format("Our list of countries is incomplete; our names: %s; expected: %s", mismatchedNames, expectedNames),
        mismatchedNames.isEmpty());
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

/*  public void testFlagIcons2() throws Exception {
    // check whether we have flag icons for all countries
    int missCount = 0;
    List<String> countryCodes = CountryCodes.listAllCodes();
    // TODO: cont here: make this a relative path (based on package)
    File baseDir = new File("C:\\Users\\Alex\\Documents\\Projects\\TR Commons\\src\\solutions\\trsoftware\\commons\\client\\images\\flags_svg");
    for (String code : countryCodes) {
      File flagFile = new File(baseDir, code + ".svg");
      if (flagFile.exists()) {
        System.out.printf("Flag for '%s': %s%n", code, flagFile.getName());
      }
      else {
        missCount++;
        System.out.printf("WARNING: No flag image available for %s (%s)%n", code, CountryCodes.getCountryName(code));
      }
    }
    if (missCount > 0) {
      System.out.printf("WARNING: %s does not have flag images for %d/%d countries%n", CountryFlagsBundle2.class.getSimpleName(), missCount, countryCodes.size());
    }
    else {
      System.out.printf("%s has flag images for all %d countries%n", CountryFlagsBundle2.class.getSimpleName(), countryCodes.size());
    }
  }*/

}