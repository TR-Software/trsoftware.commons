package solutions.trsoftware.commons.client.useragent;

import com.google.gwt.core.client.JsDate;

/**
 * @author Alex
 * @since 1/30/2019
 */
public class PolyfillIE8 extends Polyfill {

  @Override
  public native String toISOString(JsDate date) /*-{
    if (date.toISOString) {
      return date.toISOString();
    } else {
      function pad(number) {
        return number < 10 ? '0' + number : number;
      }

      return date.getUTCFullYear() +
          '-' + pad(date.getUTCMonth() + 1) +
          '-' + pad(date.getUTCDate()) +
          'T' + pad(date.getUTCHours()) +
          ':' + pad(date.getUTCMinutes()) +
          ':' + pad(date.getUTCSeconds()) +
          '.' + (date.getUTCMilliseconds() / 1000).toFixed(3).slice(2, 5) +
          'Z';
    }
  }-*/;
}
