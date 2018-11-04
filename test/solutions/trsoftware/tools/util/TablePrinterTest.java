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

package solutions.trsoftware.tools.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static solutions.trsoftware.tools.util.TablePrinter.printMenu;

/**
 * @author Alex
 * @since 5/13/2018
 */
public class TablePrinterTest extends TestCase {

  public void testPrintMenu() throws Exception {
    // print a table of all the available locales in Java
    Locale[] availableLocales = Locale.getAvailableLocales();
    List<String> lines = new ArrayList<>();
    for (Locale locale : availableLocales) {
      lines.add(String.format("%s: displayName=%s, country=%s, language=%s",
          locale,
          locale.getDisplayName(),
          locale.getDisplayCountry(),
          locale.getDisplayLanguage()
      ));
    }
    printMenu(System.out, "All Available Locales:", lines);
    // TODO: verify some assertions?
  }
}