package solutions.trsoftware.commons.client.jso;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.shared.io.TablePrinter;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.TimeUnit;

import java.util.*;

import static solutions.trsoftware.commons.client.jso.JsDateTest.dateRange;

/**
 * @author Alex
 * @since 6/11/2025
 */
public class JsDateFormatTest extends CommonsGwtTestCase {

  public void testSmartFormat() throws Exception {
    /* NOTE: this test should be run with "-runStyle Manual" to get accurate output,
       b/c HtmlUnit can't properly handle the Javascript Date formatting options
       (e.g. VM option -Dgwt.args="-runStyle Manual:1 -userAgents gecko1_8,safari")
     */
    JsDate now = JsDate.create();
    SortedSet<JsDate> dates = new TreeSet<>(getComparatorReversed());
    dates.add(now);
    // create a range of dates before now, to trigger every branch in the method
    dateRange(now, TimeUnit.HOURS, -3, -1).forEach(dates::add);
    dateRange(now, TimeUnit.DAYS, -9, -1).forEach(dates::add);
    dateRange(now, TimeUnit.MONTHS, -14, -1).forEach(dates::add);
    dateRange(dates.last(), TimeUnit.YEARS, -4, -1).forEach(dates::add);
    // TODO: also test some future dates

    // format the dates and print results
    SortedMap<JsDate, String> results =
        MapUtils.toMap(dates, JsDateFormat::smartFormat, () -> new TreeMap<>(getComparatorReversed()));

    TablePrinter resultsTable = new TablePrinter();
    results.forEach((date, result) ->
        resultsTable.newRow()
            .addCol("date", date)
            .addCol("smartFormat", result));
    // for now, just printing the results for visual inspection, but in future may want to add some assertions
    resultsTable.printTable();  // TODO: maybe print to getLogger() for web mode support

    // print to getLogger() for web mode support
    resultsTable.printTableToLogger(getLogger());
  }

  private Comparator<JsDate> getComparatorReversed() {
    Comparator<JsDate> comparator = new Comparator<JsDate>() {
      @Override
      public int compare(JsDate o1, JsDate o2) {
        return Double.compare(o1.getTime(), o2.getTime());
      }
    };
//    return comparator.reversed();
    return JsDate.comparator().reversed();
  }
}