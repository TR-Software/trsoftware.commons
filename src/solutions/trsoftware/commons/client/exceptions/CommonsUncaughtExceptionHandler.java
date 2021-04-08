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

package solutions.trsoftware.commons.client.exceptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.web.bindery.event.shared.UmbrellaException;
import solutions.trsoftware.commons.client.Messages;
import solutions.trsoftware.commons.client.Settings;
import solutions.trsoftware.commons.client.jso.JsConsole;
import solutions.trsoftware.commons.client.logging.Log;
import solutions.trsoftware.commons.client.templates.CommonTemplates;
import solutions.trsoftware.commons.client.useragent.UserAgent;
import solutions.trsoftware.commons.client.widgets.popups.ErrorMessagePopup;
import solutions.trsoftware.commons.client.widgets.popups.PopupDialog;
import solutions.trsoftware.commons.shared.util.LazyReference;
import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * Oct 12, 2011
 *
 * @author Alex
 */
public class CommonsUncaughtExceptionHandler implements GWT.UncaughtExceptionHandler {

  private PopupDialog exceptionNoticePopup;
  private LazyReference<StackTraceDeobfuscatorClient> deobfuscatorClient = new LazyReference<StackTraceDeobfuscatorClient>() {
    @Override
    protected StackTraceDeobfuscatorClient create() {
      Settings settings = Settings.get();
      String servletUrl = settings.getStackTraceDeobfuscatorServletUrl();
      if (servletUrl == null)
        throw new IllegalStateException("Did you forget to call Settings.setStackTraceDeobfuscatorServletUrl?");
      // NOTE: inheriting modules can override StackTraceDeobfuscatorClient with deferred binding
      StackTraceDeobfuscatorClient client = GWT.create(StackTraceDeobfuscatorClient.class);
      client.init(servletUrl, settings.getStackTraceSizeLimit());
      return client;
    }
  };

  /**
   * This method, onUncaughtException, should only be called behind-the-scenes
   * by GWT (when an exception is really uncaught).
   * When we wish to manually just log and ignore an exception, we should call
   * handleException(e, false).
   */
  public final void onUncaughtException(final Throwable e) {
    handleException(e, true);
  }

  public void handleException(final Throwable e, boolean uncaught) {
    Log.error("Uncaught Exception", e); // for of all, print the exception in the GWT dev mode shell, or browser console

    // 1) Invoke the new exception reporting mechanism
    reportException(e, 0);

    // 2) Let the user know that a problem has occurred; this is done after the reporting step, just in case it causes
    // an exception (which would cause the reporting code not to run otherwise)
    // NOTE: prior to R46 this code ran in a deferred command, but that sometimes caused weird UI-related exceptions
    // in prod (see: https://www.google.com/analytics/web/?hl=en#report/content-event-events/a1569770w2756850p2811177/%3F_u.date00%3D20140421%26_u.date01%3D20140821%26explorer-table.plotKeys%3D%5B%5D%26explorer-table.rowStart%3D0%26explorer-table.rowCount%3D100%26_r.drilldown%3Danalytics.eventCategory%3AExceptionStackTrace%26explorer-segmentExplorer.segmentId%3Danalytics.eventLabel/ )
    // the hypothesis was that the deferred command ran after the browser window was destroyed, so we decided not to do it in a deferred command,
    // to reduce the number of false exceptions reported to GA )
    showPopupNotice();
  }

  /**
   * @param recursionDepth prevents infinite recursion while unwrapping {@link UmbrellaException}s.  Pass 0 to start.
   */
  private void reportException(Throwable e, int recursionDepth) {
    if (e instanceof UmbrellaException && recursionDepth < 100) {
      // split the UmbrellaException into its constituents
      for (Throwable cause : ((UmbrellaException)e).getCauses())
        reportException(cause, recursionDepth + 1);
    }
    else {
      recordStackTraceToEventTracker(e);
    }
  }

  /**
   * Displays a popup warning user that an unexpected error has occurred.
   * <p>
   * Subclasses should override with an empty method if they don't want a popup to be shown.
   *
   * @see #createExceptionNoticePopup()
   */
  protected void showPopupNotice() {
    if (exceptionNoticePopup == null)  // lazy init
      exceptionNoticePopup = createExceptionNoticePopup();
    // still have to check for null because createExceptionNoticePopup could be overridden to return null if app doesn't want an error popup to be displayed
    if (exceptionNoticePopup != null) {
      if (exceptionNoticePopup.isShowing())
        exceptionNoticePopup.shake();
      else
        exceptionNoticePopup.showRelativeToWindow(.5, .2);
    }
  }

  /**
   * Creates a warning popup with a message rendered from the {@link CommonTemplates#uncaught_exception_warning()}
   * template.
   * <p>
   * Subclasses may override to return a different popup dialog (or override {@link #showPopupNotice()} instead if this
   * feature is not wanted).
   */
  protected ErrorMessagePopup createExceptionNoticePopup() {
    return new ErrorMessagePopup(false,"Warning",
        new HTML(CommonTemplates.INSTANCE.uncaught_exception_warning().render(
            "reloadPageVerb", UserAgent.getInstance().getReloadPageVerb(),
            "appName", Messages.get().getAppName())));
  }

  /**
   * This is the new exception reporting mechanism based on stack traces
   * provided with the following GWT config:
   * <pre>{@code
   * <set-property name="compiler.stackMode" value="emulated" />
   * <set-configuration-property name="compiler.emulatedStack.recordLineNumbers" value="true"/>
   * }</pre>
   * We deobfuscate the stack trace on the server using the GWT compiler-generated
   * symbolMaps files and log it with EventTracker
   */
  protected void recordStackTraceToEventTracker(final Throwable ex) {
    deobfuscatorClient.get().deobfuscateStackTrace(ex, new StackTraceDeobfuscatorClient.Callback() {
      @Override
      public void onDeobfuscationResultAvailable(Throwable ex, String stackTrace) {
        CommonsUncaughtExceptionHandler.this.onDeobfuscationResultAvailable(ex, stackTrace);
      }
    });
  }

  /**
   * Calls {@link #logException(String)} with a string containing the exception message and its de-obfuscated stack
   * trace.
   * <p>
   * Subclasses may override.
   *
   * @param ex the uncaught exception
   * @param stackTrace the de-obfuscated stack trace of the given exception
   */
  protected void onDeobfuscationResultAvailable(Throwable ex, String stackTrace) {
    logException(ex.toString() + ":\n" + StringUtils.firstNotBlank(stackTrace, "<no stack trace available>"));
  }

  /**
   * Prints the given message to the JS console.
   * <p>
   * Subclasses may override.
   *
   * @param exceptionWithStackTrace a string containing the exception message and the de-obfuscated stack trace
   */
  protected void logException(String exceptionWithStackTrace) {
    JsConsole.get().log(JsConsole.Level.ERROR, exceptionWithStackTrace);
  }
}
