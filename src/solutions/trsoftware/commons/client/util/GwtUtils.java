/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.jso.JsDocument;

/**
 * Dec 27, 2009
 *
 * @author Alex
 */
public class GwtUtils {
  /* A command that does nothing */
  public static final Command EMPTY_COMMAND = new Command() {
    public void execute() {
    }
  };

  /**
   * Imitates the functionality of {@link Class#isAssignableFrom(Class)}, which isn't emulated by GWT.
   * @return true if an instance of c2 can be cast to c1
   */
  public static boolean isAssignableFrom(Class c1, Class c2) {
    // TODO: could replace this method by emulating Class (put it in super-source, or extract it to gwt-stack-trace-kit\patch\src\com\google\gwt\emul\java\lang\Class.java)
    // c2 can be cast to c1 if c1 is a superclass of c2
    Class next = c2;
    while (next != null) {
      if (c1.equals(next))
        return true;
      next = next.getSuperclass();
    }
    return false;  // c1 didn't match any superclass of c2
  }

  /**
   * Imitates the functionality of {@link Class#getSimpleName()} which isn't emulated by older versions of GWT
   * (at least not in 2.5.0, which is the version we need to enable stack traces using the gwt-stack-trace-kit patch)
   * @return The name of the given class without the preceding package path (if any).
   */
  public static String getSimpleName(Class c) {
    // TODO: cont here: unit test this impl, by comparing the results against the native Java impl (try various combinations of inner/anonymous classes)
    // TODO: then extract it to gwt-stack-trace-kit\patch\src\com\google\gwt\emul\java\lang\Class.java, and see if that works
    // running in Javascript - extract the name manually
    int lastSeparator;
    String name = c.getName();
    {
      int lastDot = name.lastIndexOf('.');
      int lastDollar = name.lastIndexOf('$');
      lastSeparator = Math.max(lastDollar, lastDot);
    }
    if (lastSeparator < 0)
      return name;
    return name.substring(lastSeparator+1);
  }

  public static Widget elementToWidget(Element element) {
    // idea borrowed from https://stackoverflow.com/questions/17855096/gwt-how-to-retrive-real-clicked-widget/17863305#17863305
    EventListener listener = DOM.getEventListener(element);
    // No listener attached to the element, so no widget exist for this element
    if (listener != null && (listener instanceof Widget)) {
      // GWT uses the widget as event listener
      return (Widget) listener;
    }
    return null;
  }

  public static Widget getFocusedWidget() {
    Element activeElement = JsDocument.get().getActiveElement();
    if (activeElement != null)
      return (Widget)elementToWidget(activeElement);
    return null;
  }

}
