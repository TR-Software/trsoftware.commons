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
