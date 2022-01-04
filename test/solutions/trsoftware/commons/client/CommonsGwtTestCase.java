/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.widgets.popups.ModalDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A convenience base case that sets the module name, so that overriding
 * tests don't have to.
 *
 * @author Alex
 */
public abstract class CommonsGwtTestCase extends BaseGwtTestCase {

  public static final String MODULE_NAME = "solutions.trsoftware.commons.TestCommons";

  public String getModuleName() {
    return MODULE_NAME;
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    // prevent locking up the page with window.alert / prompt / confirm  (this is important for running tests under "-runStyle Manual"
    ModalDialog.setNativeDialogsEnabled(false);
  }

  @Override
  protected void gwtTearDown() throws Exception {
    RootPanel.get().clear();
    super.gwtTearDown();
  }

  /**
   * Recent versions of GWT run tests in web mode by default, so {@code System.out} can no longer be used to print
   * output. Instead, we can use <a href="http://www.gwtproject.org/doc/latest/DevGuideLogging.html">GWT's logging implementation</a>
   * <p>
   *   NOTE: the module used to run the test must properly set up the logging.  Example:
   *   <pre>{@code
   *     <inherits name="com.google.gwt.logging.Logging"/>
   *     <set-property name="gwt.logging.enabled" value="TRUE"/>
   *   }</pre>
   * </p>
   * @return a logger that can be used to print console messages when running the test in web mode
   * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideLogging.html">http://www.gwtproject.org/doc/latest/DevGuideLogging.html</a>
   */
  protected Logger getLogger() {
    return Logger.getLogger(getName());
  }

  /**
   * Prints a simple message to the default logger for the current test (at the {@link Level#INFO} level)
   * @param msg string to be logged
   * @see #getLogger()
   */
  protected void log(String msg) {
    getLogger().log(Level.INFO, msg);
  }


}
