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

package solutions.trsoftware.commons.client.logging;

import com.google.gwt.core.shared.GWT;

/**
 * Prints all logging information to GWT's console in hosted mode.
 * Delegates to the LogImpl in web mode.
 *
 * <p style="color: #0073BF; font-weight: bold;">
 *   TODO: this should be deprecated since GWT now supports {@link java.util.logging}
 *   (see http://www.gwtproject.org/doc/latest/DevGuideLogging.html)
 * </p>
 */
public class Log {

  private static final LogImpl impl = GWT.create(LogImpl.class);

  /**
   * Whether the write() method will have any effect.  This field can be used 
   * in a conditional statement to allow the compiler to eliminate the logging
   * statements in client code when logging is disabled.
   */
  public static final boolean ENABLED = !GWT.isScript() || impl.isLoggingEnabled();

  public static void write(String msg) {
    impl.log(msg);
    if (!GWT.isScript())
      GWT.log(msg, null);
  }

  /**
   * Same as write(msg), but additional actions may be taken for these
   * kinds of messages in the future
   * (e.g. posting them to the server to be logged).
   *
   * @param msg
   */
  public static void error(String msg, Throwable ex) {
    impl.error(msg, ex);
    if (!GWT.isScript())
      GWT.log(msg, ex);
  }

}
