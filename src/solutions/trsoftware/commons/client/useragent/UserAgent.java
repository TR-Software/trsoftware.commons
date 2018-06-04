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

package solutions.trsoftware.commons.client.useragent;

import com.google.gwt.core.shared.GWT;

/**
 * A simple interface to the browser's userAgent string, which allows getting
 * more detailed info about the browser than GWT's "user.agent" selection property.
 *
 *  This class serves a more lightweight purpose, when deferred binding is too
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
            ourInstance = new UserAgent("");  // no user agent string on server
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
   * @return true iff running on IE10 or older. NOTE: starting with IE11, this is no longer true, as explained in
   * http://blogs.msdn.com/b/ieinternals/archive/2013/09/21/internet-explorer-11-user-agent-string-ua-string-sniffing-compatibility-with-gecko-webkit.aspx )
   */
  public boolean isIE() {
    return userAgentStringLowercase.contains("msie");
  }

  public String getReloadPageVerb() {
    if (isIE())
      return "refresh";
    return "reload";
  }

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
}
