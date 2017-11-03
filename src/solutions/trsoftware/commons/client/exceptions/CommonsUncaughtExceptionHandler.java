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

package solutions.trsoftware.commons.client.exceptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.web.bindery.event.shared.UmbrellaException;
import solutions.trsoftware.commons.client.Messages;
import solutions.trsoftware.commons.client.Settings;
import solutions.trsoftware.commons.client.jso.JsConsole;
import solutions.trsoftware.commons.client.logging.Log;
import solutions.trsoftware.commons.client.useragent.UserAgent;
import solutions.trsoftware.commons.client.util.LazyInitFactory;
import solutions.trsoftware.commons.client.widgets.popups.ErrorMessagePopup;
import solutions.trsoftware.commons.client.widgets.popups.PopupDialog;
import solutions.trsoftware.gwt.stacktrace.client.StackTraceDeobfuscatorClient;

/**
 * Oct 12, 2011
 *
 * @author Alex
 */
public class CommonsUncaughtExceptionHandler implements GWT.UncaughtExceptionHandler {

  private PopupDialog exceptionNoticePopup;
  private LazyInitFactory<StackTraceDeobfuscatorClient> deobfuscatorClient = new LazyInitFactory<StackTraceDeobfuscatorClient>() {
    @Override
    protected StackTraceDeobfuscatorClient create() {
      Settings settings = Settings.get();
      String servletUrl = settings.getStackTraceDeobfuscatorServletUrl();
      if (servletUrl == null)
        throw new IllegalStateException("Did you forget to call Settings.setStackTraceDeobfuscatorServletUrl?");
      return new StackTraceDeobfuscatorClient(servletUrl, settings.getStackTraceSizeLimit());
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
    // 1) Invoke the new exception reporting mechanism
    reportException(e, 0);

    Log.error("Uncaught Exception", e); // also print the exception in the GWT dev mode or browser console

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

  private void showPopupNotice() {
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

  /** Subclasses may override to return {@code null} if they don't want a popup to be shown to the user */
  protected ErrorMessagePopup createExceptionNoticePopup() {
    return new ErrorMessagePopup(false,"Warning","exceptionNoticePopup",
        new HTML("Something unexpected just happened.  You may need to <strong><em>"
            + UserAgent.getInstance().getReloadPageVerb() + "</em></strong> the browser page if "
            + Messages.get().getAppName() + " stops working."));
  }

  /**
   * This is the new exception reporting mechanism based on stack traces
   * provided with the following GWT config:
   * <set-property name="compiler.stackMode" value="emulated" />
   * <set-configuration-property name="compiler.emulatedStack.recordLineNumbers" value="true"/>
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

  protected void onDeobfuscationResultAvailable(Throwable ex, String stackTrace) {
    JsConsole.get().error(ex.toString() + ":\n" + stackTrace);
  }
}
