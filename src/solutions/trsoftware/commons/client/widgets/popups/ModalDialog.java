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

package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;
import solutions.trsoftware.commons.client.event.Events;
import solutions.trsoftware.commons.client.event.KeyHoldDetector;
import solutions.trsoftware.commons.client.event.MultiHandlerRegistration;
import solutions.trsoftware.commons.client.styles.WidgetStyle;
import solutions.trsoftware.commons.client.util.GwtUtils;
import solutions.trsoftware.commons.client.util.SmartTimer;
import solutions.trsoftware.commons.client.widgets.FocusTrap;
import solutions.trsoftware.commons.client.widgets.Widgets;
import solutions.trsoftware.commons.shared.util.Assert;

import java.util.LinkedList;

/**
 * <p>
 *   Provides a more reliable alternative to {@link Window#alert(String)}, {@link Window#confirm(String)}, and
 *   {@link Window#prompt(String, String)} for showing a message/prompt in a modal dialog box.
 * </p>
 * <p>
 *   Those native methods are unreliable because Chrome and Firefox give the the user a checkbox to
 *   "<i>Prevent this page from creating additional dialogs</i>", and if the user opts out of dialogs that way,
 *   Javascript doesn't know about it, and all future calls to those methods will either
 *   <ol>
 *     <li>throw an exception (Firefox), or,</li>
 *     <li>even worse, simply return without showing the dialog (Chrome)</li>
 *   </ol>
 * </p>
 * <p>
 *   This class is designed to ensure that the desired dialog gets displayed, by falling back on a
 *   {@linkplain #softAlert(String, ResponseHandler) soft modal dialog} implementation when
 *   the native dialog fails to display.
 * </p>
 *   Tested in Chrome, FF, and IE8/9/10/11 on Windows.
 * <p>
 *   <strong>Example:</strong>
 *   <ol>
 *     <li>
 *       The safe {@link #alert(String)} method implemented here first tries to delegate to {@link Window#alert(String)}
 *       (if {@link #nativeDialogsEnabled} == {@code true}), and if that fails.
 *     </li>
 *     <li>falls back on {@link #softAlert(String)}.</li>
 *   </ol>
 * </p>
 * @author Alex, 10/4/2015
 * @see #alert(String, ResponseHandler)
 * @see #prompt(String, String, ResponseHandler)
 */
public class ModalDialog {

  /**
   * If the native alert/confirm/prompt methods return within this amount of time, those calls will be considered failed.
   * Some browsers (e.g. Chrome) don't throw an exception from those methods when the user has chosen to
   * "Prevent this page from creating additional dialogs"; they simply return a "Cancel" response right away.
   * The trick here is to make this duration long enough to account for normal CPU lag (on slow or busy systems)
   * but short enough to be sure it's not just a fast user response.  The latter case is actually not that big a deal,
   * because it would result in the user simply seeing the dialog twice.
   */
  private static final int MIN_BLOCKING_MILLIS = GWT.isScript() ? 50 : 200;  // hosted mode is much slower, so we give it more time

  /** The instance currently showing */
  private static Dialog currentlyShowing = null;
  /**
   * If another instance is already showing, requests to show a soft dialog are added to this queue, to be displayed
   * after the previous one is closed.
   */
  private static final LinkedList<Dialog> toBeShown = new LinkedList<Dialog>();

  private static final KeyHoldDetector keyHoldDetector = new KeyHoldDetector();
  static {
    keyHoldDetector.start();
  }

  /** Will be set while waiting for {@link #minDelayBetweenDialogs} to elapse */
  private static Duration sinceLastDialogClosed;

  /** Will be started when we have wait before showing a dialog */
  private static SmartTimer waitingTimer = new SmartTimer() {
    @Override
    public void doRun() {
      // Waiting for minDelayBetweenDialogs or keyRelease
      maybeShowNextInQueue();
    }
  };


  // user-modifiable prefs:

  /**
   * If this setting is changed to {@code false}, all calls to {@link #alert(String)}, {@link #confirm(String)},
   * and {@link #prompt(String, String)} will use the "soft" implementations instead of delegating to {@link Window}.
   */
  private static boolean nativeDialogsEnabled = true;

  /**
   * Milliseconds to wait between consecutive dialogs.  This is useful for 2 reasons:
   * 1) some browsers automatically suppress native dialogs when they are shown too frequently
   * 2) if user holds the Enter or Esc key too long when closing a dialog, that keyDown event will be received by the next
   * dialog (because most browsers will repeatedly generate onKeyDown events while a key is being held down).
   * Having this field set to a value greater than 50 ms or so will ensure the proper operation of
   * {@link #keyHoldDetector}.
   */
  private static int minDelayBetweenDialogs = 200;

  /**
   * Milliseconds that a dialog should remain visible.  If the user tries to close it before it's been showing for this
   * amount of time, a soft dialog will remain open and a native dialog will fall back to a soft dialog,
   * so the user will have to repeat his response a bit later in order to close the dialog and invoke its {@link ResponseHandler}.
   * This is useful to prevent accidentally clicking on a button or holding down the Enter or Esc key before the user
   * has had a chance to read the message.
   *
   * NOTE: browsers don't seem to do anything about the possibility of user holding down Enter key in window.alert/confirm/prompt
   * (they just close the next dialog immediately while the Enter or Esc key is held), but since we're going through all
   * the trouble of reimplementing those dialogs, we're trying to do a better job at handling that scenario.
   */
  private static int minDialogShowingTime = 500;

  private ModalDialog() {  // make this class uninstantiable
  }

  /**
    * @return {@code false} iff all calls to {@link #alert(String)}, {@link #confirm(String)}, and {@link #prompt(String, String)}
    * are configured to use the "soft" implementations instead of delegating to {@link Window}.
    */
   public static boolean isNativeDialogsEnabled() {
     return nativeDialogsEnabled;
   }

   /**
    * @param nativeDialogsEnabled {@code false} iff all calls to {@link #alert(String)}, {@link #confirm(String)},
    * and {@link #prompt(String, String)} should use the "soft" implementations instead of delegating to {@link Window}.
    */
   public static void setNativeDialogsEnabled(boolean nativeDialogsEnabled) {
     ModalDialog.nativeDialogsEnabled = nativeDialogsEnabled;
   }

  /**
   * @return {@link #minDelayBetweenDialogs}
   */
  public static int getMinDelayBetweenDialogs() {
    return minDelayBetweenDialogs;
  }

  /**
   * Sets {@link #minDelayBetweenDialogs}
   */
  public static void setMinDelayBetweenDialogs(int minDelayBetweenDialogs) {
    ModalDialog.minDelayBetweenDialogs = minDelayBetweenDialogs;
  }

  /**
   * @return {@link #minDialogShowingTime}
   */
  public static int getMinDialogShowingTime() {
    return minDialogShowingTime;
  }

  /**
   * Sets {@link #minDialogShowingTime}
   */
  public static void setMinDialogShowingTime(int minDialogShowingTime) {
    ModalDialog.minDialogShowingTime = minDialogShowingTime;
  }

  /**
   * @return true iff a dialog was shown as a result of this call
   */
  private static boolean maybeShowNextInQueue() {
    if (currentlyShowing == null) {
      if (!toBeShown.isEmpty()) {
        if (!keyHoldDetector.isStarted()) {
          keyHoldDetector.start();
          Assert.assertTrue(!waitingTimer.isScheduled());
          // we have to wait for a bit to allow the keyHoldDetector to figure out whether any keys are being held down
          waitingTimer.schedule(minDelayBetweenDialogs);
        }
        else if (getMillisUntilReadyToShowNextDialog() == 0 && !keyHoldDetector.isAnyKeyHeld()) {
          // we're ready to show it right now (this will cancel the waitingTime if needed
          showNextInQueue();
          return true;
        }
        else if (!waitingTimer.isScheduled()) {
          int millisUntilReady = getMillisUntilReadyToShowNextDialog();
          if (millisUntilReady > 0)
            waitingTimer.schedule(millisUntilReady + 1);
          else if (keyHoldDetector.isAnyKeyHeld())
            waitingTimer.scheduleRepeating(10); // wait until all the keys are released (checking every 10ms)
        }
      }
      else
        keyHoldDetector.stop(); // we don't need this on the event preview stack when no dialog is showing and the queue is empty
    }
    return false;
  }

  private static void showNextInQueue() {
    Assert.assertTrue(currentlyShowing == null);
    waitingTimer.cancel();
    Dialog d = toBeShown.poll();
    currentlyShowing = d;
    sinceLastDialogClosed = null;
    d.open();
  }

  /**
   * @return the number of milliseconds remaining until we can show another dialog, or 0 if ready to show immediately.
   */
  private static int getMillisUntilReadyToShowNextDialog() {
    return Math.max(0, minDelayBetweenDialogs - getMillisSinceLastDialogClosed());
  }


  private static int getMillisSinceLastDialogClosed() {
    return sinceLastDialogClosed != null ? sinceLastDialogClosed.elapsedMillis() : Integer.MAX_VALUE;
  }

  private static void dialogClosed(Dialog d, boolean showNextInQueue) {
    Assert.assertTrue(d == currentlyShowing);
    currentlyShowing = null;
    sinceLastDialogClosed = new Duration();
    if (showNextInQueue)
      maybeShowNextInQueue();
  }

  /**
   * Queues up the given dialog to be displayed after all the other outstanding dialogs.
   */
  private static void enqueue(Dialog d) {
    toBeShown.addLast(d);
    maybeShowNextInQueue();
  }

  /**
   * Queues up the given dialog to be displayed at the next opportunity.
   */
  private static void enqueueFirst(Dialog d) {
    toBeShown.addFirst(d);
    maybeShowNextInQueue();
  }

  /**
   * Same as {@link #softAlert(String, ResponseHandler)} but does not invoke a response handler when user responds to the dialog.
   */
  @SuppressWarnings("unchecked")
  public static void softAlert(String msg) {
    softAlert(msg, ResponseHandler.NOOP);
  }

  /**
   * Similar to {@link Window#alert(String)}, but uses a custom widget to display the message
   * instead of the browser-native dialog.
   *
   * @param msg the message to be displayed
   */
  public static void softAlert(String msg, ResponseHandler<Void> responseHandler) {
    enqueue(new SoftAlert(msg, responseHandler));
  }

  /**
   * @param settings will be used to customize the dialog caption, and text/html for the "OK" button
   * @see #softAlert(String, ResponseHandler)
   */
  public static void softAlert(String msg, SoftDialogSettings settings, ResponseHandler<Void> responseHandler) {
    enqueue(new SoftAlert(msg, settings, responseHandler));
  }

  /**
   * Similar to {@link Window#confirm(String)}, but uses a custom widget to display the message
   * instead of the browser-native dialog.
   *
   * @param msg the message to be displayed
   * @param responseHandler Will be invoked with the argument {@code true} if the "OK" button is clicked, and
   * {@code false} if the "Cancel" button is clicked
   */
  public static void softConfirm(String msg, ResponseHandler<Boolean> responseHandler) {
    enqueue(new SoftConfirm(msg, responseHandler));
  }
  
  /**
   * Same as {@link #softAlert(String, ResponseHandler)}, but allows specifying custom labels for the "OK" and "Cancel" buttons.
   * @param msg the message to be displayed
   * @param settings will be used to customize the dialog caption, and text/html for the "OK" and "Cancel" buttons.
   * @param responseHandler Will be invoked with the argument {@code true} if the "OK" button is clicked, and
   * {@code false} if the "Cancel" button is clicked
   */
  public static void softConfirm(String msg, SoftDialogSettings settings, ResponseHandler<Boolean> responseHandler) {
    enqueue(new SoftConfirm(msg, responseHandler, settings));
  }

  /**
   * Similar to {@link Window#prompt(String, String)}, but uses a custom widget to display the prompt
   * instead of the browser-native dialog.
   * @param msg the message to be displayed
   * @param initialValue the initial value in the dialog's text field
   * @param responseHandler Will be invoked with the value entered by the user if "OK" was pressed.
   * <b style="color:red;">WARNING</b>: the value passed back to {@link ResponseHandler#handleDialogResponse(Object)}
   * will be {@code null} if user clicked the "Cancel" button instead of "OK"
   */
  public static void softPrompt(String msg, String initialValue, ResponseHandler<String> responseHandler) {
    enqueue(new SoftPrompt(msg, initialValue, responseHandler));
  }

  /**
   * Similar to {@link Window#prompt(String, String)}, but uses a custom widget to display the prompt
   * instead of the browser-native dialog.
   * @param msg the message to be displayed
   * @param settings will be used to customize the initial value in the dialog's text field,
   * the dialog caption, and text/html for the "OK" and "Cancel" buttons.
   * @param settings the initial value in the dialog's text field
   * @param responseHandler Will be invoked with the value entered by the user if "OK" was pressed.
   * <b style="color:red;">WARNING</b>: the value passed back to {@link ResponseHandler#handleDialogResponse(Object)}
   * will be {@code null} if user clicked the "Cancel" button instead of "OK"
   */
  public static void softPrompt(String msg, SoftDialogSettings settings, ResponseHandler<String> responseHandler) {
    enqueue(new SoftPrompt(msg, responseHandler, settings));
  }


  /**
   * Calls {@link Window#alert(String)} to display a message in a modal dialog box.
   * <p>
   * If that call fails because, for example, the user chose to "Prevent this page from creating additional dialogs"
   * (a checkbox provided by Chrome and Firefox), will fall back on showing our own HTML5 version of that dialog, which,
   * unfortunately we cannot make blocking (this method will return immediately, before user responds to the dialog).
   * That's why this method takes a responseHandler as a callback to invoke when the user has responded to the dialog.
   * <p>
   * Read the source code of the {@link NativeDialog#open()} method to see how when and how the fallback is triggered.
   *
   * @see <a href="http://stackoverflow.com/questions/5848381/why-prevent-this-page-from-creating-additional-dialogs-appears-in-the-alert-bo">StackOverflow: Why "Prevent this page from creating additional dialogs" appears in the alert box?</a>
   * @param msg the message to be displayed
   * @param responseHandler Will be invoked with the argument {@code null} when user dismisses this dialog.
   */
  public static void alert(String msg, ResponseHandler<Void> responseHandler) {
    if (nativeDialogsEnabled)
      enqueue(new WindowAlert(msg, responseHandler));
    else
      softAlert(msg, responseHandler);
  }

  /**
   * Same as {@link #alert(String, ResponseHandler)} but does not invoke a response handler when user responds to the dialog.
   */
  public static void alert(String msg) {
    alert(msg, ResponseHandler.NOOP);
  }


  /**
   * Calls {@link Window#confirm(String)} to display a message in a modal dialog box,
   * along with the standard 'OK' and 'Cancel' buttons.
   * <p>
   * {@linkplain #alert(String, ResponseHandler) If that call fails}, falls back on {@link #softConfirm(String, ResponseHandler)}
   *
   * @param msg the message to be displayed
   * @param responseHandler Will be invoked with the argument {@code true} if 'OK' is clicked or {@code false} if 'Cancel' is clicked
   */
  public static void confirm(String msg, ResponseHandler<Boolean> responseHandler) {
    if (nativeDialogsEnabled)
      enqueue(new WindowConfirm(msg, responseHandler));
    else
      softConfirm(msg, responseHandler);
  }

  /**
   * Same as {@link #confirm(String, ResponseHandler)} but does not invoke a response handler when user responds to the dialog.
   */
  public static void confirm(String msg) {
    confirm(msg, ResponseHandler.NOOP);
  }

  /**
   * Calls {@link Window#prompt(String, String)} to display a request for information in a modal dialog box,
   * along with the standard 'OK' and 'Cancel' buttons.
   * <p>
   * {@linkplain #alert(String, ResponseHandler) If that call fails}, falls back on {@link #softPrompt(String, SoftDialogSettings, ResponseHandler)}
   *
   * @param msg the message to be displayed
   * @param initialValue the initial value in the dialog's text field
   */
  public static void prompt(String msg, String initialValue, ResponseHandler<String> responseHandler) {
    if (nativeDialogsEnabled)
      enqueue(new WindowPrompt(msg, initialValue, responseHandler));
    else
      softPrompt(msg, initialValue, responseHandler);
  }

  /**
   * Same as {@link #prompt(String, String, ResponseHandler)} but does not invoke a response handler when user responds to the dialog.
   */
  public static void prompt(String msg, String initialValue) {
    prompt(msg, initialValue, ResponseHandler.NOOP);
  }


  //  --------------------------------------------------------------------------------
  //  Helper classes:
  //  --------------------------------------------------------------------------------

  /** A callback invoked right after the user closes the dialog, with the user's response as the argument */
  public interface ResponseHandler<T> {
    void handleDialogResponse(T response);

    /** Use this singleton if you don't wish to handle the response.*/
    ResponseHandler NOOP = new ResponseHandler() {
      @Override
      public void handleDialogResponse(Object response) { }
    };
  }

  /** Allows invoking a {@link ResponseHandler} asynchronously */
  private static class Responder<T> implements Scheduler.ScheduledCommand {
    private final ResponseHandler<T> responseHandler;
    private final T response;

    private Responder(ResponseHandler<T> responseHandler, T response) {
      this.responseHandler = responseHandler;
      this.response = response;
    }

    @Override
    public void execute() {
      responseHandler.handleDialogResponse(response);
    }
  }

  /**
   * Base class for showing dialogs.
   * @param <T> type of value returned by the dialog
   */
  private static abstract class Dialog<T> {

    protected final String msg;
    protected ResponseHandler<T> responseHandler;

    private Dialog(String msg, ResponseHandler<T> responseHandler) {
      this.msg = msg;
      this.responseHandler = responseHandler;
    }

    abstract void open();
    abstract void close();

    /**
     * Invokes {@link #responseHandler} with the given argument and hides the dialog.
     */
    protected void returnResponse(T response) {
      if (responseHandler != null) {
        // we use a deferred command for invoking the response handler so we can still hide the dialog in case it throws,
        // and to make the response handling logic for native dialogs happen asynchronously, like it does for native dialogs
        Scheduler.get().scheduleDeferred(new Responder<T>(responseHandler, response));
        responseHandler = null;  // we clear the responseHandler to be sure we never call it more than once (which could happen if, for example, user keeps holding down Enter key, in which case browser keeps issuing click events while it's down)
        close();
      }
    }

    public String getMessage() {
      return msg;
    }
  }

  /**
   * Shows an instance of {@link SoftModalDialogBox}
   */
  private static abstract class SoftDialog<T> extends Dialog<T> {

    private SoftModalDialogBox<T> dialogBox;
    private Duration showingDuration;
    protected SoftDialogSettings settings;

    private SoftDialog(String msg, SoftDialogSettings settings, ResponseHandler<T> responseHandler) {
      super(msg, responseHandler);
      this.settings = settings;
    }

    protected abstract SoftModalDialogBox<T> createDialogBox();

    @Override
    public void open() {
      if (dialogBox == null) {
        dialogBox = createDialogBox();
        showingDuration = new Duration();
      }
      dialogBox.setPopupPositionAndShow(dialogBox);
    }

    @Override
    void close() {
      dialogBox.hide();
    }

    /**
     * This method is overridden to make sure the dialog stays open for {@link #minDialogShowingTime}
     */
    @Override
    protected void returnResponse(T response) {
      if (showingDuration.elapsedMillis() > minDialogShowingTime)
        super.returnResponse(response);
      else
        dialogBox.shake();
    }
  }

  private static class SoftAlert extends SoftDialog<Void> {

    public SoftAlert(String msg, SoftDialogSettings settings, ResponseHandler<Void> responseHandler) {
      super(msg, settings, responseHandler);
    }

    public SoftAlert(String msg, ResponseHandler<Void> responseHandler) {
      super(msg, new SoftDialogSettings(), responseHandler);
    }

    @Override
    protected SoftModalDialogBox<Void> createDialogBox() {
      return new SoftAlertBox(this, settings.caption);
    }
  }
  
  private static class SoftConfirm extends SoftDialog<Boolean> {

    public SoftConfirm(String msg, ResponseHandler<Boolean> responseHandler, SoftDialogSettings settings) {
      super(msg, settings, responseHandler);
    }

    public SoftConfirm(String msg, ResponseHandler<Boolean> responseHandler) {
      this(msg, responseHandler, new SoftDialogSettings());
    }

    @Override
    protected SoftModalDialogBox<Boolean> createDialogBox() {
      return new SoftConfirmBox(this, settings.caption);
    }
  }

  private static class SoftPrompt extends SoftDialog<String> {

    public SoftPrompt(String msg, ResponseHandler<String> responseHandler, SoftDialogSettings settings) {
      super(msg, settings, responseHandler);
    }

    private SoftPrompt(String msg, String initialValue, ResponseHandler<String> responseHandler) {
      this(msg, responseHandler, new SoftDialogSettings().setInitialValue(initialValue));
    }

    @Override
    protected SoftModalDialogBox<String> createDialogBox() {
      return new SoftPromptBox(this);
    }
  }

  public static class SoftDialogSettings {
    private String okLabel;
    private String cancelLabel;
    private DialogBox.Caption caption;
    private String initialValue;

    /**
     * Use chained setters to specify the settings
     */
    public SoftDialogSettings() {
    }

    public SoftDialogSettings setOkLabel(String okLabel) {
      this.okLabel = okLabel;
      return this;
    }

    public SoftDialogSettings setCancelLabel(String cancelLabel) {
      this.cancelLabel = cancelLabel;
      return this;
    }

    public SoftDialogSettings setCaption(DialogBox.Caption caption) {
      this.caption = caption;
      return this;
    }

    public SoftDialogSettings setInitialValue(String initialValue) {
      this.initialValue = initialValue;
      return this;
    }

    public String getOkLabel() {
      return okLabel;
    }

    public String getCancelLabel() {
      return cancelLabel;
    }

    public DialogBox.Caption getCaption() {
      return caption;
    }

    public String getInitialValue() {
      return initialValue;
    }
  }

  /**
   * Invoke a native dialog method.
   */
  private static abstract class NativeDialog<T> extends Dialog<T> {

    private NativeDialog(String msg, ResponseHandler<T> responseHandler) {
      super(msg, responseHandler);
    }

    protected abstract T showNativeDialog();
    protected abstract void showSoftDialog();

    /**
     * Calls {@link #showNativeDialog()}, and if that throws an exception or returns in less than {@link #MIN_BLOCKING_MILLIS},
     * calls {@link #showSoftDialog()}.
     */
    @Override
    public void open() {
      Duration blockingTime = new Duration();
      T nativeResponse = null;
      try {
        nativeResponse = showNativeDialog();
      }
      catch (Exception ex) {
        // Firefox throws a JavaScriptException (NS_ERROR_NOT_AVAILABLE), when the user checked "Prevent this page from creating additional dialogs"
        GWT.log("showNativeDialog threw an exception", ex);
        closeAndShowSoftDialog();  // fall back on our own "soft" implementation
        return;
      }
      // Chrome, however, does not throw an exception when the user checked "Prevent this page from creating additional dialogs",
      // instead it immediately returns a nativeResponse to the caller as if the user clicked "Cancel", but doesn't actually
      // show the dialog, so we have to measure the native call duration to decide whether the native dialog ever appeared
      // (this approach was suggested by http://stackoverflow.com/a/23523267)
      if (blockingTime.elapsedMillis() < Math.max(MIN_BLOCKING_MILLIS, minDialogShowingTime)) {
        closeAndShowSoftDialog();  // fall back on our own "soft" implementation
      }
      else {
        returnResponse(nativeResponse);
      }
    }

    private void closeAndShowSoftDialog() {
      // NOTE: we have to first call dialogClosed in order to be able to show a fallback, but we also don't want
      // to show next in queue before the fallback, so we pass false to dialogClosed
      dialogClosed(this, false);
      showSoftDialog();
    }

    @Override
    void close() {
      // we don't have to do anything here, since the browser already closed the native dialog
      dialogClosed(this, true);
    }
  }

  private static class WindowAlert extends NativeDialog<Void> {
    private WindowAlert(String msg, ResponseHandler<Void> responseHandler) {
      super(msg, responseHandler);
    }
    @Override
    protected Void showNativeDialog() {
      Window.alert(msg);
      return null;
    }
    @Override
    protected void showSoftDialog() {
      enqueueFirst(new SoftAlert(msg, responseHandler));
    }
  }

  private static class WindowConfirm extends NativeDialog<Boolean> {
    private WindowConfirm(String msg, ResponseHandler<Boolean> responseHandler) {
      super(msg, responseHandler);
    }
    @Override
    protected Boolean showNativeDialog() {
      return Window.confirm(msg);
    }
    @Override
    protected void showSoftDialog() {
      enqueueFirst(new SoftConfirm(msg, responseHandler));
    }
  }

  private static class WindowPrompt extends NativeDialog<String> {
    private final String initialValue;
    private WindowPrompt(String msg, String initialValue, ResponseHandler<String> responseHandler) {
      super(msg, responseHandler);
      this.initialValue = initialValue;
    }
    @Override
    protected String showNativeDialog() {
      return Window.prompt(msg, initialValue);
    }
    @Override
    protected void showSoftDialog() {
      enqueueFirst(new SoftPrompt(msg, initialValue, responseHandler));
    }
  }

  /**
   * The actual popup for showing a {@link SoftDialog}
   *
   * @param <T> type of value returned by the dialog
   */
  private static abstract class SoftModalDialogBox<T> extends DialogBox implements PopupPanel.PositionCallback, CloseHandler<PopupPanel>, ClickHandler, KeyDownHandler, ResizeHandler, Window.ScrollHandler {

    private final SoftDialog<T> invoker;
    protected final FlowPanel pnlBody;
    protected final HTML lblMessage;
    protected final FocusTrap leftFocusTrap;
    protected final FocusTrap rightFocusTrap;
    protected final Button btnOK;
    protected final Button btnCancel;
    private MultiHandlerRegistration windowHandlersRegistration;
    private int naturalMessageWidth;

    private SoftModalDialogBox(SoftDialog<T> invoker) {
      super(false, true, getOrCreateCaption(invoker));
      this.invoker = invoker;
      addStyleName(CommonsClientBundleFactory.INSTANCE.getCss().SoftModalDialogBox());
      SoftDialogSettings settings = invoker.settings;
      String okLabel = settings.okLabel != null ? settings.okLabel : "OK";
      String cancelLabel = settings.cancelLabel != null ? settings.cancelLabel : "Cancel";
      btnOK = new Button(okLabel, this);
      btnCancel = new Button(cancelLabel, this);
      pnlBody = Widgets.flowPanel(
          // sanitize the message (converting \n chars within the message to <br> elements)
          lblMessage = new HTML(new SafeHtmlBuilder().appendEscapedLines(invoker.getMessage()).toSafeHtml()),
          Widgets.flowPanel(new WidgetStyle(CommonsClientBundleFactory.INSTANCE.getCss().dialogButtons()),
              leftFocusTrap = new FocusTrap(btnCancel),
              btnOK,
              btnCancel,
              rightFocusTrap = new FocusTrap(btnOK))
      );
      lblMessage.setStyleName(CommonsClientBundleFactory.INSTANCE.getCss().dialogMessage());

      btnCancel.getElement().getStyle().setMarginLeft(8, Style.Unit.PX);
      setWidget(pnlBody);
      setGlassEnabled(true);
      addCloseHandler(this);
      addDomHandler(this, KeyDownEvent.getType());  // handle the Esc key to close the dialog
    }

    private static Caption getOrCreateCaption(SoftDialog invoker) {
      if (invoker.settings.caption != null)
        return invoker.settings.caption;
      else {
        HtmlCaption defaultCaption = new HtmlCaption();
        defaultCaption.addStyleName(CommonsClientBundleFactory.INSTANCE.getCss().DefaultCaption());
        return defaultCaption;
      }
    }

    /**
     * @return The value to be passed to {@link #returnResponse} when user responds to this dialog with "OK"
     */
    protected abstract T getValueForOK();

    /**
     * @return The value to be passed to the {@link #returnResponse} when user responds to this dialog with "Cancel"
     */
    protected abstract T getValueForCancel();

    /**
     * @return The widget that should be focused right after the dialog is displayed.
     */
    @Override
    public FocusWidget getFocusTarget() {
      return btnOK;
    }

    /**
     * Invokes {@link Dialog#returnResponse(Object)} with the given argument.
     */
    protected void returnResponse(T response) {
      invoker.returnResponse(response);
    }

    @Override
    public void onClick(ClickEvent event) {
      // NOTE: this handler will also be invoked when the user presses the Enter key on either of the buttons
      Object src = event.getSource();
      if (src == btnOK)
        returnResponse(getValueForOK());
      else if (src == btnCancel)
        returnResponse(getValueForCancel());
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
      NativeEvent ev = event.getNativeEvent();
      switch (ev.getKeyCode()) {
        case KeyCodes.KEY_ESCAPE:
          returnResponse(getValueForCancel());
          break;
      }
    }

    /**
     * Called just before the popup is made visible but after its layout has been performed and all the widget sizes and
     * positions are known.
     */
    @Override
    public void setPosition(int offsetWidth, int offsetHeight) {
      // the first time this dialog is shown, we want to do the following:
      // 1) register handlers that will update the dialog's position when the browser window is scrolled or resized
      // 2) save the "natural" width of the message (how the browser would render it if the dialog size is not constrained)
      if (windowHandlersRegistration == null) {
        windowHandlersRegistration = new MultiHandlerRegistration(
            Window.addWindowScrollHandler(this),
            Window.addResizeHandler(this));
        naturalMessageWidth = lblMessage.getOffsetWidth() + 1;  // +1 pixel just in case, because the actual width might have been e.g. 179.46 but rounding it to an int would have shrunk it down to 179
        maybeAdjustMessageWidth();
      }
      adjustPosition();
    }

    /**
     * Set the position of the popup such it's horizontally centered and 1/3 of the window height from the top.
     */
    private void adjustPosition() {
      setPopupPositionRelativeToWindowScrollAndSize(.5, .333);
    }

    /**
     * Constrains the width of the message so that it takes up no more than half of the browser screen.
     */
    private void maybeAdjustMessageWidth() {
      lblMessage.getElement().getStyle().setWidth(Math.min(Window.getClientWidth() / 2, naturalMessageWidth), Style.Unit.PX);
    }

    /**
     * Updates the dialog's position when the browser window is resized.
     */
    @Override
    public void onResize(ResizeEvent event) {
      if (isShowing()) {
        maybeAdjustMessageWidth();
        adjustPosition();
      }
    }

    /**
     * Updates the dialog's position when the browser window is resized.
     */
    @Override
    public void onWindowScroll(Window.ScrollEvent event) {
      if (isShowing())
        adjustPosition();
    }

    @Override
    public void onClose(CloseEvent<PopupPanel> event) {
      if (windowHandlersRegistration != null) {
        windowHandlersRegistration.removeHandler();
        windowHandlersRegistration = null;
      }
      dialogClosed(invoker, true);
    }
  }

  /**
   * Emulates the browser dialog for {@code window.alert}
   */
  private static class SoftAlertBox extends SoftModalDialogBox<Void> {

    private SoftAlertBox(SoftAlert invoker, Caption caption) {
      super(invoker);
      leftFocusTrap.setMaster(btnOK); // alert dialog has no Cancel button
    }

    @Override
    public void setVisible(boolean visible) {
      super.setVisible(visible);
      if (visible && btnCancel.isAttached())
        // the "alert" dialog should not have a Cancel button (but we remove it only after its width has been used to set the width of btnOK in the setPosition method)
        btnCancel.removeFromParent();
    }
    @Override
    protected Void getValueForOK() {
      return null;  // alert dialog does not return a value
    }
    @Override
    protected Void getValueForCancel() {
      return null;  // alert dialog does not return a value
    }
  }

  /**
   * Emulates the browser dialog for {@code window.confirm}
   */
  private static class SoftConfirmBox extends SoftModalDialogBox<Boolean> {
    private SoftConfirmBox(SoftConfirm invoker, Caption caption) {
      super(invoker);
    }
    @Override
    protected Boolean getValueForOK() {
      return true;
    }
    @Override
    protected Boolean getValueForCancel() {
      return false;
    }
  }

  /**
   * Emulates the browser dialog for {@code window.prompt}
   */
  private static class SoftPromptBox extends SoftModalDialogBox<String> {
    private TextBox txtInput;
    private SoftPromptBox(SoftPrompt invoker) {
      super(invoker);
      // add the text input field to the standard dialog and adjust the focus traps accordingly
      txtInput = new TextBox();
      txtInput.setStyleName(CommonsClientBundleFactory.INSTANCE.getCss().dialogInput());
      String initialValue = invoker.settings.getInitialValue();
      if (initialValue != null)
        txtInput.setText(initialValue);
      pnlBody.insert(txtInput, 1);
      pnlBody.insert(leftFocusTrap, 1);
      rightFocusTrap.setMaster(txtInput);
    }
    @Override
    public FocusWidget getFocusTarget() {
      return txtInput;
    }
    @Override
    public void onKeyDown(KeyDownEvent event) {
      // handle the Enter key on the text box
      NativeEvent ev = event.getNativeEvent();
      if (ev.getEventTarget() == txtInput.getElement().cast() && ev.getKeyCode() == KeyCodes.KEY_ENTER) {
        // NOTE: we have to call EventUtils.eatEvent because we don't want this keystroke percolating down to any other dialogs
        // (e.g. if the responseHandler shows another dialog, this Enter key down event would close it immediately by issuing a "click" event)
        Events.eatEvent(ev);
        returnResponse(getValueForOK());
      }
      super.onKeyDown(event);
    }
    @Override
    protected String getValueForOK() {
      return txtInput.getText();
    }
    @Override
    protected String getValueForCancel() {
      return null;
    }
  }

  /**
   * A widget containing buttons for manually testing {@link ModalDialog}.
   */
  public static class TestWidget extends Composite {
    private SoftDialogSettings softDialogSettings;

    public TestWidget() {
      this(null);
    }

    public TestWidget(SoftDialogSettings softDialogSettings) {
      this.softDialogSettings = softDialogSettings;
      // TODO: cont here: extract methods to use the settings (if not null)
      initWidget(Widgets.verticalPanel(
          new Label(getClass().getName() + " settings: " + printSettings(softDialogSettings)),
          new Button("Test softConfirm/softAlert", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              softConfirm("softConfirm 1 2 3 4 5 hello 1 2 3 4 5 hello 1 2 3 4 5 hello\nLine2\n\nLine3", new ResponseHandler<Boolean>() {
                @Override
                public void handleDialogResponse(Boolean response) {
                  ModalDialog.alert("softConfirm response = " + response);
                  softAlert("softAlert\nLine2\n\nLine3", new ResponseHandler<Void>() {
                    @Override
                    public void handleDialogResponse(Void response) {
                      ModalDialog.alert("softAlert response = " + response);
                    }
                  });
                }
              });
            }
          }),
          new Button("Test ModalDialog.confirm/prompt", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              ModalDialog.confirm("ModalDialog 1 2 3 4 5 hello 1 2 3 4 5 hello 1 2 3 4 5 hello\nLine2\n\nLine3", new ResponseHandler<Boolean>() {
                @Override
                public void handleDialogResponse(Boolean response) {
                  ModalDialog.alert("ModalDialog.confirm response = " + response);
                  ModalDialog.setNativeDialogsEnabled(false);
                  ModalDialog.prompt("ModalDialog 1 2 3 4 5 hello 1 2 3 4 5 hello 1 2 3 4 5 hello\nLine2\n\nLine3", "foo", new ResponseHandler<String>() {
                    @Override
                    public void handleDialogResponse(String response) {
                      ModalDialog.alert("ModalDialog.prompt response = " + response);
                    }
                  });
                }
              });
            }
          }),
          new Button("Test ModalDialog.alert fallback", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              class Alerter implements ResponseHandler<Void> {
                private final int ordinal;

                Alerter(int ordinal) {
                  this.ordinal = ordinal;
                  ModalDialog.alert("ModalDialog.alert " + ordinal, this);
                }

                @Override
                public void handleDialogResponse(Void response) {
                  ModalDialog.alert("ModalDialog.alert " + ordinal + " response = " + response);
                }
              }
              new Alerter(1);
              new Alerter(2);
              new Alerter(3);
              new Alerter(4);
              new Alerter(5);
              new Alerter(6);
              new Alerter(7);
              new Alerter(8);
              new Alerter(9);
              new Alerter(10);
            }
          }),
          new Button("Test ModalDialog.prompt", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              ModalDialog.prompt("ModalDialog.prompt 1 2 3 4 5 hello 1 2 3 4 5 hello 1 2 3 4 5 hello\nLine2\n\nLine3", "foo",
                  new ResponseHandler<String>() {
                    @Override
                    public void handleDialogResponse(String response) {
                      ModalDialog.alert("ModalDialog.prompt response = " + response);
                    }
                  });
            }
          }),
          new Button("Test softPrompt", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              softPrompt("softPrompt 1 2 3 4 5 hello 1 2 3 4 5 hello 1 2 3 4 5 hello\nLine2\n\nLine3", "foo", new ResponseHandler<String>() {
                @Override
                public void handleDialogResponse(String response) {
                  ModalDialog.alert("softPrompt response = " + response);
                }
              });
            }
          })
      ));
    }

    private static String printSettings(SoftDialogSettings settings) {
      if (settings == null)
        return "null";
      // we use a custom method here (instead of implementing SoftDialogSettings.toString) to allow dead code elimination
      final StringBuilder sb = new StringBuilder("SoftDialogSettings{");
      sb.append("okLabel='").append(settings.okLabel).append('\'');
      sb.append(", cancelLabel='").append(settings.cancelLabel).append('\'');
      sb.append(", caption=").append(GwtUtils.toString(settings.caption));
      sb.append(", initialValue='").append(settings.initialValue).append('\'');
      sb.append('}');
      return sb.toString();
    }

    private void softPrompt(String msg, String initialValue, ResponseHandler<String> responseHandler) {
      if (softDialogSettings != null) {
        ModalDialog.softPrompt(msg, softDialogSettings, responseHandler);
      }
      else
        ModalDialog.softPrompt(msg, initialValue, responseHandler);
    }

    private void softConfirm(String msg, ResponseHandler<Boolean> responseHandler) {
      if (softDialogSettings != null)
        ModalDialog.softConfirm(msg, softDialogSettings, responseHandler);
      else
        ModalDialog.softConfirm(msg, responseHandler);
    }

    private void softAlert(String msg, ResponseHandler<Void> responseHandler) {
      if (softDialogSettings != null)
        ModalDialog.softAlert(msg, softDialogSettings, responseHandler);
      else
        ModalDialog.softAlert(msg, responseHandler);
    }
  }

}
