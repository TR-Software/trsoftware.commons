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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.jso.JsDocument;
import solutions.trsoftware.commons.shared.util.reflect.ClassNameParser;

/**
 * Dec 27, 2009
 *
 * @author Alex
 */
public class GwtUtils {

  /** Caches the return value of {@link #emptyCommand()} */
  private static Command emptyCommand;

  /**
   * @return A command that does nothing.
   */
  public static Command emptyCommand() {
    if (emptyCommand == null)
      emptyCommand = new Command() {
        public void execute() {}
      };
    return emptyCommand;
  }

  /**
   * Imitates the functionality of {@link Class#isAssignableFrom(Class)}, which isn't emulated by GWT, by transitively
   * examining the chain of {@link Class#getSuperclass() superclasses} of c2 until c1 is encountered.
   * <p>
   *   <strong>NOTE: </strong> since GWT doesn't emulate {@link Class#getInterfaces()}, this method is only able to check
   *   the hierarchy of superclasses (but not superinterfaces), and therefore will not always produce the same result
   *   as {@link Class#isAssignableFrom(Class)}
   * </p>
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO: for the aforementioned reasons, should probably rename this method to "isSubclass", and replace all
   *   usages that need true "isAssignableFrom" functionality with a try/catch block for ClassCastException (wherever possible)
   * </p>
   * @param c1 should be a class (not an interface)
   * @param c2 should be a class (not an interface)
   * @return {@code true} if an instance of c2 can be cast to c1.
   * <strong>NOTE:</strong> <em>a return value of {@code false} does not necessarily mean that such a cast will fail</em>,
   * because this method is able to check only {@link Class#getSuperclass()} (but not {@link Class#getInterfaces()},
   * which isn't emulated by GWT).
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
   * Imitates the functionality of {@link Class#getSimpleName()} (which isn't emulated by older versions of GWT)
   * with the following differences:
   * <ul>
   *   <li>
   *   If the class is anonymous, we return a string containing the "complex" name (see {@link ClassNameParser#getComplexName()})
   *   followed by {@code "::"} and the simple name of the superclass (whereas {@link Class#getSimpleName()} would return
   *   an empty string).  If the anonymous class simply implements an interface (like {@link Runnable}) instead of a
   *   base class, the result would be something like {@code "Foo$1::Object"}
   *   (since GWT doesn't provide a way to get the implemented interfaces of a class).
   *   </li>
   * </ul>
   * @see Class#getSimpleName()
   * @see ClassNameParser
   *
   * @deprecated GWT now supports {@link Class#getSimpleName()} natively (via JRE emulation)
   */
  public static String getSimpleName(Class c) {
    ClassNameParser parser = new ClassNameParser(c);
    if (!parser.isAnonymous())
      return parser.getSimpleName();
    else {
      /*
       Class.getSimpleName returns an empty string for anonymous classes, but we want to provide some debugging info,
       so we return the "complex name" as well as the simple name of the superclass.  This should work fine,
       because there isn't any way for a superclass to be anonymous.
        */
      Class sup = c.getSuperclass();
      if (sup == Object.class)
        return parser.getComplexName();
      else {
        String supSimpleName = getSimpleName(sup); // NOTE: this will not cause infinite recursion, since a superclass cannot be anonymous
        return parser.getComplexName() + "::" + supSimpleName;
      }
    }
  }

  /**
   * Same as the default implementation of {@link Object#toString()}, but prints only the simple name of the object's
   * class (obtained via {@link #getSimpleName(Class)}).
   *
   * TODO: unit test this
   *
   * @param obj the object to print
   * @return {@code "<SimpleName>@<hashCode>"} or {@code "null"} if the argument is {@code null}
   */
  public static String toString(Object obj) {
    if (obj == null)
      return "null";
    return getSimpleName(obj.getClass()) + "@" + Integer.toHexString(obj.hashCode());
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
      return elementToWidget(activeElement);
    return null;
  }

  /**
   * @throws UnsupportedOperationException if {@link GWT#isClient()} returns {@code false}.
   */
  public static void assertIsClient() {
    if (!GWT.isClient()) {
      throw new UnsupportedOperationException("GWT-only");
    }
  }
}
