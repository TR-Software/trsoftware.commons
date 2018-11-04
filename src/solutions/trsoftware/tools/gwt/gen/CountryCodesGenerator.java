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

package solutions.trsoftware.tools.gwt.gen;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Generates code for {@link solutions.trsoftware.commons.shared.data.CountryCodes} from {@link Locale} data.
 * @author Alex
 * @since 11/20/2017
 */
public class CountryCodesGenerator {

  public static void main(String[] args) {
    List<String> isoCountries = Arrays.asList(Locale.getISOCountries());
    int i = 0;
    for (String code : isoCountries) {
      Locale loc = new Locale("", code);
      System.out.printf("\"%s\", \"%s\"%s%n",
          code.toLowerCase(), loc.getDisplayName(), (++i < isoCountries.size()) ? "," : "");
    }
  }

}
