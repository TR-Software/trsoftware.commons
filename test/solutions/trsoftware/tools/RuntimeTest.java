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

package solutions.trsoftware.tools;

import com.google.common.collect.HashBiMap;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.sql.Timestamp;
import java.text.Collator;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class can be used to test the run-time behavior of various Java features that might be ambiguous.
 *
 * @author Alex
 * @since 11/29/2017
 */
public class RuntimeTest extends TestCase {

  /**
   * Tests what happens when there is an overloaded method for a more general type and one for a more specific type.
   */
  public void testPolymorphism() throws Exception {
    class PolyTester {
      private Date date;

      public void setDate(Date date) {
        Class<? extends Date> dateClass = date.getClass();
        System.out.printf("setter for %s invoked with instance of %s%n", Date.class.getName(), dateClass.getName());
        this.date = date;
      }

      public void setDate(Timestamp date) {
        Class<? extends Timestamp> dateClass = date.getClass();
        System.out.printf("setter for %s invoked with instance of %s%n", Timestamp.class.getName(), dateClass.getName());
        this.date = date;
      }
    }

    Date d = new Date();
    Timestamp t = new Timestamp(System.currentTimeMillis());
    PolyTester polyTester = new PolyTester();
    polyTester.setDate(d);  // the Date method should be invoked
    polyTester.setDate(t);  // the Timestamp method should be invoked
    polyTester.setDate((Date)t);  // the Date method should be invoked if we up-cast a Timestamp to Date
  }

  /**
   * Tests be behavior of the various {@link java.text.Collator} settings, attempting to find a setting
   * that can match the behavior of the MySQL "utf8_general_ci" collation.
   * @see <a href="https://stackoverflow.com/q/36151582">StackOverflow question</a>
   */
  public void testCollator() throws Exception {
    String[] strengthNames = {"PRIMARY", "SECONDARY", "TERTIARY", "IDENTICAL"};

    class CollatorTester {
      HashBiMap<String, Integer> strengthValues = HashBiMap.create();
      private Supplier<Collator> collatorSupplier;

      public CollatorTester() throws NoSuchFieldException, IllegalAccessException {
        this(Collator::getInstance);
      }

      public CollatorTester(Supplier<Collator> collatorSupplier) throws NoSuchFieldException, IllegalAccessException {
        this.collatorSupplier = collatorSupplier;
        Class<Collator> collatorClass = Collator.class;
        for (String name : strengthNames) {
          strengthValues.put(name, (Integer)collatorClass.getField(name).get(collatorClass));
        }
      }

      Map<String, Integer> compareAll(String first, String second) {
        System.out.println(StringUtils.methodCallToString("Collator.compare", first, second));

        LinkedHashMap<String, Integer> results = new LinkedHashMap<>();
        for (String strengthName : strengthNames) {
          int cmp = compare(first, second, strengthValues.get(strengthName));
          results.put(strengthName, cmp);
        }
        System.out.println("  -> " + results);
        System.out.println("  first.toLowerCase() = " + first.toLowerCase());
        System.out.println("  second.toLowerCase() = " + second.toLowerCase());
        System.out.println("  first.equalsIgnoreCase(second) = " + first.equalsIgnoreCase(second));
        return results;
      }

      private int compare(String first, String second, int strength) {
        Collator collator = Collator.getInstance();
        collator.setStrength(strength);
        return collator.compare(first, second);
      }
    }


    Locale[] availableLocales = Locale.getAvailableLocales();
    System.out.println("Locale.getAvailableLocales() = " + Arrays.toString(availableLocales));


    Locale[] locales = {Locale.getDefault(), Locale.ROOT, Locale.forLanguageTag("ru")};

    for (Locale locale : locales) {
      System.out.printf("============== Locale: %s ==============%n", locale);
      CollatorTester tester = new CollatorTester(() -> Collator.getInstance(locale));

      System.out.println("ASCII strings:");
      tester.compareAll("abc", "Abc");
      tester.compareAll("abc", "ABC");
      tester.compareAll("abc", "ÀBC");  // accent marks

      System.out.println("\nUnicode (Russian) strings:");
      // Russian
      tester.compareAll("АВС", "ABC");  // ASCII "ABC" vs. Russian "АВС"
      tester.compareAll("АВС", "авс");  // upper v. lower
      tester.compareAll("авс", "АВС");  // lower v. upper

    }
    // TODO: is there any way to get case-insensitive comparison for non-English strings? Can't seem to get the Russian upper/lower strings to compare as equal using any locale
    IntStream cmpInEveryLocale = Arrays.stream(availableLocales).mapToInt(locale -> {
      Collator collator = Collator.getInstance(locale);
      collator.setStrength(Collator.PRIMARY);
      return collator.compare("АВС", "авс");
    });
    System.out.println("cmpInEveryLocale.anyMatch(cmp -> cmp == 0) = " + cmpInEveryLocale.anyMatch(cmp -> cmp == 0));

    List<Locale> caseInsensitiveLocales = Arrays.stream(availableLocales).filter(locale -> {
      Collator collator = Collator.getInstance(locale);
      collator.setStrength(Collator.PRIMARY);
      return collator.compare("АВС", "авс") == 0;
    }).collect(Collectors.toList());
    System.out.println("caseInsensitiveLocales = " + caseInsensitiveLocales);
  }

  private int compareWithCollator(String first, String second, int strength) {
     Collator collator = Collator.getInstance();
     collator.setStrength(strength);
     return collator.compare(first, second);
  }

}
