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

package solutions.trsoftware.commons.client.controller;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;
import solutions.trsoftware.commons.client.Messages;
import solutions.trsoftware.commons.client.debug.Debug;
import solutions.trsoftware.commons.client.images.CommonsImages;
import solutions.trsoftware.commons.client.jso.JsConsole;
import solutions.trsoftware.commons.client.jso.JsConsole.Level;
import solutions.trsoftware.commons.client.widgets.popups.ModalDialog;
import solutions.trsoftware.commons.client.widgets.popups.PleaseWaitPopup;
import solutions.trsoftware.commons.shared.util.compare.RichComparable;
import solutions.trsoftware.commons.shared.util.reflect.ClassNameParser;
import solutions.trsoftware.commons.shared.util.time.Clock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.client.controller.BaseRpcAction.State.*;
import static solutions.trsoftware.commons.shared.util.reflect.ClassNameParser.parseClassName;

/**
 * @param <T> the return type of the RPC method
 *
 * @author Alex, 9/22/2017
 */
public abstract class BaseRpcAction<T> implements Command, AsyncCallback<T> {

  /**
   * Any further attempt to invoke an RPC will result in user being prompted to reload the page.
   * @see #suspendRPCsAndPromptToReloadPage()
   */
  private static boolean suspendedUntilPageReload;
  /**
   * Will be used to track whether user clicks "Cancel" when prompted to reload the page.
   * @see #suspendRPCsAndPromptToReloadPage()
   */
  private static boolean reloadPromptShowing;

  private static boolean loggingEnabled = Debug.ENABLED;

  private static int nextId;

  /** Sequence number of this RPC call */
  protected final int id = ++nextId;
  /** allows timing RPC calls */
  protected long startTime;
  /** allows timing RPC calls */
  protected long endTime;
  private PleaseWaitPopup busyPopup;

  /** Description of this action (defaults to name of the concrete action class) */
  protected String name;

  private EventBus eventBus;

  /** Current state of this action */
  private State state = NEW;

  /**
   * Either {@link Success} from {@link #onSuccess(Object)}, {@link Failure} from {@link #onFailure(Throwable)},
   * or {@code null} if the RPC call hasn't completed yet or was {@linkplain State#REJECTED rejected}.
   */
  @Nullable
  private Outcome outcome;

  protected BaseRpcAction() {
    this(null);
  }

  /**
   * @param name A name for this action (for logging / debugging);
   *   if {@code null} will {@linkplain #defaultName() default} to the name of the concrete action subclass
   */
  protected BaseRpcAction(@Nullable String name) {
    this.name = name != null ? name : defaultName();
    initLogging();
  }

  private String defaultName() {
    Class<? extends BaseRpcAction> cls = getClass();
    ClassNameParser clsName = parseClassName(cls);
    String name = clsName.getSimpleName();
    if (name.isEmpty()) {
      // class is anonymous: use full class name and add super's name
      name = clsName.getComplexName() + "(extends " + parseClassName(cls.getSuperclass()).getComplexName() + ")";
    }
    return name;
  }

  public String getName() {
    return name;
  }

  /**
   * Shows a modal dialog telling the user he needs to reload the page and do the reload automatically if he hits "OK".
   * This method may return before the user has responded to the dialog. TODO: explain this
   */
  protected static void suspendRPCsAndPromptToReloadPage() {
    suspendedUntilPageReload = true;
    maybePromptToReloadPage();
  }

  protected static void maybePromptToReloadPage() {
    if (!reloadPromptShowing) {
      reloadPromptShowing = true;
      ModalDialog.softConfirm(Messages.get().reloadAppMessage(), response -> {
        if (response)
          Window.Location.reload();
        reloadPromptShowing = false;
      });
    }
  }

  public final void onFailure(Throwable caught) {
    try {
      setOutcome(new Failure(caught));
      if (caught instanceof IncompatibleRemoteServiceException) {
        /* When a RemoteServiceServlet determines that the client code version doesn't match what's currently deployed on the server,
        it will return an IncompatibleRemoteServiceException response (using RPC.encodeResponseForFailure) to the RPC.
        However there seems to be a GWT bug with the client-side deserialization of that response
        because AbstractSerializationStreamReader.readObject computes typeSignature = "com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException/3936916533",
        instead of an obfuscated typeSignature (like 's' or 'c'); example normal response: //OK[0,15,1.443469202763E12,0,11,0,0,0,0,2,14,0.0,0,13,0,0,0,0.0,0.0,0.0,12,11,0,0,10,9,8,0,7,0,6,5,0,4,0,3,2,1,["s","c","6","g","126A7FC4E5080DE58FC56EC1D6A4AD8D","w","us","h","Guest","","guest:2457920816943966115","j","1t","r","10"],1,7],
        Therefore this method will receive IncompatibleRemoteServiceException("The response could not be deserialized") from RpcCallbackAdapter.java:93,
        which is perfectly fine, because although it's not the same IncompatibleRemoteServiceException object that was in the server's response,
        it's still an IncompatibleRemoteServiceException, so we just ignore the fact that its message is "The response could not be deserialized",
        and handle it as though it signifies that the client's app code version is incompatible. */
        suspendRPCsAndPromptToReloadPage();
      }
      else {
        // for any other exception, let the subclass handle it
        handleFailure(caught);
      }
    }
    finally {
      reportCompletion();
    }
  }

  /** Subclasses should override to provide handling for exceptions that might be thrown by their particular RPCs */
  protected void handleFailure(Throwable caught) {
    throw new RpcActionFailedException(this, caught); // this is a last resort; let the UncaughtExceptionHandler deal with it
  }

  protected abstract void handleSuccess(T result);

  public int getRoundTripTime() {
    return (endTime == 0) ? 0 : (int)(endTime - startTime);
  }

  public final void onSuccess(T result) {
    try {
      setOutcome(new Success(result));
      handleSuccess(result);
    }
    finally {
      reportCompletion();
    }
  }

  /**
   * Called after either {@link #onSuccess(Object)} or {@link #onFailure(Throwable)} to perform any cleanup task
   * that needs to happen regardless of success or failure of the action.
   */
  protected void onFinished() {

  }

  /**
   * Initiates the RPC call implemented by {@link #executeRpcAction()}.
   */
  public final void execute() {
    if (state != NEW) {
      if (state == EXECUTING)
        throw new IllegalStateException("Already executing " + name);
      else
        throw new IllegalStateException("Already finished " + name + "; outcome: " + state);
      // TODO(4/6/2026): might be too harsh to throw here; perhaps original intent was to allow repeated runs?
    }
    if (suspendedUntilPageReload) {
      maybePromptToReloadPage();
      // since neither onSuccess nor onFailure will ever be called, we call onFinished in a deferred command
      // to allow the subclass to clean up (e.g. hide a popup dialog that triggered this action)
      Scheduler.get().scheduleDeferred(this::finish);
      state = REJECTED;
    }
    else {
      // invoke the RPC call
      state = EXECUTING;
      startTime = currentTimeMillis();
      endTime = 0;  // reset the last value, if any
      if (busyPopup != null)
        busyPopup.showRelativeToWindow(.5, .333);
      executeRpcAction();
      fireEvent(ExecuteEvent::new);
    }
  }

  /**
   * Invoke the desired "Async" proxy method of the {@link RemoteService} using {@code this} instance as the
   * {@link AsyncCallback}.
   */
  protected abstract void executeRpcAction();

  public State getState() {
    return state;
  }

  @Nullable
  public Outcome getOutcome() {
    return outcome;
  }

  /**
   * This should be invoked immediately from {@link #onSuccess(Object)} or {@link #onFailure(Throwable)},
   * to ensure that {@link #getOutcome()} and {@link #getRoundTripTime()} are available when the subclass
   * {@link #handleSuccess(Object)} and {@link #handleFailure(Throwable)} methods are invoked.
   */
  private void setOutcome(@Nonnull Outcome outcome) {
    endTime = currentTimeMillis();
    this.outcome = requireNonNull(outcome, "outcome");
    state = outcome.getCompletionState();
  }

  private void reportCompletion() {
    requireNonNull(outcome, "outcome");
    try {
      fireEvent(outcome::createEvent);
    }
    finally {
      finish();
    }
  }

  private void finish() {
    try {
      if (busyPopup != null)
        busyPopup.hide();
      onFinished();
    }
    finally {
      fireEvent(FinishedEvent::new);
    }
  }

  private long currentTimeMillis() {
    return Clock.currentTimeMillis();  // NOTE(2/9/2026): using Clock instead of Duration to allow testing without GWTTestCase
  }

  /**
   * Subclasses that wish to show a "please wait" popup message while the RPC is executing should call
   * this method with their customized message.
   */
  public void showBusyMessage(String message) {
    if (busyPopup == null)
      busyPopup = new PleaseWaitPopup(message, AbstractImagePrototype.create(CommonsImages.INSTANCE.info24()));
    if (!busyPopup.isShowing())
      busyPopup.showRelativeToWindow(.5, .333);
  }

  public void setBusyPopup(PleaseWaitPopup busyPopup, boolean modal) {
    this.busyPopup = busyPopup;
    busyPopup.setGlassEnabled(modal);
  }

  /**
   * Lazy-inits {@link #eventBus} and returns it.
   */
  private EventBus getEventBus() {
    if (eventBus == null)
      eventBus = createEventBus();  // lazy init
    return eventBus;
  }

  /**
   * @return A new instance of {@link SimpleEventBus}.
   * Subclasses may override to provide a different {@link EventBus} implementation.
   */
  protected EventBus createEventBus() {
    return new SimpleEventBus();
  }

  private void fireEvent(Supplier<RpcEvent<?>> eventSupplier) {
    if (eventBus != null) {
      // Note: eventBus is null when no handlers have been added, so no need to fire event in that case
      eventBus.fireEventFromSource(eventSupplier.get(), this);
    }
  }

  public HandlerRegistration addExecuteHandler(ExecuteEvent.Handler handler) {
    return getEventBus().addHandlerToSource(ExecuteEvent.TYPE, this, handler);
  }

  public HandlerRegistration addFinishedHandler(FinishedEvent.Handler handler) {
    return getEventBus().addHandlerToSource(FinishedEvent.TYPE, this, handler);
  }

  public HandlerRegistration addSuccessHandler(SuccessEvent.Handler<T> handler) {
    return getEventBus().addHandlerToSource(SuccessEvent.TYPE, this, handler);
  }

  public HandlerRegistration addFailureHandler(FailureEvent.Handler handler) {
    return getEventBus().addHandlerToSource(FailureEvent.TYPE, this, handler);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("name", name)
        .add("state", state)
        .toString();
  }

  /**
   * Generates a log message describing the current state/outcome of this RPC.
   * @see #initLogging()
   */
  private String toLogMessage() {
    /* Examples:
       - "RPC executing: EnlargeAreaAction#1"
       - "RPC success: EnlargeAreaAction#1 (899 ms)"
       - "RPC failure: EnlargeAreaAction#1 (43 ms) threw {com.google.gwt.user.client.rpc.StatusCodeException: 500 Server Error The call failed on the server; see server log for details}"
     */
    StringBuilder msg = new StringBuilder("RPC ").append(state.name().toLowerCase()).append(": ")
        .append(name).append("#").append(id);
    if (outcome != null) {
      msg.append(" (").append(getRoundTripTime()).append(" ms)");  // RTT
      if (outcome instanceof BaseRpcAction.Failure)
        msg.append(" threw {").append(((Failure)outcome).getException()).append('}');
    }
    return msg.toString();
  }

  private void initLogging() {
    if (loggingEnabled) {
      JsConsole console = JsConsole.get();
//      addExecuteHandler(event -> console.log(Level.DEBUG, "Invoking RPC: " + name));
      addExecuteHandler(event -> console.log(Level.DEBUG, toLogMessage()));
      addFinishedHandler(event -> console.log(state == SUCCESS ? Level.DEBUG : Level.WARN, toLogMessage()));
    }
  }

  public static boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  public static void setLoggingEnabled(boolean loggingEnabled) {
    BaseRpcAction.loggingEnabled = loggingEnabled;
  }

  @VisibleForTesting
  static boolean isSuspendedUntilPageReload() {
    return suspendedUntilPageReload;
  }

  @VisibleForTesting
  static void setSuspendedUntilPageReload(boolean suspendedUntilPageReload) {
    BaseRpcAction.suspendedUntilPageReload = suspendedUntilPageReload;
  }

  @VisibleForTesting
  static boolean isReloadPromptShowing() {
    return reloadPromptShowing;
  }

  public static class RpcActionFailedException extends RuntimeException {

    public RpcActionFailedException(BaseRpcAction<?> rpcAction, Throwable cause) {
      super(rpcAction.toLogMessage(), cause);
    }

    public RpcActionFailedException(String message, Throwable cause) {
      super(message, cause);
    }

    private RpcActionFailedException() {
    }
  }

  public enum State implements RichComparable<State> {
    // partial copy of the states defined in java.util.concurrent.FutureTask
    NEW,
    EXECUTING,
    // terminal states:
    REJECTED, SUCCESS, FAILURE;

    public boolean isFinished() {
      return isGreaterThan(EXECUTING);
    }
  }

  /**
   * Outcome of the RPC request, wrapping the returned object or thrown exception.
   */
  @SuppressWarnings("InnerClassMayBeStatic")
  public abstract class Outcome {
    protected abstract RpcEvent<?> createEvent();
    protected abstract State getCompletionState();
  }

  /**
   * RPC completed via {@link #onSuccess(Object)}
   * @param <T> the result type
   */
  public class Success extends Outcome {
    private final T result;

    public Success(T result) {
      this.result = result;
    }

    public T getResult() {
      return result;
    }

    @Override
    protected SuccessEvent<T> createEvent() {
      return new SuccessEvent<>(result);
    }

    @Override
    protected State getCompletionState() {
      return SUCCESS;
    }
  }

  /**
   * RPC completed via {@link #onFailure(Throwable)}
   */
  public class Failure extends Outcome {
    private final Throwable exception;

    public Failure(Throwable exception) {
      this.exception = exception;
    }

    public Throwable getException() {
      return exception;
    }

    @Override
    protected FailureEvent createEvent() {
      return new FailureEvent(exception);
    }

    @Override
    protected State getCompletionState() {
      return FAILURE;
    }
  }
}
