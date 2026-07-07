/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.client.useragent;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import solutions.trsoftware.commons.shared.util.VersionNumber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A simple interface to the browser's userAgent string, which allows getting
 * more detailed info about the browser than GWT's "user.agent" selection property.
 *
 * This class serves a more lightweight purpose, when deferred binding is too
 * cumbersome, or when the "user.agent" property values defined in UserAgent.gwt.xml
 * are inadequate.
 *
 * @author Alex
 */
public class UserAgent {

  // Singleton
  private static UserAgent ourInstance;

  public static UserAgent getInstance() {
    // lazy init to allow unit testing this class in a JVM (outside of a GWT context)
    if (ourInstance == null)
      synchronized (UserAgent.class) {
        if (ourInstance == null)
          if (GWT.isClient())
            ourInstance = new UserAgent(getUAString());
          else
            ourInstance = new UserAgent("");  // no user agent string on server TODO: use ServletUtils to construct this instance on the server (based on the HttpServletRequest header)?
      }
    return ourInstance;
  }


  /** The current browser's {@code navigator.userAgent} string */
  @Nonnull
  private final String userAgentString;


  /** Exposed with protected visibility for unit testing */
  protected UserAgent(String userAgentString) {
    this.userAgentString = userAgentString;
  }

  /**
   *
   * @return the value of the current browser's {@code navigator.userAgent} property
   */
  @Nonnull
  public String getUserAgentString() {
    return userAgentString;
  }

  /** Returns the browser user agent string in lowercase */
  public String getUserAgentStringLowercase() {
    return userAgentString.toLowerCase();
  }

  /**
   * <strong>NOTE:</strong> This method doesn't work for IE11+ because Microsoft dropped {@code MSIE} from its UA string
   * starting with IE11 (see referenced article).
   *
   * @return {@code true} iff running on IE10 or older.
   * @see <a href="http://blogs.msdn.com/b/ieinternals/archive/2013/09/21/internet-explorer-11-user-agent-string-ua-string-sniffing-compatibility-with-gecko-webkit.aspx">
   *   Internet Explorer 11’s Many User-Agent Strings</a>
   */
  public boolean isIE() {
    /*
      TODO: add support for IE11+ and MS Edge?
        - MS Edge 90.0.818.49 UA Header:
          "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49"
        - IE 11 header:
          "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729; MALCJS; Zoom 3.6.0; rv:11.0) like Gecko"
          - perhaps could use the "MALCJS;" part or "Trident"?
     */
    return getUserAgentStringLowercase().contains("msie");
  }

  public String getReloadPageVerb() {
    if (isIE())
      return "refresh";
    return "reload";
  }

  /**
   * @return the value of JS {@code navigator.userAgent}
   * @see Window.Navigator#getUserAgent()
   */
  public static String getUAString() {
    return Window.Navigator.getUserAgent();
  };

  /**
   * Internet Explorer versions starting with 8 define a document.documentMode property, which, by default, returns
   * the version of the browser it's running on.
   * @see com.google.gwt.useragent.rebind.UserAgentPropertyGenerator
   * @return 0 for IE versions < 8 or any non-IE browser, 8 for IE8, 9 for IE9, 10 for IE10, and 11 for IE11
   * (and hopefully so on when versions newer than 11 are released)
   */
  public static native int getExplorerDocumentMode() /*-{
    return $doc.documentMode || 0;  // this property is defined on IE8 and up, but not on IE6 and 7
  }-*/;


  /**
   * This method takes a specific browser/engine name, and attempts to extract the version of that particular
   * product from the user agent string.
   * <p>
   * For example, this method returns {@link VersionNumber#VersionNumber(int...) VersionNumber(5, 0)}
   * when given the argument {@code "Mozilla"}, if {@code navigator.userAgent} is
   * <code style="white-space: nowrap;">
   *   "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"
   * </code>
   * <p>
   * <i>Note:</i> some browsers also provide a <a href="https://web.dev/user-agent-client-hints/#javascript-api">{@code navigator.userAgentData}</a>
   * property, which serves a similar purpose.
   *
   * @param browserName <i>case-insensitive</i> name of the product to look for in the UA string (e.g. {@code "Chrome"}, {@code "Mozilla"})
   * @return the version number of the given browser from the UA string, if present, or
   *   {@code null} if the given browser name is not present in the UA string
   * @see #parseVersionNumbers()
   * @see <a href="https://web.dev/user-agent-client-hints/#javascript-api"><code>navigator.userAgentData</code></a>
   */
  @Nullable
  public VersionNumber parseVersionNumber(String browserName) {
    return parseVersionNumber(userAgentString, browserName);
  }

  /**
   * Parses all the browserName/versionNumber entries (e.g. "Chrome/67.0.3396.99") contained in the current
   * {@link #userAgentString}.
   * <p>
   * For example, if the UA string is <code style="white-space: nowrap;">
   *   "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"</code>,
   * this method will return the equivalent of:
   * <pre>
   *   ImmutableMap.of(
   *     "Mozilla",     new {@link VersionNumber}(5, 0),
   *     "AppleWebKit", new {@link VersionNumber}(537, 36),
   *     "Chrome",      new {@link VersionNumber}(67, 0, 3396, 99),
   *     "Safari",      new {@link VersionNumber}(537, 36));
   * </pre>
   * Note: the character case of the browser names is retained from the original {@code userAgent} string.
   * To perform case-insensitive lookups, the returned map can be wrapped in a
   * {@link java.util.TreeMap} with the {@link String#CASE_INSENSITIVE_ORDER} comparator.
   *
   * @return a mapping of all the browserName/versionNumber entries extracted from the given string, or
   *   {@code null} if it contains no substrings matching the pattern
   *     <code style="white-space:nowrap;text-decoration:underline">/(\w+)/([\d.]+)/i</code>
   * @see #parseVersionNumber(String)
   * @see <a href="https://www.baeldung.com/java-map-with-case-insensitive-keys#treemap">Java Map With Case-Insensitive Keys</a>
   */
  @Nullable
  public ImmutableMap<String, VersionNumber> parseVersionNumbers() {
    return parseVersionNumbers(userAgentString);
  }

  /**
   * Modern browsers list several products in their user agent string, for example
   * <pre>"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"</pre>
   *
   * This method takes a specific browser name, and attempts to extract the version of that particular
   * browser from the given user agent string.
   *
   * @param uaString the value of {@code navigator.userAgent}
   * @param browserName <i>case-insensitive</i> name of the product to look for in the UA string (e.g. {@code "Chrome"}, {@code "Mozilla"})
   * @return the version number of the given browser from the UA string, if present, or
   *   {@code null} if the given browser name is not present in the UA string
   * @see #parseVersionNumbers(String)
   */
  @Nullable
  public static VersionNumber parseVersionNumber(String uaString, String browserName) {
    RegExp regExp = RegExp.compile(".*?" + browserName + "/([\\d.]+).*", "i");
    MatchResult match = regExp.exec(uaString);
    if (match != null) {
      String versionStr = match.getGroup(1);  // e.g. "67.0.3396.99"
      return VersionNumber.parse(versionStr);
    }
    return null;  // match not found
  }

  /**
   * Parses all the browserName/versionNumber entries (e.g. "Chrome/67.0.3396.99") contained in the given {@code userAgent} string.
   * <p>For example:
   * <pre>
   *   {@link #parseVersionNumbers}("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")
   *     // equals:
   *     ImmutableMap.of(
   *       "Mozilla",     new {@link VersionNumber}(5, 0),
   *       "AppleWebKit", new {@link VersionNumber}(537, 36),
   *       "Chrome",      new {@link VersionNumber}(67, 0, 3396, 99),
   *       "Safari",      new {@link VersionNumber}(537, 36));
   * </pre>
   * Note: the character case of the browser names is retained from the original {@code userAgent} string.
   * To perform case-insensitive lookups, the returned map can be wrapped in a
   * {@link java.util.TreeMap} with the {@link String#CASE_INSENSITIVE_ORDER} comparator.
   *
   * @param userAgent a {@code navigator.userAgent} string
   * @return a mapping of all the browserName/versionNumber entries extracted from the given string, or
   *   {@code null} if it contains no substrings matching the pattern
   *     <code style="white-space:nowrap;text-decoration:underline">/(\w+)/([\d.]+)/i</code>
   * @see #parseVersionNumber(String, String)
   * @see <a href="https://www.baeldung.com/java-map-with-case-insensitive-keys#treemap">Java Map With Case-Insensitive Keys</a>
   */
  @Nullable
  public static ImmutableMap<String, VersionNumber> parseVersionNumbers(String userAgent) {
    // Note: global flag ("g") required to find all matches; see Google AI overview @ https://www.google.com/search?q=gwt+regexp+find+all+matches&oq=gwt+regexp+find+all+matches&aqs=chrome..69i57j0i22i30l3j0i546i649j0i751j0i546i649.5057j0j7&sourceid=chrome&ie=UTF-8
    RegExp regExp = RegExp.compile("(\\w+)/([\\d.]+)", "gi");
    MatchResult match = regExp.exec(userAgent);
    if (match != null) {
      ImmutableMap.Builder<String, VersionNumber> builder = ImmutableMap.builder();
      while (match != null) {
        String browserName = match.getGroup(1);  // e.g. "Chrome"
        String versionStr = match.getGroup(2);  // e.g. "67.0.3396.99"
        builder.put(browserName, VersionNumber.parse(versionStr));
        match = regExp.exec(userAgent);
      }
      return builder.build();
    }
    return null;  // match not found
  }

  /**
   * @return an instance of {@link Property}, which provides both the compile time and runtime
   * <code>user.agent</code> property value (for GWT deferred binding).
   */
  public static Property getUserAgentProperty() {
    return new Property();
  }

  /**
   * Wraps an instance of {@link com.google.gwt.useragent.client.UserAgent}, which provides
   * both the compile time and runtime <code>user.agent</code> property value (for GWT deferred binding).
   */
  public static class Property {
    private com.google.gwt.useragent.client.UserAgent delegate
        = GWT.create(com.google.gwt.useragent.client.UserAgent.class);

    public String getCompileTimeValue() {
      return delegate.getCompileTimeValue();
    }

    public String getRuntimeValue() {
      return delegate.getRuntimeValue();
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(delegate.getClass())
          .add("compileTimeValue", getCompileTimeValue())
          .add("runtimeValue", getRuntimeValue())
          .toString();
    }
  }

}
