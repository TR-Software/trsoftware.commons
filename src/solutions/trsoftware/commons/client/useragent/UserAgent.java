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
import com.google.gwt.core.shared.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import solutions.trsoftware.commons.shared.util.VersionNumber;

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

  /** Exposed with protected visibility for unit testing */
  protected UserAgent(String userAgentString) {
    this.userAgentStringLowercase = userAgentString.toLowerCase();
  }

  private String userAgentStringLowercase;


  /** Returns the browser user agent string in lowercase */
  public String getUserAgentStringLowercase() {
    return userAgentStringLowercase;
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
     */
    return userAgentStringLowercase.contains("msie");
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
  public static native String getUAString() /*-{
    return navigator.userAgent || ""
  }-*/;

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
   * Modern browsers list several products in their user agent string, for example
   * <pre>Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36</pre>
   * Therefore this method takes a specific browser name, and attempts to extract the version of that particular
   * browser from the user agent string.
   * @param browserName the name of the product to look for in the UA string, e.g. {@code Chrome}, {@code Mozilla}, etc.
   * @return the version number of the given browser from the UA string, if present, or {@code null} if the given browser
   * name isn't mentioned anywhere in the UA string
   */
  public VersionNumber parseVersionNumber(String browserName) {
    return parseVersionNumber(this.userAgentStringLowercase, browserName);
  }

  /**
   * Modern browsers list several products in their user agent string, for example
   * <pre>Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36</pre>
   * Therefore this method takes a specific browser name, and attempts to extract the version of that particular
   * browser from the user agent string.
   * @param uaString the value of {@code navigator.userAgent}
   * @param browserName the name of the product to look for in the UA string, e.g. {@code Chrome}, {@code Mozilla}, etc.
   * @return the version number of the given browser from the UA string, if present, or {@code null} if the given browser
   * name isn't mentioned anywhere in the UA string
   */
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
